package com.griddynamics.sshmygin.reactive.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.griddynamics.sshmygin.reactive.model.Product;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(WireMockExtension.class)
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductInfoCommunicationServiceTest {

    private static WireMockServer server;
    private static ObjectMapper objectMapper;
    private static final List<Product> products = new ArrayList<>() {{
        add(new Product("id1", "1111", "name1", 100));
        add(new Product("id2", "2222", "name2", 90));
        add(new Product("id3", "3333", "name3", 80));
        add(new Product("id4", "4444", "name4", 70));
    }};

    @Autowired
    private WebClient productInfoServiceWebClient;

    @Autowired
    private ProductInfoCommunicationService productInfoCommunicationService;

    @BeforeClass
    public static void startWireMockServer() {
        objectMapper = new ObjectMapper();
        server = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        server.start();
    }

    @DynamicPropertySource
    public static void overrideWebClientBaseUrl(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("product_info_service_base_url", server::baseUrl);
    }

    @Test
    public void checkGetProductsByProductCodeReturnsFluxOfFourElements() throws JsonProcessingException {
        String productsJson = objectMapper.writeValueAsString(products);

        server.stubFor(WireMock.get(WireMock.urlMatching("/productInfoService/product/names.*"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(productsJson))
        );
        StepVerifier.create(productInfoCommunicationService.getProductsByProductCode("1111"))
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    public void checkGetProductsByProductCodeReturnsEmptyFluxIfExceptionHasBeenThrown() {
        server.stubFor(WireMock.get(WireMock.urlMatching("/productInfoService/product/names.*"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.GATEWAY_TIMEOUT.value()))
        );
        StepVerifier.create(productInfoCommunicationService.getProductsByProductCode("1111"))
                .expectNextCount(0)
                .verifyComplete();
    }

    @AfterClass
    public static void shutdownMireMockServer() {
        server.stop();
    }

}
