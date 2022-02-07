package com.griddynamics.sshmygin.reactive.service;

import com.griddynamics.sshmygin.reactive.model.User;
import com.griddynamics.sshmygin.reactive.repository.UserInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class UserService {

    private final UserInfoRepository userInfoRepository;

    public UserService(UserInfoRepository userInfoRepository) {
        this.userInfoRepository = userInfoRepository;
    }

    public Mono<User> getUserById(String userId) {
        return userInfoRepository.findById(userId);
    }
}
