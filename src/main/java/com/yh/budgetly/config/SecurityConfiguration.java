package com.yh.budgetly.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Slf4j
@AllArgsConstructor
@Configuration
@EnableWebSecurity
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(AbstractHttpConfigurer::disable)
                .cors(httpSecurityCorsConfigurer ->
                        httpSecurityCorsConfigurer.configurationSource(request ->
                                new CorsConfiguration().applyPermitDefaultValues()))
                .csrf(AbstractHttpConfigurer::disable)
                .securityMatcher("/**")
                .sessionManagement(sessionManagementConfigurer ->
                        sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(registry -> registry
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/swagger-ui.html", "/webjars/**")
                                .permitAll()
                                .requestMatchers("/").permitAll()
                                .requestMatchers(HttpMethod.GET, "/*").permitAll()
                                .requestMatchers(HttpMethod.GET, "/finances/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/*").permitAll()
                                .requestMatchers(HttpMethod.POST, "/user/**").permitAll() // Allow access to this endpoint
                                .requestMatchers(HttpMethod.POST, "/transactions/**").permitAll() // Allow access to this endpoint
                                .requestMatchers(HttpMethod.POST, "/dashboard/**").permitAll() // Allow access to this endpoint
                                .requestMatchers(HttpMethod.POST, "/finances/**").permitAll() // Allow access to this endpoint
                                .requestMatchers(HttpMethod.GET, "/api/**").permitAll() // Allow access to this endpoint
//                        .requestMatchers(HttpMethod.POST, "/**").permitAll() // Allow access to this endpoint
                                .anyRequest().authenticated()
                )
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("frame-ancestors 'self' http://localhost:8085 http://localhost:3006")))
//                .headers(headers -> headers
//                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
//                        .contentSecurityPolicy(contentSecurityPolicyConfig -> contentSecurityPolicyConfig
//                                .policyDirectives("frame-ancestors 'self' http://localhost:8085")))
//
                .httpBasic(Customizer.withDefaults());
        return http.build();

    }
}
