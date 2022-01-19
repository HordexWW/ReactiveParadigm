package com.griddynamics.sshmygin.reactive.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.griddynamics.sshmygin.reactive.model.Order;
import com.griddynamics.sshmygin.reactive.model.Product;
import com.griddynamics.sshmygin.reactive.model.User;
import com.griddynamics.sshmygin.reactive.repository.UserInfoRepository;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTest {

    @MockBean private UserInfoRepository userInfoRepository;
    @Autowired private WebClient orderSearchServiceWebClient;
    @Autowired private WebClient productInfoServiceWebClient;

    private UserInfoService userInfoService;
    private static final ObjectMapper mapper = new ObjectMapper();

    private static WireMockServer server;

    private static final List<Order> orders = new ArrayList<>() {{
        add(new Order("123456789", "order1", "1111"));
        add(new Order("123456789", "order2", "2222"));
        add(new Order("123456789", "order3", "3333"));
        add(new Order("123456789", "order4", "4444"));
    }};

    private static final List<Product> products = new ArrayList<>() {{
        add(new Product("id1", "1111", "name1", 100));
        add(new Product("id2", "2222", "name2", 90));
        add(new Product("id3", "3333", "name3", 80));
        add(new Product("id4", "4444", "name4", 70));
    }};

    @BeforeClass
    public static void startWireMockServer() {
         server = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
         server.start();
    }

    @AfterClass
    public static void shutdownMireMockServer() {
        server.stop();
    }

    @DynamicPropertySource
    static void overrideWebClientBaseUrl(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("order_search_service_base_url", server::baseUrl);
        dynamicPropertyRegistry.add("product_info_service_base_url", server::baseUrl);
    }

    @Test
    public void getTheMostRelevantProductTest() throws JsonProcessingException {

        userInfoService = new UserInfoService(
                userInfoRepository,
                orderSearchServiceWebClient,
                productInfoServiceWebClient);

        User user = new User("user1", "John", "123456789");

        String ordersJson = mapper.writeValueAsString(orders);
        String ordersMVSF = convertJsonListIntoMultyValueStreamFormat(ordersJson);
        System.out.println(ordersMVSF);

        server.stubFor(get(urlMatching("/orderSearchService/order/phone?phoneNumber=\\d{9}"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_NDJSON_VALUE)
                        .withBody(ordersMVSF)
                        .withStatus(200))
        );

        String productsJson = mapper.writeValueAsString(products);

        server.stubFor(get(urlMatching("/productInfoService/product/names?productCode\\d{4}}"))
                .willReturn(aResponse()
                        .withBody(productsJson)
                        .withStatus(200))
        );

        given(userInfoRepository.findById("user1"))
                .willReturn(Mono.just(user));

        Product mostRelevantProduct = new Product("id1", "1111", "name1", 100);

        StepVerifier.create(userInfoService.getTheMostRelevantProduct("user1"))
                .expectNext(mostRelevantProduct)
                .verifyComplete();
    }


    static public String convertJsonListIntoMultyValueStreamFormat(String jsonValue) {
        return jsonValue.replace("[", "")
                .replace("]", "")
                .replace("},{", "}{");
    }
}