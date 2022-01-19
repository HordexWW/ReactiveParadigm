package com.griddynamics.sshmygin.reactive.controller;

import com.griddynamics.sshmygin.reactive.model.Product;
import com.griddynamics.sshmygin.reactive.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;


@RestController
@RequestMapping("/userInfoService")
@Slf4j
public class UserInfoController {

    UserInfoService userInfoService;

    public UserInfoController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @GetMapping(value = "/orders", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Mono<Product> noname(
            @RequestParam String userId,
            @RequestHeader(required = false, name = "requestId") String requestId) {

        String idToPutInContext = requestId == null ?
                UUID.randomUUID().toString().substring(0, 11) :
                requestId;

        return userInfoService.getTheMostRelevantProduct(userId)
                .contextWrite(Context.of("requestId", idToPutInContext));
    }
}
