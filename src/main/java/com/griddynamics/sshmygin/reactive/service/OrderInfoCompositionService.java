package com.griddynamics.sshmygin.reactive.service;

import com.griddynamics.sshmygin.reactive.model.OrderFullInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
public class OrderInfoCompositionService {

    private final OrderSearchCommunicationService orderSearchCommunicationService;
    private final ProductInfoCommunicationService productInfoCommunicationService;
    private final UserService userService;


    public OrderInfoCompositionService(OrderSearchCommunicationService orderSearchCommunicationService,
                                       ProductInfoCommunicationService productInfoCommunicationService,
                                       UserService userService) {
        this.orderSearchCommunicationService = orderSearchCommunicationService;
        this.productInfoCommunicationService = productInfoCommunicationService;
        this.userService = userService;
    }

    public Mono<OrderFullInfo> getTheMostRelevantProduct(String userId) {

        return userService.getUserById(userId)
                .switchIfEmpty(Mono.error(new RuntimeException("User with id " + userId + "not found")))
                .doOnError(ignore -> log.error("User with id {} not found", userId))
                .map(user -> OrderFullInfo.builder()
                        .username(user.getName())
                        .phoneNumber(user.getPhone())
                        .build())
                .flatMapMany(orderFullInfo -> orderSearchCommunicationService
                        .getOrdersByPhoneNumber(orderFullInfo.getPhoneNumber())
                        .map(orderInfo -> OrderFullInfo.builder()
                                .username(orderFullInfo.getUsername())
                                .phoneNumber(orderFullInfo.getPhoneNumber())
                                .orderNumber(orderInfo.getOrderNumber())
                                .productCode(orderInfo.getProductCode())
                                .build())
                        .subscribeOn(Schedulers.boundedElastic()))
                .flatMap(orderFullInfo -> productInfoCommunicationService
                        .getProductsByProductCode(orderFullInfo.getProductCode())
                        .map(productInfo -> OrderFullInfo.builder()
                                .username(orderFullInfo.getUsername())
                                .phoneNumber(orderFullInfo.getPhoneNumber())
                                .orderNumber(orderFullInfo.getOrderNumber())
                                .productCode(orderFullInfo.getProductCode())
                                .productId(productInfo.getProductId())
                                .productName(productInfo.getProductName())
                                .productScore(productInfo.getScore())
                                .build())
                        .defaultIfEmpty(OrderFullInfo.builder()
                                .username(orderFullInfo.getUsername())
                                .phoneNumber(orderFullInfo.getPhoneNumber())
                                .orderNumber(orderFullInfo.getOrderNumber())
                                .productCode(orderFullInfo.getProductCode())
                                .productId(null)
                                .productName(null)
                                .build())
                        .subscribeOn(Schedulers.boundedElastic()))
                .sort((info1, info2) -> Double.compare(info2.getProductScore(), info1.getProductScore()))
                .next();
    }
}
