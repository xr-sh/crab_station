package com.example.controller;

import com.example.dto.*;
import com.example.entity.PurchaseRecord;
import com.example.service.PurchaseService;
import com.example.util.PageRequestUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"})
public class PurchaseController {

    private final PurchaseService purchaseService;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("purchaseDate", "supplier", "totalWeight", "totalAmount", "createdAt", "updatedAt");

    @GetMapping
    public ResponseEntity<PageResponse<PurchaseRecordDTO>> getPurchaseRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "purchaseDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        PageRequest pageRequest = PageRequestUtils.of(page, size, sortBy, sortDirection, ALLOWED_SORT_FIELDS);

        Page<PurchaseRecord> recordPage = purchaseService.getPurchaseRecords(supplier, startDate, endDate, pageRequest);

        PageResponse<PurchaseRecordDTO> response = PageResponse.<PurchaseRecordDTO>builder()
                .content(recordPage.getContent().stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()))
                .totalPages(recordPage.getTotalPages())
                .totalElements(recordPage.getTotalElements())
                .size(recordPage.getSize())
                .number(recordPage.getNumber())
                .first(recordPage.isFirst())
                .last(recordPage.isLast())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    public ResponseEntity<PurchaseStatisticsDTO> getPurchaseStatistics(
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        PurchaseStatisticsDTO statistics = purchaseService.getPurchaseStatistics(supplier, startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseRecordDTO> getPurchaseRecordById(@PathVariable UUID id) {
        PurchaseRecord record = purchaseService.getPurchaseRecordById(id);
        return ResponseEntity.ok(convertToDTO(record));
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createPurchaseRecord(@Valid @RequestBody CreatePurchaseRecordRequest request) {
        purchaseService.createPurchaseRecord(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "进货记录创建成功");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updatePurchaseRecord(@PathVariable UUID id, @Valid @RequestBody UpdatePurchaseRecordRequest request) {
        purchaseService.updatePurchaseRecord(id, request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "进货记录更新成功");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePurchaseRecord(@PathVariable UUID id) {
        purchaseService.deletePurchaseRecord(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "进货记录删除成功");
        return ResponseEntity.ok(response);
    }

    private PurchaseRecordDTO convertToDTO(PurchaseRecord record) {
        List<PurchaseItemDTO> itemDTOs = record.getItems().stream()
                .map(item -> PurchaseItemDTO.builder()
                        .id(item.getId())
                        .purchaseSpecId(item.getPurchaseSpec().getId())
                        .purchaseSpecName(item.getPurchaseSpec().getName())
                        .weight(item.getWeight())
                        .unitPrice(item.getUnitPrice())
                        .amount(item.getAmount())
                        .createdAt(item.getCreatedAt())
                        .updatedAt(item.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return PurchaseRecordDTO.builder()
                .id(record.getId())
                .purchaseDate(record.getPurchaseDate())
                .supplier(record.getSupplier())
                .totalWeight(record.getTotalWeight())
                .totalAmount(record.getTotalAmount())
                .remark(record.getRemark())
                .items(itemDTOs)
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
