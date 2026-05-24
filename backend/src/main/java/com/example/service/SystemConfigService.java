package com.example.service;

import com.example.dto.PlatformPackageCostConfigDTO;
import com.example.entity.SystemConfig;
import com.example.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private static final String PLATFORM_PACKAGE_FIXED_COST_KEY = "PLATFORM_PACKAGE_FIXED_COST";
    private static final BigDecimal DEFAULT_FIXED_COST = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final SystemConfigRepository systemConfigRepository;

    @Transactional(readOnly = true)
    public BigDecimal getPlatformPackageFixedCost() {
        return systemConfigRepository.findById(PLATFORM_PACKAGE_FIXED_COST_KEY)
                .map(SystemConfig::getConfigValue)
                .map(BigDecimal::new)
                .map(value -> value.setScale(2, RoundingMode.HALF_UP))
                .orElse(DEFAULT_FIXED_COST);
    }

    @Transactional(readOnly = true)
    public PlatformPackageCostConfigDTO getPlatformPackageCostConfig() {
        String remark = systemConfigRepository.findById(PLATFORM_PACKAGE_FIXED_COST_KEY)
                .map(SystemConfig::getRemark)
                .orElse("");
        return PlatformPackageCostConfigDTO.builder()
                .fixedCost(getPlatformPackageFixedCost())
                .remark(remark)
                .build();
    }

    @Transactional
    public PlatformPackageCostConfigDTO updatePlatformPackageFixedCost(BigDecimal fixedCost, String remark) {
        BigDecimal normalizedValue = fixedCost.setScale(2, RoundingMode.HALF_UP);
        SystemConfig config = systemConfigRepository.findById(PLATFORM_PACKAGE_FIXED_COST_KEY)
                .orElse(SystemConfig.builder()
                        .configKey(PLATFORM_PACKAGE_FIXED_COST_KEY)
                        .remark("Platform package fixed cost for labor and packaging")
                        .build());
        config.setConfigValue(normalizedValue.toPlainString());
        config.setRemark(remark);
        systemConfigRepository.save(config);
        return PlatformPackageCostConfigDTO.builder()
                .fixedCost(normalizedValue)
                .remark(config.getRemark())
                .build();
    }
}
