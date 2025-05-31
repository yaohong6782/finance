package com.yh.budgetly.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

@Slf4j
@Component
@Service
@AllArgsConstructor
public class SupabaseStorageService {

    @Value("${supabase.storage.url}")
    private String supabaseUrl;

    @Value("${supabase.storage.bucket}")
    private String bucketName;

    @Value("${supabase.storage.service-role-key}")
    private String serviceRoleKey;

    private WebClient webClient;

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

    public String signedBucketFile(String fileName, int expiresInSeconds) {
        webClient = WebClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader("apiKey,", serviceRoleKey)
                .defaultHeader("Authorization", "Bearer " + serviceRoleKey)
                .build();

        String endpoint = "/storage/v1/object/sign" + bucketName + "/" + fileName;
        String requestBody = "{\"expiresIn\":" +  expiresInSeconds + "}";

        String response = webClient.post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (response == null || !response.contains("signedURL"))  {
            throw new RuntimeException("Failed to get signed URL " + response);
        }

        String signedUrl = supabaseUrl + response.replaceAll(".*\"signedURL\":\"([^\"]+)\".*", "$1");

        return signedUrl;
    }
}
