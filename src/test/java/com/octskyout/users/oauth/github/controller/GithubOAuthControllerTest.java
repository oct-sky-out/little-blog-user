package com.octskyout.users.oauth.github.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.octskyout.users.oauth.github.adapter.GithubOauthAdapter;
import com.octskyout.users.oauth.github.dto.GithubUserDto;
import com.octskyout.users.oauth.github.service.GithubOauthService;
import com.octskyout.users.token.JWTUtil;
import com.octskyout.users.token.TokenType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = GithubOAuthController.class)
class GithubOAuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GithubOauthAdapter githubOauthAdapter;
    @MockBean
    private GithubOauthService githubOauthService;
    @MockBean
    private JWTUtil jwtUtil;

    @Test
    void 사용자의_깃허브_로그인_접속시도시_요청을_받는다() throws Exception {
        String adapterResult = "https://github.com/login?client_id=8dd94b69615b6ca2abe5&return_to=%2Flogin%2Foauth%2Fauthorize%3Fclient_id%3D8dd94b69615b6ca2abe5%26redirect_uri%3Dhttp%253A%252F%252F127.0.0.1%253A8081%252Fapi%252Flogin%252Foauth2%252Fgithub%26scope%3Duser%26state%3Dd2170c64-4c90-47fd-9f6c-d028e6af1756";
        given(githubOauthAdapter.requestGithubOauthSignIn(anyString()))
            .willReturn(adapterResult);

        mockMvc.perform(get("/api/login/oauth2/github"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", MediaTypes.HAL_JSON_VALUE))
            .andExpect(jsonPath("$.result").value("OK"))
            .andExpect(jsonPath("$._links.self.href").value("http://localhost/api/login/oauth2/github"))
            .andExpect(jsonPath("$._links.login.href").value(adapterResult))
            .andDo(print());

        then(githubOauthAdapter)
            .should(times(1))
            .requestGithubOauthSignIn(anyString());
    }

    @Test
    void 깃허브로부터오는_콜백요청을_받는다() throws Exception {
        String username = "example";
        Long id = 123L;
        String avatar = "http://github.com/example/avartar";
        String html = "http://github.com/example";
        String email = null;
        GithubUserDto githubUserDto = new GithubUserDto(username, id, avatar, html, email);
        String exampleAccessToken = "example access token";
        String exampleRefreshToken = "example refresh token";

        given(githubOauthService.doSignIn(githubUserDto)).willReturn(false);
        given(githubOauthAdapter.processOAuthLogin(anyString(), anyString()))
            .willReturn(githubUserDto);
        given(jwtUtil.createToken(githubUserDto, TokenType.ACCESS))
            .willReturn(exampleAccessToken);
        given(jwtUtil.createToken(githubUserDto, TokenType.REFRESH))
            .willReturn(exampleRefreshToken);

        String requestUri = "/api/login/oauth2/github/callback?code={code}&state={state}";
        String code = "12345";
        String state = "abcd";
        mockMvc.perform(get(requestUri, code, state))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", MediaTypes.HAL_JSON_VALUE))
            .andExpect(jsonPath("$.isAdmin").value(false))
            .andExpect(jsonPath("$.user.username").value(username))
            .andExpect(jsonPath("$.user.id").value(id))
            .andExpect(jsonPath("$.user.avatarUrl").value(avatar))
            .andExpect(jsonPath("$.user.htmlUrl").value(html))
            .andExpect(jsonPath("$.token.accessToken").value(exampleAccessToken))
            .andExpect(jsonPath("$.token.refreshToken").value(exampleRefreshToken))
            .andExpect(jsonPath("$._links.self.href").value("http://localhost/api/login/oauth2/github/callback?code=12345&state=abcd"))
            .andExpect(jsonPath("$._links.githubHtml.href").value(html));

        then(githubOauthService)
            .should(times(1))
                .doSignIn(githubUserDto);
        then(githubOauthAdapter)
            .should(times(1))
            .processOAuthLogin(code, state);
        then(jwtUtil)
            .should(times(1))
            .createToken(githubUserDto, TokenType.ACCESS);
        then(jwtUtil)
            .should(times(1))
            .createToken(githubUserDto, TokenType.REFRESH);
    }
}
