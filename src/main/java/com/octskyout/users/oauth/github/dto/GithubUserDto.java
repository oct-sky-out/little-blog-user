package com.octskyout.users.oauth.github.dto;

import org.springframework.data.web.JsonPath;

public record GithubUserDto(
    @JsonPath("login")
    String username,
    String id,
    @JsonPath("avatar_url")
    String avatarUrl,
    @JsonPath("html_url")
    String htmlUrl) {
}
