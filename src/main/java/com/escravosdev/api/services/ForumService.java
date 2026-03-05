package com.escravosdev.api.services;

import com.escravosdev.api.dtos.request.CreateForumPostRequest;
import com.escravosdev.api.dtos.response.PostResponse;
import com.escravosdev.api.entities.Post;
import com.escravosdev.api.entities.PostImage;
import com.escravosdev.api.entities.enums.PostStatus;
import com.escravosdev.api.entities.enums.PostType;
import com.escravosdev.api.repo.CategoryRepo;
import com.escravosdev.api.repo.PostRepo;
import com.escravosdev.api.repo.TagRepo;
import com.escravosdev.api.repo.UserRepo;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ForumService {

    private final PostRepo postRepo;
    private final UserRepo userRepo;
    private final CategoryRepo categoryRepo;
    private final TagRepo tagRepo;

    private final VoteService voteService;

    @Transactional
    public PostResponse create(CreateForumPostRequest req, Claims claims) {
        var author = userRepo.findByDiscordId(claims.getSubject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (author.isMuted() || author.isBanned()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você está impedido de postar");
        }

        var post = new Post();
        post.setAuthor(author);
        post.setType(PostType.FORUM);
        post.setStatus(PostStatus.PENDING_APPROVAL);
        post.setTitle(req.title());
        post.setContent(req.content());
        post.setCoverImageUrl(req.coverImageUrl());

        if (req.categorySlug() != null) {
            var category = categoryRepo.findBySlug(req.categorySlug())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria não encontrada"));
            post.setCategory(category);
        }

        if (req.tagSlugs() != null && !req.tagSlugs().isEmpty()) {
            post.setTags(tagRepo.findBySlugIn(req.tagSlugs()));
        }

        if (req.imageUrls() != null && !req.imageUrls().isEmpty()) {
            var images = new LinkedHashSet<PostImage>();
            for (int i = 0; i < req.imageUrls().size(); i++) {
                var img = new PostImage();
                img.setPost(post);
                img.setUrl(req.imageUrls().get(i));
                img.setPosition(i);
                images.add(img);
            }
            post.setImages(images);
        }

        return PostResponse.from(postRepo.save(post));
    }

    @Transactional(readOnly = true)
    public List<PostResponse> list(Claims claims) {
        return postRepo.findByTypeAndStatusFetched(PostType.FORUM, PostStatus.PUBLISHED)
                .stream().map(post -> {
                    var votes = voteService.buildPostVoteResponse(post.getId(), claims);
                    return PostResponse.from(post, votes);
                }).toList();
    }

    @Transactional(readOnly = true)
    public PostResponse getById(UUID id, Claims claims) {
        var post = postRepo.findByIdFetched(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (post.getType() != PostType.FORUM || post.getStatus() != PostStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        var votes = voteService.buildPostVoteResponse(id, claims);
        return PostResponse.from(post, votes);
    }
}
