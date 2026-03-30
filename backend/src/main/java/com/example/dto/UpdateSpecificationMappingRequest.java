package com.example.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UpdateSpecificationMappingRequest {

    private UUID purchaseSpecId;

    private UUID platformSpecId;

    private Integer status;

    private String remark;
}