package com.griddynamics.sshmygin.reactive.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Order {
    private String phoneNumber;
    private String orderNumber;
    private String productCode;
}
