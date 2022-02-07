package com.griddynamics.sshmygin.reactive.service;

import com.griddynamics.sshmygin.reactive.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import static com.griddynamics.sshmygin.reactive.logging.ContextualLoggingHelper.logOnError;
import static com.griddynamics.sshmygin.reactive.logging.ContextualLoggingHelper.logOnNext;

@Service
@Slf4j
public class OrderSearchCommunicationService {

    private final WebClient orderSearchServiceWebClient;

    public OrderSearchCommunicationService(WebClient orderSearchServiceWebClient) {
        this.orderSearchServiceWebClient = orderSearchServiceWebClient;
    }

    public Flux<Order> getOrdersByPhoneNumber(String phoneNumber) {
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
}
