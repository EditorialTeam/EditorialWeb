package com.editorial.user.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.converter.JacksonJsonMessageConverter;

@Configuration
public class KafkaConfig {

    @Bean
    public JacksonJsonMessageConverter kafkaMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}