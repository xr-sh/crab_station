package com.example.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdatePurchaseSpecRequest {

    private String name;

    private String category;

    private BigDecimal price;

    private String priceChangeReason;

    private Integer status;

    private String remark;
}
