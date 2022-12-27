package com.octskyout.users.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity(debug = true)
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//            .oauth2Client()
//            .clientRegistrationRepository(clientRegistrationRepository())
//            .authorizedClientService();

        return http.build();
    }

    private ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(
            githubOauthClientRegistration(null));
    }

    @Autowired
    public ClientRegistration githubOauthClientRegistration(GitHubOauth2ClientConfig githubConfig) {
        return CommonOAuth2Provider.GITHUB
            .getBuilder("Github")
            .clientId(githubConfig.getClientId())
            .clientSecret(githubConfig.getClientSecret())
            .redirectUri(githubConfig.getRedirectUrl())
            .build();
    }

}
