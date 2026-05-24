package com.example.controller;

import com.example.dto.PlatformPackageCostConfigDTO;
import com.example.dto.UpdatePlatformPackageCostConfigRequest;
import com.example.service.SystemConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system-configs")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"})
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping("/platform-package-cost")
    public ResponseEntity<PlatformPackageCostConfigDTO> getPlatformPackageCostConfig() {
        return ResponseEntity.ok(systemConfigService.getPlatformPackageCostConfig());
    }

    @PutMapping("/platform-package-cost")
    public ResponseEntity<PlatformPackageCostConfigDTO> updatePlatformPackageCostConfig(
            @Valid @RequestBody UpdatePlatformPackageCostConfigRequest request) {
        return ResponseEntity.ok(systemConfigService.updatePlatformPackageFixedCost(
                request.getFixedCost(),
                request.getRemark()));
    }
}
