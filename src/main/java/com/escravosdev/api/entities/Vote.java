package com.escravosdev.api.entities;

import com.escravosdev.api.entities.enums.VoteType;
import jakarta.persistence.*;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "votes", indexes = {
        @Index(name = "idx_vote_user", columnList = "user_id"),
        @Index(name = "idx_vote_post", columnList = "post_id"),
        @Index(name = "idx_vote_comment", columnList = "comment_id")
},
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "post_id"}),
                @UniqueConstraint(columnNames = {"user_id", "comment_id"})
        })
@Getter
@Setter
@NoArgsConstructor
public class Vote {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "post_id")
        private Post post; // null se for voto em comentário

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "comment_id")
        private Comment comment; // null se for voto em post

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private VoteType type; // UPVOTE ou DOWNVOTE

        @CreationTimestamp
        private Instant createdAt;
}
