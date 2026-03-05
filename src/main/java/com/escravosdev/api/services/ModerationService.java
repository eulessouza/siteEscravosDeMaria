package com.escravosdev.api.services;

import com.escravosdev.api.dtos.response.PostResponse;
import com.escravosdev.api.entities.enums.PostStatus;
import com.escravosdev.api.entities.enums.PostType;
import com.escravosdev.api.repo.PostRepo;
import com.escravosdev.api.repo.UserRepo;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ModerationService {

    private final PostRepo postRepo;
    private final UserRepo userRepo;


    public List<PostResponse> listPending() {
        return postRepo.findByStatusFetched(PostStatus.PENDING_APPROVAL)
                .stream().map(PostResponse::from).toList();
    }

    @Transactional
    public PostResponse approve(UUID id, Claims claims) {
        var post = postRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (post.getStatus() != PostStatus.PENDING_APPROVAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post não está pendente");
        }

        var moderator = userRepo.findByDiscordId(claims.getSubject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        post.setStatus(PostStatus.PUBLISHED);
        post.setApprovedBy(moderator);
        post.setApprovedAt(Instant.now());

        return PostResponse.from(postRepo.save(post));
    }

    @Transactional
    public PostResponse reject(UUID id, Claims claims) {
        var post = postRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        post.setStatus(PostStatus.REJECTED);
        return PostResponse.from(postRepo.save(post));
    }

    @Transactional(readOnly = true)
    public List<PostResponse> listRejected() {
        return postRepo.findByStatusInFetched(
                List.of(PostStatus.REJECTED)
        ).stream().map(PostResponse::from).toList();
    }

    @Transactional
    public PostResponse archive(UUID id, Claims claims) {
        var post = postRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        post.setStatus(PostStatus.ARCHIVED);
        return PostResponse.from(postRepo.save(post));
    }

    @Transactional(readOnly = true)
    public List<PostResponse> listArchived() {
        return postRepo.findByStatusInFetched(
                List.of(PostStatus.ARCHIVED)
        ).stream().map(PostResponse::from).toList();
    }

    @Transactional
    public PostResponse close(UUID id, Claims claims) {
        var post = postRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (post.getType() != PostType.QUESTION) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Só dúvidas podem ser fechadas");
        }

        var moderator = userRepo.findByDiscordId(claims.getSubject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        post.setStatus(PostStatus.CLOSED);
        post.setClosedBy(moderator);
        post.setClosedAt(Instant.now());

        return PostResponse.from(postRepo.save(post));
    }

    @Transactional(readOnly = true)
    public List<PostResponse> listClosed() {
        return postRepo.findByStatusInFetched(
                List.of(PostStatus.CLOSED)
        ).stream().map(PostResponse::from).toList();
    }
}