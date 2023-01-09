package com.octskyout.users.token.controller;

import static org.mockito.BDDMockito.given;

import com.octskyout.users.token.JWTUtil;
import com.octskyout.users.token.JwtDecoded;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TokenVerifyController.class)
class TokenVerifyControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockBean
    private JWTUtil jwtUtil;

    @Test
    void 토큰_검증시도_후_정상적인_토큰임을_반환한다() throws Exception {
        String token =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJvY3Qtc2t5LW91dCIsImdpdGh1YlByb2ZpbGUiOiJFc0YwT3ZLaXh1Sk50Wk9uaWxzUUJML1BTMS9rSGpGSmVFTExzbTREak40PSIsImV4cCI6MzMyMzAwNzk1MjQsImlhdCI6MTY3MzE3MDcyNCwiZW1haWwiOm51bGwsInVzZXJuYW1lIjoiT1VxQ0RJTVNwRE1LRUdKYlZ6MDNPZz09In0.G_7-rkD3_991FrI9GoU3hnIarMCb-Dx_946awdctAF4";
        JwtDecoded.Header header = new JwtDecoded.Header("jwt", "HS256");
        JwtDecoded.Payload payload = new JwtDecoded.Payload(
            "oct-sky-out",
            "http://github.com/example",
            123L,
            123L,
            null,
            "username"
        );

        given(jwtUtil.verifyToken(token))
            .willReturn(new JwtDecoded(header, payload));

        mockMvc.perform(get("/api/token/verify")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(header().string("Content-Type", MediaTypes.HAL_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token.header.type").value(header.type()))
            .andExpect(jsonPath("$.token.header.algorithm").value(header.algorithm()))
            .andExpect(jsonPath("$.token.payload.issuer").value(payload.issuer()))
            .andExpect(jsonPath("$.token.payload.githubProfile").value(payload.githubProfile()))
            .andExpect(jsonPath("$.token.payload.expireTime").value(payload.expireTime()))
            .andExpect(jsonPath("$.token.payload.issueTime").value(payload.issueTime()))
            .andExpect(jsonPath("$.token.payload.email").value(payload.email()))
            .andExpect(jsonPath("$.token.payload.username").value(payload.username()))
            .andExpect(jsonPath("$._links.self.href").value("http://localhost/api/token/verify"));

        then(jwtUtil)
            .should(times(1))
            .verifyToken(token);
    }

    @Test
    void 토큰이_만료되어_비정상적인_토큰임을_응답한다() {

    }

    @Test
    void 토큰_재발급_시도로_요청할_경우_refreshToken이_유효한지_검사한다() {
    }

    @Test
    void refreshToken조차_만료되었을_경우_github_재로그인_시도를_수행한다() {
    }
}
