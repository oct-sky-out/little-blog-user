package com.octskyout.users.config;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate githubRestTemplate() {
        return new RestTemplateBuilder()
            .rootUri("https://github.com")
            .setConnectTimeout(Duration.of(3, ChronoUnit.SECONDS))
            .requestFactory(ManualFollowRedirectsHttpRequestFactory::new)
            .build();
    }

    private static class ManualFollowRedirectsHttpRequestFactory extends SimpleClientHttpRequestFactory {
        @Override
        protected void prepareConnection(HttpURLConnection connection, String httpMethod)
            throws IOException {
            super.prepareConnection(connection, httpMethod);
            connection.setInstanceFollowRedirects(false);
        }
    }
}
