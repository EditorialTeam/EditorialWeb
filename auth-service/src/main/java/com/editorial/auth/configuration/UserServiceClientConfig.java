package com.editorial.auth.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class UserServiceClientConfig {
    @Bean
    public RestClient userServiceRestClient(
            @Value("${USER_SERVICE_URL}") String serviceUrl) {
        return RestClient.builder().baseUrl(serviceUrl).build();
    }
}
