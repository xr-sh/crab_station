package com.example.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdatePlatformPackageRequest {

    @Valid
    private List<CreatePlatformPackageRequest.SelectionRequest> selections;

    private BigDecimal price;

    private Integer status;

    private String remark;
}
