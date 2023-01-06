package com.octskyout.users.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.octskyout.users.aes.Aes256;
import com.octskyout.users.oauth.github.dto.GithubUserDto;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class JWTUtilTest {
    String alg = "AES/CBC/PKCS5Padding";
    String key = "9d6bf7b53696697eb36e1b05f25fad18";
    String iv = key.substring(0,16);

    Aes256 aes256 = new Aes256(alg, key, iv);
    private JWTUtil jwtUtil = new JWTUtil("example", aes256);
    @Test
    void 토큰을_생성한다() {
        String username = "username";
        String id = "id";
        String avatarUrl = "http://github.com/username/avatar";
        String htmlUrl = "http://github.com/username";
        GithubUserDto githubUserDto = new GithubUserDto(username, id, avatarUrl, htmlUrl);

        String accessToken = jwtUtil.createToken(githubUserDto, TokenType.ACCESS);
        assertThat(accessToken).isInstanceOf(String.class);
        log.debug(accessToken);
    }

    @Test
    void 이전에_생성되었던_토큰을_검사한다_성공() throws InvalidAlgorithmParameterException, NoSuchPaddingException,
        IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException,
        InvalidKeyException {
        String accessToken =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJvY3Qtc2t5LW91dCIsImdpdGh1YlByb2ZpbGUiOiJFc0YwT3ZLaXh1Sk50Wk9uaWxzUUJML1BTMS9rSGpGSmVFTExzbTREak40PSIsImV4cCI6NDgyODY2MzM5NSwiaWF0IjoxNjcyOTg5Nzk1LCJ1c2VybmFtZSI6Ik9VcUNESU1TcERNS0VHSmJWejAzT2c9PSJ9.rE7WuGgYOWBX9xbrOkyPPu2gU8QwhLzTRJUoDyIHB2o";

        Map<String, Claim> decodedJWTClaims = jwtUtil.verifyToken(accessToken).getClaims();

        String username = "username";
        String htmlUrl = "http://github.com/username";

        String issuer = decodedJWTClaims.get("iss").asString();
        Long expireTimeAsLong = decodedJWTClaims.get("exp").asLong();
        String encryptedUsername = decodedJWTClaims.get("username").asString();
        String encryptedGithubHtml = decodedJWTClaims.get("githubProfile").asString();
        String jwtUsername = aes256.decrypt(encryptedUsername);
        String jwtGithubHtml = aes256.decrypt(encryptedGithubHtml);

        assertThat(issuer).isEqualTo("oct-sky-out");
        assertThat(expireTimeAsLong).isGreaterThan(Instant.now().getEpochSecond());
        assertThat(jwtUsername).isEqualTo(username);
        assertThat(jwtGithubHtml).isEqualTo(htmlUrl);
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
