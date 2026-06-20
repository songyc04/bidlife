package com.example.final_project.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Configuration
public class TossConfig {

    @Value("${toss.secret-key}")
    private String secretKey;

    @Value("${toss.api-base-url}")
    private String apiBaseUrl;

    @Value("${toss.test-mode:true}")
    private boolean testMode;

    public String getSecretKey() {
        return secretKey;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public String getAuthorizationHeader() {
        String credentials = secretKey + ":";
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(30000);
        return new RestTemplate(factory);
    }
}
