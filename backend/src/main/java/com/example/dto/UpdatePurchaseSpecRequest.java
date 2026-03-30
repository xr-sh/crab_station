package com.example.dto;

import lombok.Data;

@Data
public class UpdatePurchaseSpecRequest {

    private String name;

    private Integer status;

    private String remark;
}