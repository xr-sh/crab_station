package com.example.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdatePurchaseRecordRequest {

    private LocalDate purchaseDate;

    private String supplier;

    private String remark;

    @Valid
    private List<UpdatePurchaseItemRequest> items;
}
