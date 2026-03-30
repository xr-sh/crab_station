package com.example.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateFinanceRecordRequest {

    private LocalDate recordDate;

    @Positive(message = "金额必须大于0")
    private BigDecimal amount;

    private String type;

    private String remark;
}
