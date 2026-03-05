package com.escravosdev.api.services;

import com.escravosdev.api.dtos.request.CreateBlogPostRequest;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final PostRepo postRepo;
    private final UserRepo userRepo;
    private final CategoryRepo categoryRepo;
    private final TagRepo tagRepo;

    @Transactional(readOnly = true)
    public PostResponse create(CreateBlogPostRequest req, Claims claims) {
        var author = userRepo.findByDiscordId(claims.getSubject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!req.publishToSite() && !req.publishToInstagram() && !req.publishToDiscord()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione pelo menos um destino de publicação.");
        }

        var post = new Post();
        post.setAuthor(author);
        post.setType(PostType.BLOG);
        post.setStatus(PostStatus.PUBLISHED);
        post.setTitle(req.title());
        post.setContent(req.content());
        post.setCoverImageUrl(req.coverImageUrl());
        post.setPublishToSite(req.publishToSite());
        post.setPublishToDiscord(req.publishToDiscord());
        post.setPublishToInstagram(req.publishToInstagram());

        if (req.categorySlug() != null) {
            var category = categoryRepo.findBySlug(req.categorySlug())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria não encontrada"));
            post.setCategory(category);
        }

        if (req.categorySlug() != null) {
            var category = categoryRepo.findBySlug(req.categorySlug())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria não encontrada"));
            post.setCategory(category);
        }

        if (req.tagSlugs() != null && !req.tagSlugs().isEmpty()) {
            var tags = tagRepo.findBySlugIn(req.tagSlugs());
            post.setTags(tags);
        }

        if (req.imageUrls() != null) {
            var images = new HashSet<PostImage>();
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
    public List<PostResponse> list(Pageable pageable) {
        return postRepo.findByTypeAndStatus(PostType.BLOG, PostStatus.PUBLISHED, pageable)
                .stream().map(PostResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PostResponse getById(UUID id) {
        var post = postRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (post.getType() != PostType.BLOG || post.getStatus() != PostStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return PostResponse.from(post);
    }

}
