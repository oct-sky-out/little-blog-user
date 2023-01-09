package com.octskyout.users.oauth.github.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.octskyout.users.oauth.dummy.OauthUserDummy;
import com.octskyout.users.oauth.entity.OauthUser;
import com.octskyout.users.oauth.github.dto.GithubUserDto;
import com.octskyout.users.oauth.repository.OauthUserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import(GithubOauthService.class)
class GithubOauthServiceTest {
    @Autowired
    GithubOauthService githubOauthService;

    @MockBean
    OauthUserRepository oauthUserRepository;

    @Test
    void 이미_존재하는_회원이_로그인시_로그인_기록을_남기고_로그인성공한다() {
        GithubUserDto githubUser = new GithubUserDto(
            "example",
            123L,
            "http://github.com/example/avatar",
            "http://github.com/example",
            null);
        Optional<OauthUser> oauthUser = OauthUserDummy.githubUserDummy();
        LocalDateTime beforeLoginDt = oauthUser.get().getLastLoginDateTime();

        given(oauthUserRepository.findOauthUserByUsername(githubUser.getUsername()))
            .willReturn(oauthUser);

        boolean isAdmin = githubOauthService.doSignIn(githubUser);
        assertThat(isAdmin).isFalse();

        then(oauthUserRepository)
            .should(times(1))
            .findOauthUserByUsername(githubUser.getUsername());

        assertThat(oauthUser.get().getLastLoginDateTime())
            .isNotEqualTo(beforeLoginDt);
    }

    @Test
    void 새로운_회원이_로그인시_회원가입을_진행하고_로그인시킨다() {
        GithubUserDto githubUser = new GithubUserDto(
            "example",
            123L,
            "http://github.com/example/avatar",
            "http://github.com/example",
            null);

        given(oauthUserRepository.findOauthUserByUsername(githubUser.getUsername()))
            .willReturn(Optional.empty());

        boolean isAdmin = githubOauthService.doSignIn(githubUser);
        assertThat(isAdmin).isFalse();

        then(oauthUserRepository)
            .should(times(1))
            .findOauthUserByUsername(githubUser.getUsername());

        then(oauthUserRepository)
            .should(times(1))
            .save(any());
    }

    @Test
    void 데이터베이스에서_관리자를_찾을_수_있다() {
        GithubUserDto githubUser = new GithubUserDto(
            "admin",
            123L,
            "http://github.com/example/avatar",
            "http://github.com/example",
            null);

        Optional<OauthUser> oauthAdminUser = OauthUserDummy.githubAdminUserDummy();
        LocalDateTime lastLoginDt = oauthAdminUser.get().getLastLoginDateTime();

        given(oauthUserRepository.findOauthUserByUsername(githubUser.getUsername()))
            .willReturn(oauthAdminUser);

        boolean isAdmin = githubOauthService.doSignIn(githubUser);
        assertThat(isAdmin).isFalse();

        then(oauthUserRepository)
            .should(times(1))
            .findOauthUserByUsername(githubUser.getUsername());
        then(oauthUserRepository)
            .should(never())
            .save(any());
        assertThat(oauthAdminUser.get().getLastLoginDateTime())
            .isNotEqualTo(lastLoginDt);
    }
}
