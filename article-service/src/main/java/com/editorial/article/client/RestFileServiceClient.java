package com.editorial.article.client;

import com.editorial.article.dto.client.UploadFileResponse;
import com.editorial.article.exception.FileUploadException;
import com.editorial.article.exception.InvalidFileRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

@Component
@Slf4j
public class RestFileServiceClient {
    private final RestClient restClient;

    public RestFileServiceClient(@Qualifier("fileServiceRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public String uploadFile(long entityId, String category, MultipartFile file) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add("file", file.getResource());
        body.add("fileCategory", category); //cover or article file
        body.add("entityId", entityId);

        try {
            UploadFileResponse response = restClient
                    .post()
                    .uri("/api/internal/files")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(UploadFileResponse.class);
            if (response == null || response.objectKey() == null || response.objectKey().isBlank()) throw new FileUploadException("File service returned an empty key");
            return response.objectKey();
        } catch (RestClientException e) {
            log.warn("Couldn't upload file", e);
            throw new FileUploadException("Couldn't upload file", e);
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
