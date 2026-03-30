package com.example.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class FinanceRecordDTO {
    private UUID id;
    private LocalDate recordDate;
    private BigDecimal amount;
    private String type;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
