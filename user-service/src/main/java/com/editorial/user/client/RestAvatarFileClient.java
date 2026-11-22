package com.editorial.user.client;

import com.editorial.user.dto.client.UploadFileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Slf4j
@Component
public class RestAvatarFileClient {
    private final RestClient restClient;

    public RestAvatarFileClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public String uploadAvatar(long accountId, MultipartFile file) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        builder.part("file", file.getResource());
        builder.part("fileCategory", "AVATAR");
        builder.part("entityId", accountId);

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
            log.warn("Couldn't upload user's avatar", e);
            return null;
        }
    }

    public void deleteAvatar(String objectKey) {
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
            log.warn("Couldn't delete user's avatar picture", e);
        }
    }

    public byte[] getAvatar(String objectKey) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/internal/files")
                            .queryParam("key", objectKey)
                            .build())
                    .retrieve()
                    .body(byte[].class);
        } catch (RestClientException e) {
            log.warn("Couldnt't retrieve avatar");
            return null;
        }
    }


}
