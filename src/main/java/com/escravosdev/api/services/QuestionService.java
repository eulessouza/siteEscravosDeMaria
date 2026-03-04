package com.escravosdev.api.services;

import com.escravosdev.api.dtos.CreateQuestionRequest;
import com.escravosdev.api.dtos.PostResponse;
import com.escravosdev.api.entities.Post;
import com.escravosdev.api.entities.PostImage;
import com.escravosdev.api.entities.PostStatus;
import com.escravosdev.api.entities.PostType;
import com.escravosdev.api.repo.CategoryRepo;
import com.escravosdev.api.repo.PostRepo;
import com.escravosdev.api.repo.TagRepo;
import com.escravosdev.api.repo.UserRepo;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final PostRepo postRepo;
    private final UserRepo userRepo;
    private final CategoryRepo categoryRepo;
    private final TagRepo tagRepo;

    @Transactional
    public PostResponse create(CreateQuestionRequest req, Claims claims) {
        var author = userRepo.findByDiscordId(claims.getSubject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (author.isMuted() || author.isBanned()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você está impedido de postar");
        }

        var post = new Post();
        post.setAuthor(author);
        post.setType(PostType.QUESTION);
        post.setStatus(PostStatus.PENDING_APPROVAL);
        post.setTitle(req.title());
        post.setContent(req.content());

        if (req.categorySlug() != null) {
            var category = categoryRepo.findBySlug(req.categorySlug())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
            post.setCategory(category);
        }

        if (req.tagSlugs() != null && !req.tagSlugs().isEmpty()) {
            var tags = tagRepo.findBySlugIn(req.tagSlugs());
            post.setTags(tags);
        }

        return PostResponse.from(postRepo.save(post));
    }

    public Page<PostResponse> list(Pageable pageable) {
        return postRepo.findByTypeAndStatus(PostType.QUESTION, PostStatus.PUBLISHED, pageable)
                .map(PostResponse::from);
    }

    public PostResponse getById(UUID id) {
        var post = postRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (post.getType() != PostType.QUESTION || post.getStatus() != PostStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return PostResponse.from(post);
    }
}
