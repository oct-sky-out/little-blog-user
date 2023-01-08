package com.octskyout.users.oauth.github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.octskyout.users.oauth.interfaces.OauthUser;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GithubUserDto implements OauthUser {
    @JsonProperty("login")
    private String username;
    private String id;
    @JsonProperty("avatar_url")
    private String avatarUrl;
    @JsonProperty("html_url")
    private String htmlUrl;
    private String email;
}
