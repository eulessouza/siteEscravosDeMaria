package com.escravosdev.api.services;

import com.escravosdev.api.dtos.response.VoteResponse;
import com.escravosdev.api.entities.Vote;
import com.escravosdev.api.entities.enums.PostType;
import com.escravosdev.api.entities.enums.VoteType;
import com.escravosdev.api.repo.*;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepo voteRepo;
    private final PostRepo postRepo;
    private final CommentRepo commentRepo;
    private final UserRepo userRepo;

    @Transactional
    public VoteResponse voteOnPost(UUID postId, VoteType type, Claims claims) {
        var post = postRepo.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // só fórum tem upvote/downvote em posts
        if (post.getType() != PostType.FORUM) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Votos em posts só são permitidos no fórum");
        }

        var user = userRepo.findByDiscordId(claims.getSubject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        var existing = voteRepo.findByUserIdAndPostId(user.getId(), postId);

        if (existing.isPresent()) {
            var vote = existing.get();
            if (vote.getType() == type) {
                // mesmo voto → desfaz (toggle)
                voteRepo.delete(vote);
            } else {
                // voto diferente → troca
                vote.setType(type);
                voteRepo.save(vote);
            }
        } else {
            var vote = new Vote();
            vote.setUser(user);
            vote.setPost(post);
            vote.setType(type);
            voteRepo.save(vote);
        }

        return buildPostVoteResponse(postId, claims);
    }

    @Transactional
    public VoteResponse voteOnComment(UUID commentId, VoteType type, Claims claims) {
        var comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // dúvidas: só upvote nas respostas
        if (comment.getPost().getType() == PostType.QUESTION && type == VoteType.DOWNVOTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Respostas de dúvidas só aceitam upvote");
        }

        var user = userRepo.findByDiscordId(claims.getSubject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        var existing = voteRepo.findByUserIdAndCommentId(user.getId(), commentId);

        if (existing.isPresent()) {
            var vote = existing.get();
            if (vote.getType() == type) {
                voteRepo.delete(vote);
            } else {
                vote.setType(type);
                voteRepo.save(vote);
            }
        } else {
            var vote = new Vote();
            vote.setUser(user);
            vote.setComment(comment);
            vote.setType(type);
            voteRepo.save(vote);
        }

        return buildCommentVoteResponse(commentId, claims);
    }

    public VoteResponse buildPostVoteResponse(UUID postId, Claims claims) {
        var upvotes   = voteRepo.countByPostIdAndType(postId, VoteType.UPVOTE);
        var downvotes = voteRepo.countByPostIdAndType(postId, VoteType.DOWNVOTE);

        String userVote = null;
        if (claims != null) {
            var user = userRepo.findByDiscordId(claims.getSubject()).orElse(null);
            if (user != null) {
                userVote = voteRepo.findByUserIdAndPostId(user.getId(), postId)
                        .map(v -> v.getType().name())
                        .orElse(null);
            }
        }

        return new VoteResponse(upvotes, downvotes, userVote);
    }

    public VoteResponse buildCommentVoteResponse(UUID commentId, Claims claims) {
        var upvotes   = voteRepo.countByCommentIdAndType(commentId, VoteType.UPVOTE);
        var downvotes = voteRepo.countByCommentIdAndType(commentId, VoteType.DOWNVOTE);

        String userVote = null;
        if (claims != null) {
            var user = userRepo.findByDiscordId(claims.getSubject()).orElse(null);
            if (user != null) {
                userVote = voteRepo.findByUserIdAndCommentId(user.getId(), commentId)
                        .map(v -> v.getType().name())
                        .orElse(null);
            }
        }

        return new VoteResponse(upvotes, downvotes, userVote);
    }
}