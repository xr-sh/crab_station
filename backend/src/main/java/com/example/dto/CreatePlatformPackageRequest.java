package com.example.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CreatePlatformPackageRequest {

    @NotEmpty(message = "请选择平台套餐规格")
    @Valid
    private List<SelectionRequest> selections;

    private BigDecimal price;

    private Integer status;

    private String remark;

    @Data
    public static class SelectionRequest {
        private String category;
        private UUID platformSpecId;
        private Integer qty;
    }
}
