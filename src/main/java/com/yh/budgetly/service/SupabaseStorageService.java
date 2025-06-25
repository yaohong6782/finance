package com.yh.budgetly.service;

import com.yh.budgetly.rest.dtos.SignedUrlDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class SupabaseStorageService {

    @Value("${supabase.storage.url}")
    private String supabaseUrl;

    @Value("${supabase.storage.bucket}")
    private String bucketName;

    @Value("${supabase.storage.service-role-key}")
    private String serviceRoleKey;

    private final WebClient supabaseWebClient;

    public SupabaseStorageService(WebClient supabaseWebClient) {
        this.supabaseWebClient = supabaseWebClient;
    }

    public String uploadFile(MultipartFile file, String fileName) throws IOException {
        String uploadUrl = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucketName, fileName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Bearer " + serviceRoleKey);
        headers.set("apikey", serviceRoleKey);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucketName, fileName);
        } else {
            throw new RuntimeException("Failed to upload file: " + response.getBody());
        }
    }

    public Mono<String> signedBucketFile(String fileName, int expiresInSeconds) {
        log.info("signing bucket file : {}, {} ", fileName, expiresInSeconds);

        String endpoint = "/storage/v1/object/sign/" + bucketName + "/" + fileName;
        Map<String, Object> requestBody = Map.of("expiresIn", expiresInSeconds);

        Mono<String> response = supabaseWebClient.post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(SignedUrlDTO.class)
                .map(res -> {

                    if (res == null || res.getSignedURL() == null) {
                        throw new RuntimeException("Failed to get signed URL " + res);
                    }
                    String signedUrl = supabaseUrl + "/storage/v1" + res.getSignedURL();

                    log.info("signed url : {} ", signedUrl);
                    return signedUrl;
                });

        return response;
    }
}
