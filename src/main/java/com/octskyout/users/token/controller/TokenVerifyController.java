package com.octskyout.users.token.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.octskyout.users.oauth.github.dto.GithubUserDto;
import com.octskyout.users.token.AccessRefreshTokens;
import com.octskyout.users.token.JWTUtil;
import com.octskyout.users.token.JwtDecoded;
import com.octskyout.users.token.TokenType;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class TokenVerifyController {
    private final JWTUtil jwtUtil;

    @SneakyThrows
    private static <T> EntityModel<Map<String, T>> wrapTokenExpiredException(
        Callable<EntityModel<Map<String, T>>> callable, Callable<Void> exceptAfterDo) {
        try {
            return callable.call();
        } catch (TokenExpiredException e) {
            log.error("TokenVerifyController - 토큰 만료에러");
            Stream.of(e.getStackTrace())
                .forEach(stackTraceElement -> log.error(
                "TokenVerifyController - " + "line " + stackTraceElement.getLineNumber() +
                    "message : " + stackTraceElement.getModuleName() +
                    stackTraceElement.getClassName() + stackTraceElement.getMethodName()));

            if (Objects.nonNull(exceptAfterDo)) {
                exceptAfterDo.call();
            }
        }

        return null;
    }

    @GetMapping(value = "/verify", headers = HttpHeaders.AUTHORIZATION)
    public EntityModel<Map<String, JwtDecoded>> verifyJwtToken(
        @Valid @NotEmpty @RequestHeader(HttpHeaders.AUTHORIZATION) String jwtBearerToken,
        HttpServletResponse response) {
        String jwtToken = getJwtToken(jwtBearerToken);
        EntityModel<Map<String, JwtDecoded>> selfRel =
            methodOn(TokenVerifyController.class).verifyJwtToken(jwtBearerToken, response);
        return wrapTokenExpiredException(() -> {
            JwtDecoded decodedJWT = jwtUtil.verifyToken(jwtToken);

            return EntityModel.of(Map.of("token", decodedJWT), linkTo(selfRel).withSelfRel());
        }, () -> {
            response.sendRedirect("/api/token/reissue");
            return null;
        });
    }

    @GetMapping("/reissue")
    public EntityModel<Map<String, String>> guideReissueToken() {
        EntityModel<Map<String, String>> selfRel =
            methodOn(TokenVerifyController.class).guideReissueToken();
        EntityModel<Map<String, AccessRefreshTokens>> reissue =
            methodOn(TokenVerifyController.class).reissueToken(null);

        return EntityModel.of(Map.of("require", "refreshToken"), linkTo(selfRel).withSelfRel(),
            linkTo(reissue).withRel("reissue"));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/reissue", headers = HttpHeaders.AUTHORIZATION)
    public EntityModel<Map<String, AccessRefreshTokens>> reissueToken(
        @Valid @NotEmpty @RequestHeader(HttpHeaders.AUTHORIZATION) String jwtRefreshBearerToken){
        String refreshToken = getJwtToken(jwtRefreshBearerToken);
        EntityModel<Map<String, AccessRefreshTokens>> selfRel =
            methodOn(TokenVerifyController.class).reissueToken(jwtRefreshBearerToken);
        return wrapTokenExpiredException(() -> {
            JwtDecoded decodedRefreshToken = jwtUtil.verifyToken(refreshToken);
            GithubUserDto githubUserDto =
                new GithubUserDto(decodedRefreshToken.payload().username(), null, null,
                    decodedRefreshToken.payload().githubProfile(),
                    decodedRefreshToken.payload().email());

            String access = jwtUtil.createToken(githubUserDto, TokenType.ACCESS);
            String refresh = jwtUtil.createToken(githubUserDto, TokenType.REFRESH);
            return EntityModel.of(Map.of("token", new AccessRefreshTokens(access, refresh)),
                linkTo(selfRel).withSelfRel());
        }, () -> {
            throw new RuntimeException("로그인 세션이 만료되었습니다. 처음부터 로그인해주세요.");
        });
    }

    private String getJwtToken(String jwtBearerToken) {
        return jwtBearerToken.trim().split(" ")[1];
    }
}
