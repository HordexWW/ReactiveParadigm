package com.griddynamics.sshmygin.reactive.service;

import com.griddynamics.sshmygin.reactive.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static com.griddynamics.sshmygin.reactive.logging.ContextualLoggingHelper.logOnError;
import static com.griddynamics.sshmygin.reactive.logging.ContextualLoggingHelper.logOnNext;

@Service
@Slf4j
public class ProductInfoCommunicationService {

    private final WebClient productInfoServiceWebClient;

    public ProductInfoCommunicationService(WebClient productInfoServiceWebClient) {
        this.productInfoServiceWebClient = productInfoServiceWebClient;
    }

    public Flux<Product> getProductsByProductCode(String productCode) {
        return productInfoServiceWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/productInfoService/product/names")
                        .queryParam("productCode", productCode)
                        .build())
                .retrieve()
                .bodyToFlux(Product.class)
                .timeout(Duration.ofSeconds(5))
                .doOnEach(logOnNext(product -> log.info("Received product from ProductInfoService. Product: {}", product)))
                .doOnEach(logOnError(e -> log.error("Could not receive products from ProductInfoService", e)))
                .onErrorResume(ex -> Flux.empty());
    }
}
