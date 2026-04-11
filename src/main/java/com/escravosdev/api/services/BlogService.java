package com.escravosdev.api.services;

import com.escravosdev.api.dtos.request.CreateBlogPostRequest;
import com.escravosdev.api.dtos.request.UpdateBlogPostRequest;
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
import org.hibernate.sql.Update;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final PostRepo postRepo;
    private final UserRepo userRepo;
    private final CategoryRepo categoryRepo;
    private final TagRepo tagRepo;

    @Transactional
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
    public List<PostResponse> list() {
        return postRepo.findByTypeAndStatusFetched(PostType.BLOG, PostStatus.PUBLISHED)
                .stream().map(PostResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getRecentPublished(int limit) {
        if (limit <= 0) limit = 6;

        var pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());

        return postRepo.findRecentPublished(PostType.BLOG, PostStatus.PUBLISHED, pageable)
                .stream()
                .map(PostResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PostResponse getById(UUID id, Claims claims) {
        var post = postRepo.findByIdFetched(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (post.getType() != PostType.BLOG || post.getStatus() != PostStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return PostResponse.from(post);
    }

    @Transactional
    public PostResponse update(UUID id, UpdateBlogPostRequest req, Claims claims) {
        var post = postRepo.findByIdFetched(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (post.getType() != PostType.BLOG) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        var author = userRepo.findByDiscordId(claims.getSubject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!post.getAuthor().getId().equals(author.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Só o autor pode editar este post");
        }

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
        } else {
            post.setCategory(null);
        }

        if (req.tagSlugs() != null) {
            post.setTags(tagRepo.findBySlugIn(req.tagSlugs()));
        }

        if (req.imageUrls() != null) {
            post.getImages().clear();
            for (int i = 0; i < req.imageUrls().size(); i++) {
                var img = new PostImage();
                img.setPost(post);
                img.setUrl(req.imageUrls().get(i));
                img.setPosition(i);
                post.getImages().add(img);
            }
        }

        return PostResponse.from(postRepo.save(post));
    }

    @Transactional
    public void delete(UUID id, Claims claims) {
        var post = postRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (post.getType() != PostType.BLOG) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        postRepo.delete(post);
    }
}
