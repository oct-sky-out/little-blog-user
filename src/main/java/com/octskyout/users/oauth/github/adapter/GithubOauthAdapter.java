package com.octskyout.users.oauth.github.adapter;

import com.octskyout.users.config.GitHubOauth2ClientConfig;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GithubOauthAdapter {
    private static final String OAUTH_STATE = "OAUTH_STATE";
    private final RestTemplate githubRestTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final GitHubOauth2ClientConfig gitHubOauthConfig;

    public String requestGithubOauthSignIn() {
        String state = UUID.randomUUID().toString();
        redisTemplate.opsForHash().put(OAUTH_STATE, state, false);

        Map<String, String> uriParam =
            Map.of("client_id", gitHubOauthConfig.getClientId(), "redirect_uri",
                gitHubOauthConfig.getRedirectUrl(), "scope", "user", "state", state);
        String loginRequestUrl =
            "/login/oauth/authorize?client_id={client_id}&redirect_uri={redirect_uri}&scope={scope}&state={state}";

        return Optional.ofNullable(
            githubRestTemplate.getForEntity(loginRequestUrl, String.class, uriParam)
                .getHeaders()
                .getLocation())
            .orElseThrow(() -> new RuntimeException("유효하지않은 요청입니다."))
            .toString();
    }

}
