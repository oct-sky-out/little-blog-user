package com.octskyout.users.oauth.github.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;

import com.octskyout.users.config.GitHubOauth2ClientConfig;
import com.octskyout.users.config.RestTemplateConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@ExtendWith(SpringExtension.class)
@Import({GithubOauthAdapter.class, RestTemplateConfig.class})
class GithubOauthAdapterTest {
    @MockBean
    private GitHubOauth2ClientConfig oauthConfig;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    HashOperations<String, Object, Object> hashOperations;

    @Autowired
    private GithubOauthAdapter githubOauthAdapter;

    @BeforeEach
    void setUp() {
        given(oauthConfig.getClientId())
            .willReturn("8dd94b69615b6ca2abe5");
        given(oauthConfig.getRedirectUrl())
            .willReturn("http://127.0.0.1:8081/api/login/oauth2/github");
        given(redisTemplate.opsForHash())
            .willReturn(hashOperations);
    }

    @Test
    void requestGithubOauthSignIn() {
        willDoNothing()
            .given(hashOperations)
            .put(anyString(), anyString(), any(Boolean.class));

        String redirectUrl = githubOauthAdapter.requestGithubOauthSignIn();

        assertThat(redirectUrl).contains("github.com");
        log.debug("result REDIRECT URL => " + redirectUrl);

        then(redisTemplate.opsForHash())
            .should(times(1))
            .put(anyString(), anyString(), any(Boolean.class));
    }
}
