package com.yh.budgetly.config;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
public class WebClientConfig {

    @Value("${supabase.storage.url}")
    private String supabaseUrl;

    @Value("${supabase.storage.service-role-key}")
    private String serviceRoleKey;

    @Bean
    public WebClient supabaseWebClient() {
        return WebClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader("apikey", serviceRoleKey)
                .defaultHeader("Authorization", "Bearer " + serviceRoleKey)
                .build();
    }
}
