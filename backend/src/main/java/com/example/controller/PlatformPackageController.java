package com.example.controller;

import com.example.dto.CreatePlatformPackageRequest;
import com.example.dto.PageResponse;
import com.example.dto.PlatformPackageDTO;
import com.example.dto.UpdatePlatformPackageRequest;
import com.example.entity.PlatformPackage;
import com.example.service.PlatformPackageService;
import com.example.util.PageRequestUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/platform-packages")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"})
public class PlatformPackageController {

    private final PlatformPackageService platformPackageService;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "updatedAt", "name", "status");

    @GetMapping
    public ResponseEntity<PageResponse<PlatformPackageDTO>> getPlatformPackages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {

        PageRequest pageRequest = PageRequestUtils.of(page, size, sortBy, sortDirection, ALLOWED_SORT_FIELDS);
        Page<PlatformPackage> packagePage = platformPackageService.getPlatformPackages(name, status, pageRequest);

        PageResponse<PlatformPackageDTO> response = PageResponse.<PlatformPackageDTO>builder()
                .content(packagePage.getContent().stream()
                        .map(pkg -> platformPackageService.getPlatformPackageById(pkg.getId()))
                        .collect(Collectors.toList()))
                .totalPages(packagePage.getTotalPages())
                .totalElements(packagePage.getTotalElements())
                .size(packagePage.getSize())
                .number(packagePage.getNumber())
                .first(packagePage.isFirst())
                .last(packagePage.isLast())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PlatformPackageDTO>> getAllPlatformPackages() {
        return ResponseEntity.ok(platformPackageService.getAllPlatformPackages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlatformPackageDTO> getPlatformPackageById(@PathVariable UUID id) {
        return ResponseEntity.ok(platformPackageService.getPlatformPackageById(id));
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createPlatformPackage(@Valid @RequestBody CreatePlatformPackageRequest request) {
        platformPackageService.createPlatformPackage(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "平台套餐创建成功");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updatePlatformPackage(@PathVariable UUID id, @Valid @RequestBody UpdatePlatformPackageRequest request) {
        platformPackageService.updatePlatformPackage(id, request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "平台套餐更新成功");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePlatformPackage(@PathVariable UUID id) {
        log.info("Deleting platform package, path id={}", id);
        platformPackageService.deletePlatformPackage(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "平台套餐删除成功");
        return ResponseEntity.ok(response);
    }
}
