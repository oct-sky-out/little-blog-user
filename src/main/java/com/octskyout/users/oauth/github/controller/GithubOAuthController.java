package com.octskyout.users.oauth.github.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.octskyout.users.oauth.github.adapter.GithubOauthAdapter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login/oauth2/github")
@RequiredArgsConstructor
public class GithubOAuthController {
    private final GithubOauthAdapter githubOauthAdapter;

    @GetMapping
    public EntityModel<Map<String, String>> trySignInAtGithubOauth() {
        String loginPageUrl = githubOauthAdapter.requestGithubOauthSignIn();
        EntityModel<Map<String, String>> selfRel =
            methodOn(GithubOAuthController.class).trySignInAtGithubOauth();

        return EntityModel.of(
            Map.of("result", "OK"),
            linkTo(selfRel).withSelfRel(),
            Link.of(loginPageUrl).withRel("login")
        );
    }


}
