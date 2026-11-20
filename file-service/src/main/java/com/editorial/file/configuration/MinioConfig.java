package com.editorial.file.configuration;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.editorial.file.configuration.MinioProperties;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {
    @Bean
    public MinioClient minioClient(MinioProperties minioProperties) {
        return MinioClient.builder()
                .endpoint(minioProperties.url())
                .credentials(minioProperties.accessKey(), minioProperties.secretKey())
                .build();
    }
}
