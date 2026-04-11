package com.escravosdev.api.dtos.response;

import java.util.List;
import java.util.Map;

public record HomeData(
        Object liturgyToday,
        Object prayerOfTheDay,
        Object saintToday,
        List<Map<String, Object>> lastPopes,
        List<PostResponse> recentBlogPosts,
        List<PostResponse> trendingForumPosts,
        Object lastAnsweredQuestion,
        List<Map<String, Object>> supporters,
        long discordMemberCount,
        String discordInviteLink
) {}
