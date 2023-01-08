package com.octskyout.users.oauth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.octskyout.users.oauth.entity.OauthUser;
import com.octskyout.users.oauth.github.dto.GithubUserDto;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class OauthUserRepositoryTest {
    @Autowired
    private OauthUserRepository oauthUserRepository;

    private String username = "user";
    private String email = "email";

    @BeforeEach
    void setUp() {
        GithubUserDto githubUserDto =
            new GithubUserDto(username, 123L, "", "", email);
        oauthUserRepository.save(OauthUser.createGithubOauthUser(githubUserDto));
    }

    @Test
    void username으로_사용자_탐색_성공() {
        Optional<OauthUser> user = oauthUserRepository.findOauthUserByUsername(username);

        assertThat(user).isPresent();

        OauthUser oauthUser = user.get();
        assertThat(oauthUser.getUsername()).isNotNull().isEqualTo(username);
        assertThat(oauthUser.getEmail()).isNotNull().isEqualTo(email);
        assertThat(oauthUser.getSignUpDate()).isNotNull();
        assertThat(oauthUser.getLastLoginDateTime()).isNotNull();
        assertThat(oauthUser.getGithubId()).isNotNull().isEqualTo(123L);
    }

    @Test
    void username으로_사용자_탐색_실패() {
        Optional<OauthUser> user = oauthUserRepository.findOauthUserByUsername("empty");
        assertThat(user).isEmpty();
    }
}
