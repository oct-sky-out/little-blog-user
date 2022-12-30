package com.octskyout.users.oauth.github.dto;

public record GithubAccessTokenArgsDto(String clientId, String clientSecret, String code) {
}
