package com.octskyout.users.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.octskyout.users.aes.Aes256;
import com.octskyout.users.oauth.github.dto.GithubUserDto;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class JWTUtilTest {
    String alg = "AES/CBC/PKCS5Padding";
    String key = "9d6bf7b53696697eb36e1b05f25fad18";
    String iv = key.substring(0,16);

    Aes256 aes256 = new Aes256(alg, key, iv);
    private final JWTUtil jwtUtil = new JWTUtil("example", aes256);
    @Test
    void 토큰을_생성한다() {
        String username = "username";
        Long id = 123L;
        String avatarUrl = "http://github.com/username/avatar";
        String htmlUrl = "http://github.com/username";
        String email = null;
        GithubUserDto githubUserDto = new GithubUserDto(username, id, avatarUrl, htmlUrl, email);

        String accessToken = jwtUtil.createToken(githubUserDto, TokenType.ACCESS);
        assertThat(accessToken).isInstanceOf(String.class);
        log.debug(accessToken);
    }

    @Test
    void 이전에_생성되었던_토큰을_검사한다_성공() {
        String accessToken =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJvY3Qtc2t5LW91dCIsImdpdGh1YlByb2ZpbGUiOiJFc0YwT3ZLaXh1Sk50Wk9uaWxzUUJML1BTMS9rSGpGSmVFTExzbTREak40PSIsImV4cCI6MzMyMzAwNzk1MjQsImlhdCI6MTY3MzE3MDcyNCwiZW1haWwiOm51bGwsInVzZXJuYW1lIjoiT1VxQ0RJTVNwRE1LRUdKYlZ6MDNPZz09In0.G_7-rkD3_991FrI9GoU3hnIarMCb-Dx_946awdctAF4";

        JwtDecoded decodedJWTClaims = jwtUtil.verifyToken(accessToken);

        String username = "username";
        String htmlUrl = "http://github.com/username";

        String issuer = decodedJWTClaims.payload().issuer();
        Long expireTimeAsLong = decodedJWTClaims.payload().expireTime();
        String decryptedUsername = decodedJWTClaims.payload().username();
        String decryptedGithubHtml = decodedJWTClaims.payload().githubProfile();
        String decryptedEmail = decodedJWTClaims.payload().email();

        assertThat(issuer).isEqualTo("oct-sky-out");
        assertThat(expireTimeAsLong).isGreaterThan(Instant.now().getEpochSecond());
        assertThat(decryptedUsername).isEqualTo(username);
        assertThat(decryptedGithubHtml).isEqualTo(htmlUrl);
        assertThat(decryptedEmail).isNull();
    }

    @Test
    void 이전에_생성되었던_토큰을_검사한다_실패() {
        String expiredToken =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJvY3Qtc2t5LW91dCIsImdpdGh1YlByb2ZpbGUiOiJFc0YwT3ZLaXh1Sk50Wk9uaWxzUUJML1BTMS9rSGpGSmVFTExzbTREak40PSIsImV4cCI6MTY3Mjk5MDIxNywiaWF0IjoxNjcyOTkwMjE3LCJ1c2VybmFtZSI6Ik9VcUNESU1TcERNS0VHSmJWejAzT2c9PSJ9.BzRkecNt1S0NpabOwnu56Z-4l6PGFSA-UrSSA8iLvoM";

        assertThatThrownBy(() -> jwtUtil.verifyToken(expiredToken))
            .isInstanceOf(TokenExpiredException.class)
            .hasMessage("이미 만료된 토큰입니다.");
    }

    @Test
    void 이전에_생성된_클레임_혹은_서명이_잘못된_토큰_검사에_예외를_발생시킨다() {
        String diffClaim =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJvY3Qtc2t5IiwiZ2l0aHViUHJvZmlsZSI6IkVzRjBPdktpeHVKTnRaT25pbHNRQkwvUFMxL2tIakZKZUVMTHNtNERqTjQ9IiwiZXhwIjoxNjcyOTkxNDgyLCJpYXQiOjE2NzI5OTA1ODIsInVzZXJuYW1lIjoiT1VxQ0RJTVNwRE1LRUdKYlZ6MDNPZz09In0.c68wOaihcTcTa_qUBTCJWtyegCPqQwR-kl__C0Mi108";

        assertThatThrownBy(() -> jwtUtil.verifyToken(diffClaim))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("토큰 검증에 문제가 발생했습니다.");
    }
}
