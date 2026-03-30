package com.example.controller;

import com.example.dto.*;
import com.example.entity.PurchaseSpec;
import com.example.service.PurchaseSpecService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/purchase-specs")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"})
public class PurchaseSpecController {

    private final PurchaseSpecService purchaseSpecService;

    @GetMapping
    public ResponseEntity<PageResponse<PurchaseSpecDTO>> getPurchaseSpecs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {

        Sort sort = Sort.by(sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<PurchaseSpec> specPage = purchaseSpecService.getPurchaseSpecs(name, status, pageRequest);

        PageResponse<PurchaseSpecDTO> response = PageResponse.<PurchaseSpecDTO>builder()
                .content(specPage.getContent().stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()))
                .totalPages(specPage.getTotalPages())
                .totalElements(specPage.getTotalElements())
                .size(specPage.getSize())
                .number(specPage.getNumber())
                .first(specPage.isFirst())
                .last(specPage.isLast())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PurchaseSpecDTO>> getAllPurchaseSpecs() {
        List<PurchaseSpec> specs = purchaseSpecService.getAllPurchaseSpecs();
        List<PurchaseSpecDTO> dtos = specs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseSpecDTO> getPurchaseSpecById(@PathVariable UUID id) {
        PurchaseSpec spec = purchaseSpecService.getPurchaseSpecById(id);
        return ResponseEntity.ok(convertToDTO(spec));
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createPurchaseSpec(@Valid @RequestBody CreatePurchaseSpecRequest request) {
        purchaseSpecService.createPurchaseSpec(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "进货规格创建成功");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updatePurchaseSpec(@PathVariable UUID id, @Valid @RequestBody UpdatePurchaseSpecRequest request) {
        purchaseSpecService.updatePurchaseSpec(id, request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "进货规格更新成功");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePurchaseSpec(@PathVariable UUID id) {
        purchaseSpecService.deletePurchaseSpec(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "进货规格删除成功");
        return ResponseEntity.ok(response);
    }

    private PurchaseSpecDTO convertToDTO(PurchaseSpec spec) {
        return PurchaseSpecDTO.builder()
                .id(spec.getId())
                .name(spec.getName())
                .status(spec.getStatus())
                .remark(spec.getRemark())
                .createdAt(spec.getCreatedAt())
                .updatedAt(spec.getUpdatedAt())
                .build();
    }
}