package com.octskyout.users.oauth.github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.octskyout.users.oauth.interfaces.OauthUser;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GithubUserDto implements OauthUser {
    @NotEmpty
    private String username;

    @NotNull
    private Long id;

    @NotNull
    private String avatarUrl;

    @NotNull
    private String htmlUrl;
    private String email;

    public String getId() {
        return id.toString();
    }

    @JsonProperty("login")
    public void setUsername(String username) {
        this.username = username;
    }

    @JsonProperty("avatar_url")
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @JsonProperty("html_url")
    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    @JsonProperty("avatarUrl")
    public String getAvatarUrl() {
        return avatarUrl;
    }

    @JsonProperty("htmlUrl")
    public String getHtmlUrl() {
        return htmlUrl;
    }
}
