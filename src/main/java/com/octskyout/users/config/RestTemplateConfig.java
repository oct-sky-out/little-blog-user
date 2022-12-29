package com.octskyout.users.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate githubRestTemplate() {
        return new RestTemplateBuilder()
            .rootUri("https://github.com")
            .setConnectTimeout(Duration.of(3, ChronoUnit.SECONDS))
            .build();
    }
}
