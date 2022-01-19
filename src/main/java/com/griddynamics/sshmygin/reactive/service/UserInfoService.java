package com.griddynamics.sshmygin.reactive.service;

import com.griddynamics.sshmygin.reactive.model.Order;
import com.griddynamics.sshmygin.reactive.model.Product;
import com.griddynamics.sshmygin.reactive.model.User;
import com.griddynamics.sshmygin.reactive.repository.UserInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static com.griddynamics.sshmygin.reactive.logging.ContextualLoggingHelper.logOnError;
import static com.griddynamics.sshmygin.reactive.logging.ContextualLoggingHelper.logOnNext;

@Service
@Slf4j
public class UserInfoService {

    private final UserInfoRepository userInfoRepository;

    private final WebClient orderSearchServiceWebClient;
    private final WebClient productInfoServiceWebClient;

    public UserInfoService(UserInfoRepository userInfoRepository,
                           WebClient orderSearchServiceWebClient,
                           WebClient productInfoServiceWebClient) {
        this.userInfoRepository = userInfoRepository;
        this.orderSearchServiceWebClient = orderSearchServiceWebClient;
        this.productInfoServiceWebClient = productInfoServiceWebClient;
    }

    public Mono<Product> getTheMostRelevantProduct(String userId) {
        return userInfoRepository.findById(userId)
                .map(User::getPhone)
                .switchIfEmpty(Mono.error(new RuntimeException("User with id " + userId + "not found")))
                .doOnError(ignore -> log.error("User with id {} not found", userId))
                .flatMapMany(this::getOrdersByPhoneNumber)
                .map(Order::getProductCode)
                .flatMap(this::getProductsByProductCode)
                .sort((product1, product2) -> Double.compare(product2.getScore(), product1.getScore()))
                .next();
    }

    private Flux<Order> getOrdersByPhoneNumber(String phoneNumber) {
        return orderSearchServiceWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/orderSearchService/order/phone")
                        .queryParam("phoneNumber", phoneNumber)
                        .build())
                .retrieve()
                .bodyToFlux(Order.class)
                .doOnEach(logOnNext(order -> log.info("Received order from OrderSearchService. Order: {}", order)))
                .doOnEach(logOnError(e -> log.error("Could not receive order from OrderSearchService", e)));

    }

    private Flux<Product> getProductsByProductCode(String productCode) {
        return productInfoServiceWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/productInfoService/product/names")
                        .queryParam("productCode", productCode)
                        .build())
                .retrieve()
                .bodyToFlux(Product.class)
                .doOnEach(logOnNext(product -> log.info("Received product from ProductInfoService. Product: {}", product)))
                .doOnEach(logOnError(e -> log.error("Could not receive products from ProductInfoService", e)))
                .onErrorResume(ex -> Flux.empty());
    }
}
