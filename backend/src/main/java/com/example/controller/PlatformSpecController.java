package com.example.controller;

import com.example.dto.*;
import com.example.entity.PlatformSpec;
import com.example.service.PlatformSpecService;
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
@RequestMapping("/api/platform-specs")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"})
public class PlatformSpecController {

    private final PlatformSpecService platformSpecService;

    @GetMapping
    public ResponseEntity<PageResponse<PlatformSpecDTO>> getPlatformSpecs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {

        Sort sort = Sort.by(sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<PlatformSpec> specPage = platformSpecService.getPlatformSpecs(name, status, pageRequest);

        PageResponse<PlatformSpecDTO> response = PageResponse.<PlatformSpecDTO>builder()
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
    public ResponseEntity<List<PlatformSpecDTO>> getAllPlatformSpecs() {
        List<PlatformSpec> specs = platformSpecService.getAllPlatformSpecs();
        List<PlatformSpecDTO> dtos = specs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlatformSpecDTO> getPlatformSpecById(@PathVariable UUID id) {
        PlatformSpec spec = platformSpecService.getPlatformSpecById(id);
        return ResponseEntity.ok(convertToDTO(spec));
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createPlatformSpec(@Valid @RequestBody CreatePlatformSpecRequest request) {
        platformSpecService.createPlatformSpec(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "平台规格创建成功");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updatePlatformSpec(@PathVariable UUID id, @Valid @RequestBody UpdatePlatformSpecRequest request) {
        platformSpecService.updatePlatformSpec(id, request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "平台规格更新成功");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePlatformSpec(@PathVariable UUID id) {
        platformSpecService.deletePlatformSpec(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "平台规格删除成功");
        return ResponseEntity.ok(response);
    }

    private PlatformSpecDTO convertToDTO(PlatformSpec spec) {
        return PlatformSpecDTO.builder()
                .id(spec.getId())
                .name(spec.getName())
                .status(spec.getStatus())
                .remark(spec.getRemark())
                .createdAt(spec.getCreatedAt())
                .updatedAt(spec.getUpdatedAt())
                .build();
    }
}