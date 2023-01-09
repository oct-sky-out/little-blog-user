package com.octskyout.users.oauth.github.dto;

import org.springframework.data.web.JsonPath;

public record GithubUserAccessTokenDto(
    @JsonPath("access_token")
    String accessToken,
    String scope,
    @JsonPath("token_type")
    String tokenType) {
}
