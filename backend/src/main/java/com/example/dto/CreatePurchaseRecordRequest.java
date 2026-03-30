package com.example.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreatePurchaseRecordRequest {

    @NotNull(message = "进货日期不能为空")
    private LocalDate purchaseDate;

    private String supplier;

    private String remark;

    @NotNull(message = "进货明细不能为空")
    @Valid
    private List<CreatePurchaseItemRequest> items;
}
