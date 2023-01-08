package com.octskyout.users.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.octskyout.users.aes.Aes256;
import com.octskyout.users.oauth.github.dto.GithubUserDto;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JWTUtil {
    @Value("${secure.jwt.secret}")
    private final String hmacSecret;

    private final Aes256 aes256;

    public String createToken(GithubUserDto githubUserDto, TokenType type) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(hmacSecret);
            Instant issueAt = LocalDateTime.now().toInstant(ZoneOffset.ofHours(9));
            Instant expAt = LocalDateTime.now().plus(type.getExpMilliSeconds(), ChronoUnit.MILLIS)
                .toInstant(ZoneOffset.ofHours(9));
            String encryptUsername = aes256.encrypt(githubUserDto.getUsername());
            String encryptHtmlUrl = aes256.encrypt(githubUserDto.getHtmlUrl());
            String encryptEmail = aes256.encrypt(githubUserDto.getEmail());

            return JWT.create()
                .withIssuer("oct-sky-out")
                .withClaim("username", encryptUsername)
                .withClaim("githubProfile", encryptHtmlUrl)
                .withClaim("email", encryptEmail)
                .withIssuedAt(issueAt)
                .withExpiresAt(expAt)
                .sign(algorithm);
        } catch (JWTCreationException exception){
            throw new RuntimeException("토큰 생성에 실패했습니다.");
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException
            | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException
            | InvalidKeyException e) {
            throw new RuntimeException("개인정보 암호화에 실패했습니다.");
        }
    }

    public DecodedJWT verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(hmacSecret);
            JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer("oct-sky-out")
                .acceptLeeway(3)
                .build();

            return verifier.verify(token);
        } catch (TokenExpiredException expiredException) {
            // expired jwt token
            throw new TokenExpiredException("이미 만료된 토큰입니다.", Instant.now());
        } catch (JWTVerificationException exception){
            // Invalid signature/claims
            throw new RuntimeException("토큰 검증에 문제가 발생했습니다.");
        }
    }
}
