package com.octskyout.users.oauth.dummy;

import com.octskyout.users.oauth.entity.OauthUser;
import com.octskyout.users.oauth.github.dto.GithubUserDto;
import java.util.Optional;

public class OauthUserDummy extends OauthUser {
    public static Optional<OauthUser> githubUserDummy() {
        GithubUserDto githubUser = new GithubUserDto(
            "example",
            123L,
            "http://github.com/example/avatar",
            "http://github.com/example",
            null);

        return Optional.of(OauthUser.createGithubOauthUser(githubUser));
    }
}
