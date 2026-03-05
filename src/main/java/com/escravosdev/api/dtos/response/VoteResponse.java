package com.escravosdev.api.dtos.response;

public record VoteResponse(
        long upvotes,
        long downvotes,
        String userVote  // "UPVOTE", "DOWNVOTE" ou null
) {}