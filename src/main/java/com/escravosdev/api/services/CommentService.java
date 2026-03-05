package com.escravosdev.api.services;

import com.escravosdev.api.dtos.request.CreateCommentRequest;
import com.escravosdev.api.dtos.response.CommentResponse;
import com.escravosdev.api.entities.Comment;
import com.escravosdev.api.entities.enums.PostType;
import com.escravosdev.api.entities.discord.DiscordRoles;
import com.escravosdev.api.repo.CommentRepo;
import com.escravosdev.api.repo.PostRepo;
import com.escravosdev.api.repo.UserRepo;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepo commentRepo;
    private final PostRepo postRepo;
    private final UserRepo userRepo;
    private final VoteService voteService;

    public List<CommentResponse> listByPost(UUID postId, Claims claims) {
        var comments = commentRepo.findByPostIdFetched(postId);

        return comments.stream()
                .filter(c -> c.getParent() == null)
                .map(root -> {
                    var rootVotes = voteService.buildCommentVoteResponse(root.getId(), claims);
                    var replies = comments.stream()
                            .filter(c -> c.getParent() != null && c.getParent().getId().equals(root.getId()))
                            .map(reply -> {
                                var replyVotes = voteService.buildCommentVoteResponse(reply.getId(), claims);
                                return CommentResponse.from(reply, List.of(), replyVotes);
                            })
                            .toList();
                    return CommentResponse.from(root, replies, rootVotes);
                })
                .toList();
    }

    @Transactional
    public CommentResponse create(UUID postId, CreateCommentRequest req, Claims claims) {
        var post = postRepo.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // dúvidas: só Orientador e ADM comentam
        if (post.getType() == PostType.QUESTION) {
            if (!DiscordRoles.canAnswerDuvida(claims)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Apenas Orientadores e ADMs podem responder dúvidas");
            }
        }

        var author = userRepo.findByDiscordId(claims.getSubject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (author.isMuted() || author.isBanned()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você está impedido de comentar");
        }

        var comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setContent(req.content());

        if (req.parentId() != null) {
            var parent = commentRepo.findById(req.parentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comentário pai não encontrado"));
            // não permite reply de reply (só 2 níveis)
            if (parent.getParent() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é permitido responder uma resposta");
            }
            comment.setParent(parent);
        }

        return CommentResponse.from(commentRepo.save(comment), List.of());
    }

    @Transactional
    public CommentResponse delete(UUID commentId) {
        var comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        comment.setDeleted(true);
        return CommentResponse.from(commentRepo.save(comment), List.of());
    }
}