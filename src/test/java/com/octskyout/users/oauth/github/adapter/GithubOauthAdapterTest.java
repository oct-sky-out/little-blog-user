package com.octskyout.users.oauth.github.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.octskyout.users.config.GitHubOauth2ClientConfig;
import com.octskyout.users.oauth.github.dto.GithubUserAccessTokenDto;
import com.octskyout.users.oauth.github.dto.GithubUserDto;
import com.octskyout.users.token.TokenType;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

@Slf4j
@ExtendWith(SpringExtension.class)
@Import({GithubOauthAdapter.class})
class GithubOauthAdapterTest {
    @MockBean
    private GitHubOauth2ClientConfig oauthConfig;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private RestTemplate restTemplate;

    @Mock
    ValueOperations<String, Object> valueOperations;

    @Autowired
    private GithubOauthAdapter githubOauthAdapter;
    private String redirectUrl = "http://127.0.0.1:8081/api/login/oauth2/github";
    private String clientId = "8dd94b69615b6ca2abe5";
    private String clientSecret = "secret";
    private String state = UUID.randomUUID().toString();
    private String key = GithubOauthAdapter.OAUTH_STATE + state;

    @BeforeEach
    void setUp() {
        given(oauthConfig.getClientId())
            .willReturn(clientId);
        given(oauthConfig.getRedirectUrl())
            .willReturn(redirectUrl);
        given(oauthConfig.getClientSecret())
            .willReturn(clientSecret);
        given(redisTemplate.opsForValue())
            .willReturn(valueOperations);
    }

    @Test
    void 소셜_로그인요청을_깃허브에_수행한다() {
        // setup
        String loginRequestUrl =
            "/login/oauth/authorize?client_id={client_id}&redirect_uri={redirect_uri}&scope={scope}&state={state}";
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, "http://github.com");
        ResponseEntity<String> response = new ResponseEntity<>(headers, HttpStatus.MOVED_TEMPORARILY);

        // given
        willDoNothing()
            .given(valueOperations)
            .set(anyString(), any(Boolean.class));
        given(redisTemplate.expire(key, 5, TimeUnit.MINUTES))
            .willReturn(true);
        given(restTemplate.getForEntity(loginRequestUrl, String.class,
            Map.of("client_id", clientId,
            "redirect_uri", redirectUrl,
            "scope", "user:login user:id user:avatar_url user:html_url user:email",
            "state", state)))
            .willReturn(response);

        // when
        String redirectUrl = githubOauthAdapter.requestGithubOauthSignIn(state);

        // then
        assertThat(redirectUrl).contains("github.com");
        log.debug("result REDIRECT URL => " + redirectUrl);

