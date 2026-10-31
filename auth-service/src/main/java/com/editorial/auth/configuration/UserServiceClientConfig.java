package com.editorial.auth.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver;
import org.springframework.web.client.RestClient;

@Configuration
public class UserServiceClientConfig {
    @Bean
    public RestClient userServiceRestClient(
            @Value("${USER_SERVICE_URL}") String serviceUrl,
            OAuth2AuthorizedClientManager clientManager) {
        OAuth2ClientHttpRequestInterceptor interceptor = new OAuth2ClientHttpRequestInterceptor(clientManager);

        interceptor.setPrincipalResolver(new RequestAttributePrincipalResolver());

        return RestClient.builder().baseUrl(serviceUrl).requestInterceptor(interceptor).build();
    }
}
