package com.griddynamics.sshmygin.reactive.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private final WebClient.Builder builder;

    public WebClientConfig(WebClient.Builder builder) {
        this.builder = builder;
    }

    @Bean
    public WebClient orderSearchServiceWebClient(@Value("${order_search_service_base_url}") String baseUrl) {
        return builder
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    public WebClient productInfoServiceWebClient(@Value("${product_info_service_base_url}") String baseUrl) {
        return builder
                .baseUrl(baseUrl)
                .build();
    }
}
