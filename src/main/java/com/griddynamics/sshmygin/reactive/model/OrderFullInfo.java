package com.griddynamics.sshmygin.reactive.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Builder
@Getter
public class OrderFullInfo {
    private String orderNumber;
    private String username;
    private String phoneNumber;
    private String productCode;
    private String productName;
    private String productId;

    @JsonIgnore
    private double productScore;
}