        then(redisTemplate.opsForValue())
            .should(times(1))
            .set(anyString(), any(Boolean.class));
        then(redisTemplate)
            .should(times(1))
            .expire(key, 5, TimeUnit.MINUTES);
    }

    @Test
    void 계정코드를_통해_깃허브_액세스_토큰을_얻은_후_계정정보를_얻어_로그인_성공_및_계정정보를_가져온다() {
        String testAccessCode = "access code";
        String accessTokenUri = "/login/oauth/access_token";
        String testAccessToken = "test token";
        String scope = "user:login user:id user:avatar_url user:html_url user:email";
        String tokenType = TokenType.ACCESS.toString();
        GithubUserAccessTokenDto accessTokenArgs =
            new GithubUserAccessTokenDto(testAccessToken, scope, tokenType);
        String userAccountAccessUri = "https://api.github.com/user";

        ResponseEntity<GithubUserDto> accountUser =
            new ResponseEntity<>(new GithubUserDto("username", 123L, "", "", null), HttpStatus.OK);

        given(valueOperations.get(key))
            .willReturn(false);
        given(restTemplate.postForObject(eq(accessTokenUri), any(HttpEntity.class), eq(GithubUserAccessTokenDto.class)))
            .willReturn(accessTokenArgs);
        given(restTemplate.exchange(eq(userAccountAccessUri), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(GithubUserDto.class)))
            .willReturn(accountUser);

        githubOauthAdapter.processOAuthLogin(testAccessCode, state);

        then(valueOperations)
            .should(times(1))
            .get(key);
        then(restTemplate)
            .should(times(1))
            .postForObject(eq(accessTokenUri), any(HttpEntity.class), eq(GithubUserAccessTokenDto.class));
        then(restTemplate)
            .should(times(1))
            .exchange(eq(userAccountAccessUri), eq(HttpMethod.GET), any(HttpEntity.class), eq(GithubUserDto.class));
    }

    @Test
    void key가_만료되거나_이미_접근했을_경우_접근시도_예외를_발생시킨다() {

        String testAccessCode = "access code";
        String accessTokenUri = "/login/oauth/access_token";
        String testAccessToken = "test token";
        String scope = "user:login user:id user:avatar_url user:html_url user:email";
        String tokenType = TokenType.ACCESS.toString();
        GithubUserAccessTokenDto accessTokenArgs =
            new GithubUserAccessTokenDto(testAccessToken, scope, tokenType);
        String userAccountAccessUri = "https://api.github.com/user";

        ResponseEntity<GithubUserDto> accountUser =
            new ResponseEntity<>(new GithubUserDto("username", 123L, "", "", null), HttpStatus.OK);

        given(valueOperations.get(key))
            .willReturn(true);
        given(restTemplate.postForObject(eq(accessTokenUri), any(HttpEntity.class), eq(GithubUserAccessTokenDto.class)))
            .willReturn(accessTokenArgs);
        given(restTemplate.exchange(eq(userAccountAccessUri), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(GithubUserDto.class)))
            .willReturn(accountUser);

        assertThatThrownBy(() -> githubOauthAdapter.processOAuthLogin(testAccessCode, state))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("이전에 로그인 접근을 시도한 계정입니다. 처음부터 새로 로그인에 접근해주세요.");

        then(valueOperations)
            .should(times(1))
            .get(key);
        then(restTemplate)
            .should(never())
            .postForObject(eq(accessTokenUri), any(HttpEntity.class), eq(GithubUserAccessTokenDto.class));
        then(restTemplate)
            .should(never())
            .exchange(eq(userAccountAccessUri), eq(HttpMethod.GET), any(HttpEntity.class), eq(GithubUserDto.class));
    }

    @Test
    void code가_깃허브에서_만료되어_accessToken에_접근시도할_경우_예외를_발생시킨다() {
        String testAccessCode = "access code";
        String accessTokenUri = "/login/oauth/access_token";
        String userAccountAccessUri = "https://api.github.com/user";

        ResponseEntity<GithubUserDto> accountUser =
            new ResponseEntity<>(new GithubUserDto("username", 123L, "", "", null), HttpStatus.OK);

        given(valueOperations.get(key))
            .willReturn(false);
        given(restTemplate.postForObject(eq(accessTokenUri), any(HttpEntity.class), eq(GithubUserAccessTokenDto.class)))
            .willReturn(null);
        given(restTemplate.exchange(eq(userAccountAccessUri), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(GithubUserDto.class)))
            .willReturn(accountUser);

        assertThatThrownBy(() -> githubOauthAdapter.processOAuthLogin(testAccessCode, state))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("사용자 정보가 일치하지 않아 접근권한을 얻을 수 없습니다.");

        then(valueOperations)
            .should(times(1))
            .get(key);
        then(restTemplate)
            .should(times(1))
            .postForObject(eq(accessTokenUri), any(HttpEntity.class), eq(GithubUserAccessTokenDto.class));
        then(restTemplate)
            .should(never())
            .exchange(eq(userAccountAccessUri), eq(HttpMethod.GET), any(HttpEntity.class), eq(GithubUserDto.class));
    }
}
