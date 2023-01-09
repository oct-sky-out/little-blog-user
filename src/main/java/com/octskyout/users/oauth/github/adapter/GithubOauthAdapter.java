package com.octskyout.users.oauth.github.adapter;

import com.octskyout.users.config.GitHubOauth2ClientConfig;
import com.octskyout.users.oauth.github.dto.GithubAccessTokenArgsDto;
import com.octskyout.users.oauth.github.dto.GithubUserAccessTokenDto;
import com.octskyout.users.oauth.github.dto.GithubUserDto;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GithubOauthAdapter {
    public static final String OAUTH_STATE = "OAUTH_STATE:";
    private final RestTemplate githubRestTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final GitHubOauth2ClientConfig gitHubOauthConfig;

    public String requestGithubOauthSignIn(String state) {
        String key = OAUTH_STATE + state;
        redisTemplate.opsForValue().set(key, false);
        redisTemplate.expire(key, 5, TimeUnit.MINUTES);

        Map<String, String> uriParam =
            Map.of("client_id", gitHubOauthConfig.getClientId(),
                "redirect_uri", gitHubOauthConfig.getRedirectUrl(),
                "scope", "user:login user:id user:avatar_url user:html_url user:email",
                "state", state);
        String loginRequestUrl =
            "/login/oauth/authorize?client_id={client_id}&redirect_uri={redirect_uri}&scope={scope}&state={state}";

        return Optional.ofNullable(
            githubRestTemplate.getForEntity(loginRequestUrl, String.class, uriParam).getHeaders()
                .getLocation()).orElseThrow(() -> new RuntimeException("유효하지않은 요청입니다.")).toString();
    }

    public GithubUserDto processOAuthLogin(String code, String state) {
        Boolean isExpired =
            (Boolean) Optional.ofNullable(redisTemplate.opsForValue().get(OAUTH_STATE + state))
                .orElse(true);

        if (Boolean.TRUE.equals(isExpired)) {
            throw new RuntimeException("이전에 로그인 접근을 시도한 계정입니다. 처음부터 새로 로그인에 접근해주세요.");
        }
        GithubUserAccessTokenDto accessTokenDto = requestAccessToken(code);
        return requestGithubUserProfile(accessTokenDto);
    }

    private GithubUserAccessTokenDto requestAccessToken(String code) {
        String exchangeUri = "/login/oauth/access_token";
        HttpEntity<GithubAccessTokenArgsDto> httpRequest = new HttpEntity<>(
            new GithubAccessTokenArgsDto(gitHubOauthConfig.getClientId(), gitHubOauthConfig.getClientSecret(), code));

        GithubUserAccessTokenDto githubUserAccessToken =
            githubRestTemplate.postForObject(exchangeUri, httpRequest, GithubUserAccessTokenDto.class);

        return Optional.ofNullable(githubUserAccessToken)
            .orElseThrow(() -> new RuntimeException("사용자 정보가 일치하지 않아 접근권한을 얻을 수 없습니다."));
    }

    private GithubUserDto requestGithubUserProfile(GithubUserAccessTokenDto accessTokenDto) {
        String exchangeUri = "https://api.github.com/user";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessTokenDto.accessToken());
        HttpEntity<Void> httpEntity = new HttpEntity<>(null, headers);

        return githubRestTemplate.exchange(
            exchangeUri, HttpMethod.GET, httpEntity, GithubUserDto.class).getBody();
    }
}
