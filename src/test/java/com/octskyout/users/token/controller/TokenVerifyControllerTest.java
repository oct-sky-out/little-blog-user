package com.octskyout.users.token.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.octskyout.users.token.JWTUtil;
import com.octskyout.users.token.JwtDecoded;
import com.octskyout.users.token.TokenType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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
    void 액세스_토큰이_만료되어_비정상적인_토큰임을_응답한다() throws Exception {
        String expiredToken =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJvY3Qtc2t5LW91dCIsImdpdGh1YlByb2ZpbGUiOiJFc0YwT3ZLaXh1Sk50Wk9uaWxzUUJML1BTMS9rSGpGSmVFTExzbTREak40PSIsImV4cCI6MTY3Mjk5MDIxNywiaWF0IjoxNjcyOTkwMjE3LCJ1c2VybmFtZSI6Ik9VcUNESU1TcERNS0VHSmJWejAzT2c9PSJ9.BzRkecNt1S0NpabOwnu56Z-4l6PGFSA-UrSSA8iLvoM";

        given(jwtUtil.verifyToken(expiredToken))
            .willThrow(TokenExpiredException.class);

        mockMvc.perform(get("/api/token/verify")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().exists(HttpHeaders.LOCATION))
            .andDo(print());
    }

    @Test
    void 토큰_재발급_리다이렉션_요청에_응답한다() throws Exception {
        mockMvc.perform(get("/api/token/reissue"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._links.self.href").value("http://localhost/api/token/reissue"))
            .andExpect(jsonPath("$.require").value("refreshToken"))
            .andDo(print());
    }

    @Test
    void 토큰_재발급_시도로_요청할_경우_refreshToken이_유효한지_검사한다() throws Exception {
        String refreshToken =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJvY3Qtc2t5LW91dCIsImdpdGh1YlByb2ZpbGUiOiJFc0YwT3ZLaXh1Sk50Wk9uaWxzUUJML1BTMS9rSGpGSmVFTExzbTREak40PSIsImV4cCI6MzMyMzAwNzk1MjQsImlhdCI6MTY3MzE3MDcyNCwiZW1haWwiOm51bGwsInVzZXJuYW1lIjoiT1VxQ0RJTVNwRE1LRUdKYlZ6MDNPZz09In0.G_7-rkD3_991FrI9GoU3hnIarMCb-Dx_946awdctAF4";
        String accessToken = "accessToken";
        String newRefreshToken = "refreshToken";
        JwtDecoded.Header header = new JwtDecoded.Header("jwt", "HS256");
        JwtDecoded.Payload payload = new JwtDecoded.Payload(
            "oct-sky-out",
            "http://github.com/example",
            123L,
            123L,
            null,
            "username"
        );

        given(jwtUtil.verifyToken(refreshToken))
            .willReturn(new JwtDecoded(header, payload));
        given(jwtUtil.createToken(any(), eq(TokenType.ACCESS)))
            .willReturn(accessToken);
        given(jwtUtil.createToken(any(), eq(TokenType.REFRESH)))
            .willReturn(newRefreshToken);

        mockMvc.perform(post("/api/token/reissue")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken))
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token.accessToken").exists())
            .andExpect(jsonPath("$.token.refreshToken").exists())
            .andExpect(jsonPath("$._links.self.href").value("http://localhost/api/token/reissue"));

        then(jwtUtil)
            .should(times(1))
            .verifyToken(refreshToken);
        then(jwtUtil)
            .should(times(1))
            .createToken(any(), eq(TokenType.ACCESS));
        then(jwtUtil)
            .should(times(1))
            .createToken(any(), eq(TokenType.REFRESH));
    }

    @Test
    void refreshToken조차_만료되었을_경우_예외를_발생시킨_후_로그인_href를_전송한다() throws Exception {
        String expiredRefreshToken =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJvY3Qtc2t5LW91dCIsImdpdGh1YlByb2ZpbGUiOiJFc0YwT3ZLaXh1Sk50Wk9uaWxzUUJML1BTMS9rSGpGSmVFTExzbTREak40PSIsImV4cCI6MTY3Mjk5MDIxNywiaWF0IjoxNjcyOTkwMjE3LCJ1c2VybmFtZSI6Ik9VcUNESU1TcERNS0VHSmJWejAzT2c9PSJ9.BzRkecNt1S0NpabOwnu56Z-4l6PGFSA-UrSSA8iLvoM";

        given(jwtUtil.verifyToken(expiredRefreshToken))
            .willThrow(TokenExpiredException.class);

        assertThatThrownBy(() -> mockMvc.perform(post("/api/token/reissue")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredRefreshToken))
            .andReturn())
            .hasMessageContaining("로그인 세션이 만료되었습니다. 처음부터 로그인해주세요.");

        then(jwtUtil)
            .should(times(1))
            .verifyToken(expiredRefreshToken);
    }
}
