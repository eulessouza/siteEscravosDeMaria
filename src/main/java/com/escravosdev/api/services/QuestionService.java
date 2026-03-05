package com.escravosdev.api.services;

import com.escravosdev.api.dtos.request.CreateQuestionRequest;
import com.escravosdev.api.dtos.response.PostResponse;
import com.escravosdev.api.entities.Post;
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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final PostRepo postRepo;
    private final UserRepo userRepo;
    private final CategoryRepo categoryRepo;
    private final TagRepo tagRepo;

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public List<PostResponse> list(Pageable pageable) {
        return postRepo.findByTypeAndStatusInFetched(PostType.QUESTION, List.of(PostStatus.PUBLISHED, PostStatus.CLOSED))
                .stream().map(PostResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PostResponse getById(UUID id) {
        var post = postRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (post.getType() != PostType.QUESTION ||
                (post.getStatus() != PostStatus.PUBLISHED && post.getStatus() != PostStatus.CLOSED)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return PostResponse.from(post);
    }
}
