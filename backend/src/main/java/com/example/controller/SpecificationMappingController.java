package com.example.controller;

import com.example.dto.*;
import com.example.entity.SpecificationMapping;
import com.example.service.SpecificationMappingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/specification-mappings")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"})
public class SpecificationMappingController {

    private final SpecificationMappingService specificationMappingService;

    @GetMapping
    public ResponseEntity<PageResponse<SpecificationMappingDTO>> getSpecificationMappings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String purchaseSpecName,
            @RequestParam(required = false) String platformSpecName,
            @RequestParam(required = false) Integer status) {

        Sort sort = Sort.by(sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<SpecificationMapping> mappingPage = specificationMappingService.getSpecificationMappings(purchaseSpecName, platformSpecName, status, pageRequest);

        PageResponse<SpecificationMappingDTO> response = PageResponse.<SpecificationMappingDTO>builder()
                .content(mappingPage.getContent().stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()))
                .totalPages(mappingPage.getTotalPages())
                .totalElements(mappingPage.getTotalElements())
                .size(mappingPage.getSize())
                .number(mappingPage.getNumber())
                .first(mappingPage.isFirst())
                .last(mappingPage.isLast())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpecificationMappingDTO> getSpecificationMappingById(@PathVariable UUID id) {
        SpecificationMapping mapping = specificationMappingService.getSpecificationMappingById(id);
        return ResponseEntity.ok(convertToDTO(mapping));
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createSpecificationMapping(@Valid @RequestBody CreateSpecificationMappingRequest request) {
        specificationMappingService.createSpecificationMapping(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "规格映射创建成功");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateSpecificationMapping(@PathVariable UUID id, @Valid @RequestBody UpdateSpecificationMappingRequest request) {
        specificationMappingService.updateSpecificationMapping(id, request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "规格映射更新成功");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteSpecificationMapping(@PathVariable UUID id) {
        specificationMappingService.deleteSpecificationMapping(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "规格映射删除成功");
        return ResponseEntity.ok(response);
    }

    private SpecificationMappingDTO convertToDTO(SpecificationMapping mapping) {
        return SpecificationMappingDTO.builder()
                .id(mapping.getId())
                .purchaseSpecId(mapping.getPurchaseSpec().getId())
                .purchaseSpecName(mapping.getPurchaseSpec().getName())
                .platformSpecId(mapping.getPlatformSpec().getId())
                .platformSpecName(mapping.getPlatformSpec().getName())
                .status(mapping.getStatus())
                .remark(mapping.getRemark())
                .createdAt(mapping.getCreatedAt())
                .updatedAt(mapping.getUpdatedAt())
                .build();
    }
}