package com.escravosdev.api.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "posts")
@Getter
@Setter
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;
    private String content;
    private String authorId;
    private String authorName;

}
