package com.griddynamics.sshmygin.reactive.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.griddynamics.sshmygin.reactive.model.Order;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;


@ExtendWith(WireMockExtension.class)
@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderSearchCommunicationServiceTest {

    private static final WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());

    private static final List<Order> orders = new ArrayList<>() {{
        add(new Order("123456789", "order1", "1111"));
        add(new Order("123456789", "order2", "2222"));
        add(new Order("123456789", "order3", "3333"));
        add(new Order("123456789", "order4", "4444"));
    }};



    @Autowired
    private WebClient orderSearchServiceWebClient;

    @Autowired
    private OrderSearchCommunicationService orderSearchCommunicationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeClass
    public static void startWireMockServer() {
        server.start();
        System.out.println(server.baseUrl());
    }

    @DynamicPropertySource
    public static void overrideWebClientBaseUrl(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("order_search_service_base_url", server::baseUrl);
    }

    @Test
    public void getOrdersByPhoneNumberShouldReturnTheFluxOfProducts() throws JsonProcessingException {
        String ordersJson = objectMapper.writeValueAsString(orders);
        String ordersMVSF = convertJsonListIntoMultyValueStreamFormat(ordersJson);

        server.stubFor(get(urlMatching("/orderSearchService/order/phone.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_NDJSON_VALUE)
                        .withBody(ordersMVSF)
                        .withStatus(200)));

        StepVerifier.create(orderSearchCommunicationService.getOrdersByPhoneNumber("123456789"))
                .expectNextCount(4)
                .verifyComplete();

    }

    @AfterClass
    public static void shutdownMireMockServer() {
        server.stop();
    }

    static public String convertJsonListIntoMultyValueStreamFormat(String jsonValue) {
        return jsonValue.replace("[", "")
                .replace("]", "")
                .replace("},{", "}{");
    }
}