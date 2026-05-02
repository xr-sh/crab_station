package com.example.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreatePurchaseRecordRequest {

    @NotNull(message = "Purchase date cannot be null")
    private LocalDate purchaseDate;

    private String supplier;

    private String remark;

    @NotEmpty(message = "Purchase items cannot be empty")
    @Valid
    private List<CreatePurchaseItemRequest> items;
}
