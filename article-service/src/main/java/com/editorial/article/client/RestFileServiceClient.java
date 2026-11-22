package com.editorial.article.client;

import com.editorial.article.dto.client.UploadFileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Component
@Slf4j
public class RestFileServiceClient {
    private final RestClient restClient;

    public RestFileServiceClient(@Qualifier("fileServiceRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public String uploadFile(long entityId, String category, MultipartFile file) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        builder.part("file", file.getResource());
        builder.part("fileCategory", category); //cover or article file
        builder.part("entityId", entityId);

        try {
            return Objects.requireNonNull(
                            restClient
                                    .post()
                                    .uri("/api/internal/files")
                                    .contentType(MediaType.MULTIPART_FORM_DATA)
                                    .body(builder.build())
                                    .retrieve()
                                    .body(UploadFileResponse.class))
                    .objectKey();
        } catch (RestClientException e) {
            log.warn("Couldn't upload file", e);
            return null;
        }
    }

    public void deleteFile(String objectKey) {
        try {
            restClient
                    .delete()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/internal/files")
                            .queryParam("key", objectKey)
                            .build())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("Couldn't delete file", e);
        }
    }

}
