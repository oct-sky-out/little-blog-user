package com.octskyout.users.oauth.github.dto;


import com.octskyout.users.token.AccessRefreshTokens;

public record GithubLoginSuccessResponseDto(GithubUserDto user, AccessRefreshTokens token){
}
