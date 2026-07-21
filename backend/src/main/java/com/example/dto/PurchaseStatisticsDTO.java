package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseStatisticsDTO {

    private long totalCount;

    private BigDecimal totalWeight;

    private BigDecimal totalAmount;
}
