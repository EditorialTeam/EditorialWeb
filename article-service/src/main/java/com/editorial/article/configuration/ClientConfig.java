package com.editorial.article.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfig {
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient userServiceRestClient(RestClient.Builder builder) {
        return builder.baseUrl("http://user-service:8080").build();
    }

    @Bean
    public RestClient fileServiceRestClient(RestClient.Builder builder) {
        return builder.baseUrl("http://file-service:8080").build();
    }
}
