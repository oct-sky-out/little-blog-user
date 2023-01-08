package com.octskyout.users.oauth.github.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.octskyout.users.oauth.github.adapter.GithubOauthAdapter;
import com.octskyout.users.oauth.github.dto.GithubLoginSuccessResponseDto;
import com.octskyout.users.oauth.github.dto.GithubUserDto;
import com.octskyout.users.oauth.github.service.GithubOauthService;
import com.octskyout.users.token.AccessRefreshTokens;
import com.octskyout.users.token.JWTUtil;
import com.octskyout.users.token.TokenType;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login/oauth2/github")
@RequiredArgsConstructor
public class GithubOAuthController {
    private final GithubOauthAdapter githubOauthAdapter;
    private final GithubOauthService githubOauthService;
    private final JWTUtil jwtUtil;

    @GetMapping
    public EntityModel<Map<String, String>> trySignInAtGithubOauth() {
        String loginPageUrl =
            githubOauthAdapter.requestGithubOauthSignIn(UUID.randomUUID().toString());
        EntityModel<Map<String, String>> selfRel =
            methodOn(GithubOAuthController.class).trySignInAtGithubOauth();

        return EntityModel.of(
            Map.of("result", "OK"),
            linkTo(selfRel).withSelfRel(),
            Link.of(loginPageUrl).withRel("login")
        );
    }

    @GetMapping("/callback")
    public EntityModel<GithubLoginSuccessResponseDto> successLogin(@RequestParam String code,
                                                   @RequestParam String state) {
        GithubUserDto user = githubOauthAdapter.processOAuthLogin(code, state);
        githubOauthService.doSignIn(user);

        String access = jwtUtil.createToken(user, TokenType.ACCESS);
        String refresh = jwtUtil.createToken(user, TokenType.REFRESH);
        AccessRefreshTokens tokens = new AccessRefreshTokens(access, refresh);
        GithubLoginSuccessResponseDto resource = new GithubLoginSuccessResponseDto(user, tokens);

        var selfRel = methodOn(GithubOAuthController.class).successLogin(code, state);

        return EntityModel.of(resource,
            linkTo(selfRel).withSelfRel(),
            Link.of(user.getHtmlUrl()).withRel("githubHtml"));
    }
}
