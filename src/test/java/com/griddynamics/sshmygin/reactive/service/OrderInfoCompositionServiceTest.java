package com.griddynamics.sshmygin.reactive.service;

import com.griddynamics.sshmygin.reactive.model.Order;
import com.griddynamics.sshmygin.reactive.model.OrderFullInfo;
import com.griddynamics.sshmygin.reactive.model.Product;
import com.griddynamics.sshmygin.reactive.model.User;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderInfoCompositionServiceTest {

    @Mock
    private OrderSearchCommunicationService orderSearchCommunicationService;
    @Mock
    private ProductInfoCommunicationService productInfoCommunicationService;
    @Mock
    private UserService userService;


    @Test
    public void checkGetTheMostRelevantProductReturnsOrderInfoWithTheHighestProductScore() {

        OrderInfoCompositionService orderInfoCompositionService = new OrderInfoCompositionService(
                orderSearchCommunicationService,
                productInfoCommunicationService,
                userService);

        User testUser = new User("id1", "Test", "123456789");
        Product expectedProduct = new Product("id1", "2222", "name1", 100);
        Order expectedOrder = new Order("123456789", "order2", "2222");

        Mono<User> testUserMono = Mono.just(testUser);
        Flux<Order> orderFlux = Flux.fromIterable(new ArrayList<>() {{
            add(new Order("123456789", "order1", "1111"));
            add(new Order("123456789", "order2", "2222"));
        }});
        Flux<Product> productFlux1 = Flux.fromIterable(new ArrayList<>() {{
            add(new Product("id4", "1111", "name4", 70));
            add(new Product("id2", "1111", "name2", 90));
        }});
        Flux<Product> productFlux2 = Flux.fromIterable(new ArrayList<>() {{
            add(new Product("id3", "2222", "name3", 80));
            add(new Product("id1", "2222", "name1", 100));
        }});

        given(userService.getUserById(ArgumentMatchers.anyString()))
                .willReturn(testUserMono);
        given(orderSearchCommunicationService.getOrdersByPhoneNumber(ArgumentMatchers.anyString()))
                .willReturn(orderFlux);
        given(productInfoCommunicationService.getProductsByProductCode("1111"))
                .willReturn(productFlux1);
        given(productInfoCommunicationService.getProductsByProductCode("2222"))
                .willReturn(productFlux2);

        OrderFullInfo expectedOrderFullInfo = OrderFullInfo.builder()
                .orderNumber(expectedOrder.getOrderNumber())
                .username(testUser.getName())
                .phoneNumber(testUser.getPhone())
                .productCode(expectedProduct.getProductCode())
                .productName(expectedProduct.getProductName())
                .productId(expectedProduct.getProductId())
                .productScore(expectedProduct.getScore())
                .build();


        StepVerifier.create(orderInfoCompositionService.getTheMostRelevantProduct(testUser.getId()))
                .expectNext(expectedOrderFullInfo)
                .verifyComplete();
    }

    @Test
    public void checkGetTheMostRelevantProductReturnsOrderInfoWithNullValueBelongsProductIfCouldNotRecieveProducts() {

        OrderInfoCompositionService orderInfoCompositionService = new OrderInfoCompositionService(
                orderSearchCommunicationService,
                productInfoCommunicationService,
                userService);

        User testUser = new User("id1", "Test", "123456789");

        Mono<User> testUserMono = Mono.just(testUser);
        Flux<Order> orderFlux = Flux.fromIterable(new ArrayList<>() {{
            add(new Order("123456789", "order1", "1111"));
            add(new Order("123456789", "order2", "2222"));
        }});

        given(userService.getUserById(ArgumentMatchers.anyString()))
                .willReturn(testUserMono);
        given(orderSearchCommunicationService.getOrdersByPhoneNumber(ArgumentMatchers.anyString()))
                .willReturn(orderFlux);
        given(productInfoCommunicationService.getProductsByProductCode(ArgumentMatchers.matches(".*")))
                .willReturn(Flux.empty());

        StepVerifier.create(orderInfoCompositionService.getTheMostRelevantProduct(testUser.getId()))
                .expectNextMatches(orderFullInfo ->
                        orderFullInfo.getProductName() == null && orderFullInfo.getProductId() == null
                )
                .verifyComplete();
    }

    /*
      By this test we are checking if the getTheMostRelevantProduct() method returns
       OrderFullInfo that contains info about the most relevant product even though
       products were recieved from ProductInfoService by some productCode successfully,
       but by other productCode it was with failure
     */
    @Test
    public void checkCaseIfWeHaveSomePartialIssuesWithReceivingProductInfoFromService() {

        OrderInfoCompositionService orderInfoCompositionService = new OrderInfoCompositionService(
                orderSearchCommunicationService,
                productInfoCommunicationService,
                userService);

        User testUser = new User("id1", "Test", "123456789");
        Product expectedProduct = new Product("id1", "2222", "name1", 100);
        Order expectedOrder = new Order("123456789", "order2", "2222");

        Mono<User> testUserMono = Mono.just(testUser);
        Flux<Order> orderFlux = Flux.fromIterable(new ArrayList<>() {{
            add(new Order("123456789", "order1", "1111"));
            add(new Order("123456789", "order2", "2222"));
        }});
        Flux<Product> productFlux2 = Flux.fromIterable(new ArrayList<>() {{
            add(new Product("id3", "2222", "name3", 80));
            add(new Product("id1", "2222", "name1", 100));
        }});

        given(userService.getUserById(ArgumentMatchers.anyString()))
                .willReturn(testUserMono);
        given(orderSearchCommunicationService.getOrdersByPhoneNumber(ArgumentMatchers.anyString()))
                .willReturn(orderFlux);
        given(productInfoCommunicationService.getProductsByProductCode("1111"))
                .willReturn(Flux.empty());
        given(productInfoCommunicationService.getProductsByProductCode("2222"))
                .willReturn(productFlux2);

        OrderFullInfo expectedOrderFullInfo = OrderFullInfo.builder()
                .orderNumber(expectedOrder.getOrderNumber())
                .username(testUser.getName())
                .phoneNumber(testUser.getPhone())
                .productCode(expectedProduct.getProductCode())
                .productName(expectedProduct.getProductName())
                .productId(expectedProduct.getProductId())
                .productScore(expectedProduct.getScore())
                .build();


        StepVerifier.create(orderInfoCompositionService.getTheMostRelevantProduct(testUser.getId()))
                .expectNext(expectedOrderFullInfo)
                .verifyComplete();
    }
}