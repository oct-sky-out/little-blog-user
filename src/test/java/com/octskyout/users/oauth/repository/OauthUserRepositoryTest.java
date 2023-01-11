package com.octskyout.users.oauth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.octskyout.users.oauth.entity.OauthUser;
import com.octskyout.users.oauth.github.dto.GithubUserDto;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    @Test
    void 마지막_로그인이_1년전인_계정들을_찾는쿼리_성공() {
        Pageable pageable = Pageable.ofSize(10);
        LocalDateTime oneYearAgoLdt = LocalDateTime.now().minusYears(1L);
        Page<OauthUser> oneYearsAgoLoggedUsers =
            oauthUserRepository.findLastLoginAtOneYearsAgoUsers(oneYearAgoLdt, pageable);

        assertThat(oneYearsAgoLoggedUsers.getTotalPages())
            .isEqualTo(1);
        assertThat(oneYearsAgoLoggedUsers.hasContent()).isTrue();
    }

    @Test
    void 마지막_로그인이_1년전인_계정들의_2번페이지는_존재하지않으므로_실패() {
        Pageable pageable = PageRequest.of(2, 10);

        LocalDateTime oneYearAgoLdt = LocalDateTime.now().minusYears(1L);
        Page<OauthUser> oneYearsAgoLoggedUsers =
            oauthUserRepository.findLastLoginAtOneYearsAgoUsers(oneYearAgoLdt, pageable);

        assertThat(oneYearsAgoLoggedUsers.getNumber())
            .isEqualTo(2);
        assertThat(oneYearsAgoLoggedUsers.hasContent()).isFalse();
    }

    @Test
    void 마지막_로그인이_3년전인_계정은_없으므로_순열의_크기는_0이어야한다() {
        Pageable pageable = Pageable.ofSize(10);
        LocalDateTime threeYearsAgo = LocalDateTime.now().minusYears(3L);
        Page<OauthUser> threeYearsAgoLoggedUsers =
            oauthUserRepository.findLastLoginAtOneYearsAgoUsers(threeYearsAgo, pageable);

        assertThat(threeYearsAgoLoggedUsers.getTotalPages())
            .isEqualTo(0);
        assertThat(threeYearsAgoLoggedUsers.hasContent()).isFalse();
    }
}
