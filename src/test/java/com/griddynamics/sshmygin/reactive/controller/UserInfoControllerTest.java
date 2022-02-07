package com.griddynamics.sshmygin.reactive.controller;

import com.griddynamics.sshmygin.reactive.model.OrderFullInfo;
import com.griddynamics.sshmygin.reactive.service.OrderInfoCompositionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.BDDMockito.given;


@RunWith(SpringRunner.class)
@WebFluxTest
public class UserInfoControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderInfoCompositionService orderInfoCompositionService;

    @Test
    public void checkGettingCorrectResponse() {
        OrderFullInfo expectedOrderInfo = OrderFullInfo.builder()
                .orderNumber("order1")
                .username("user1")
                .phoneNumber("123456789")
                .productCode("1")
                .productName("product1")
                .productId("id1")
                .build();

        given(orderInfoCompositionService.getTheMostRelevantProduct("user1"))
                .willReturn(Mono.just(expectedOrderInfo));

        webTestClient.get()
                .uri("/userInfoService/orders?userId=user1")
                .accept(MediaType.valueOf(MediaType.APPLICATION_NDJSON_VALUE))
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderFullInfo.class)
                .isEqualTo(expectedOrderInfo);
    }

    @Test
    public void checkGettingResponseWithNullFieldsBelongToProduct() {
        OrderFullInfo expectedOrderInfo = OrderFullInfo.builder()
                .orderNumber("order1")
                .username("user1")
                .phoneNumber("123456789")
                .productCode("1")
                .productName(null)
                .productId(null)
                .build();

        given(orderInfoCompositionService.getTheMostRelevantProduct("user1"))
                .willReturn(Mono.just(expectedOrderInfo));

        webTestClient.get()
                .uri("/userInfoService/orders?userId=user1")
                .accept(MediaType.valueOf(MediaType.APPLICATION_NDJSON_VALUE))
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderFullInfo.class)
                .isEqualTo(expectedOrderInfo);
    }
}