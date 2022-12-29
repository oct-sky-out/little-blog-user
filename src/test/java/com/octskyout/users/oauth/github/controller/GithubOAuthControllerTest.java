package com.octskyout.users.oauth.github.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.octskyout.users.oauth.github.adapter.GithubOauthAdapter;
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

    @Test
    void trySignInAtGithubOauth() throws Exception {
        String adapterResult = "https://github.com/login?client_id=8dd94b69615b6ca2abe5&return_to=%2Flogin%2Foauth%2Fauthorize%3Fclient_id%3D8dd94b69615b6ca2abe5%26redirect_uri%3Dhttp%253A%252F%252F127.0.0.1%253A8081%252Fapi%252Flogin%252Foauth2%252Fgithub%26scope%3Duser%26state%3Dd2170c64-4c90-47fd-9f6c-d028e6af1756";
        given(githubOauthAdapter.requestGithubOauthSignIn())
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
            .requestGithubOauthSignIn();
    }
}
