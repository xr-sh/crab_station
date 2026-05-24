package com.example.service;

import com.example.dto.CreatePlatformSpecRequest;
import com.example.dto.UpdatePlatformSpecRequest;
import com.example.entity.PlatformSpec;
import com.example.exception.BusinessException;
import com.example.repository.PlatformSpecRepository;
import com.example.repository.SpecificationMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlatformSpecService {

    private final PlatformSpecRepository platformSpecRepository;
    private final SpecificationMappingRepository specificationMappingRepository;

    @Transactional(readOnly = true)
    public Page<PlatformSpec> getPlatformSpecs(String name, Integer status, Pageable pageable) {
        return platformSpecRepository.findByFilters(name, status, pageable);
    }

    @Transactional(readOnly = true)
    public List<PlatformSpec> getAllPlatformSpecs() {
        return platformSpecRepository.findByStatusOrderByCreatedAtDesc(1);
    }

    @Transactional(readOnly = true)
    public List<PlatformSpec> getActivePlatformSpecs(String category) {
        return platformSpecRepository.findByStatusOrderByCreatedAtDesc(1).stream()
                .filter(spec -> category == null || category.isBlank() || category.equals(spec.getCategory()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlatformSpec getPlatformSpecById(UUID id) {
        return platformSpecRepository.findById(id)
                .orElseThrow(() -> new BusinessException("平台规格不存在"));
    }

    @Transactional
    public PlatformSpec createPlatformSpec(CreatePlatformSpecRequest request) {
        String name = normalizeSpecRange(request.getName());
        String category = normalizeCategory(request.getCategory());
        if (existsByNameAndCategory(name, category, null)) {
            throw new BusinessException("平台规格名称已存在");
        }

        PlatformSpec spec = PlatformSpec.builder()
                .name(name)
                .category(category)
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .remark(request.getRemark())
                .build();
        return platformSpecRepository.save(spec);
    }

    @Transactional
    public PlatformSpec updatePlatformSpec(UUID id, UpdatePlatformSpecRequest request) {
        PlatformSpec spec = getPlatformSpecById(id);

        if (request.getName() != null) {
            spec.setName(normalizeSpecRange(request.getName()));
        }
        if (request.getCategory() != null) {
            spec.setCategory(normalizeCategory(request.getCategory()));
        }
        if (request.getStatus() != null) {
            spec.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            spec.setRemark(request.getRemark());
        }

        if (existsByNameAndCategory(spec.getName(), spec.getCategory(), spec.getId())) {
            throw new BusinessException("平台规格名称已存在");
        }
        return platformSpecRepository.save(spec);
    }

    @Transactional
    public void deletePlatformSpec(UUID id) {
        PlatformSpec spec = getPlatformSpecById(id);
        if (specificationMappingRepository.countByPlatformSpec_Id(id) > 0) {
            throw new BusinessException("平台规格已被使用，不能删除");
        }
        platformSpecRepository.delete(spec);
    }

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new BusinessException("类别不能为空");
        }
        String value = category.trim();
        if (!"公".equals(value) && !"母".equals(value)) {
            throw new BusinessException("类别只能是公或母");
        }
        return value;
    }

    private String normalizeSpecRange(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("规格范围不能为空");
        }
        String value = name.trim();
        if (!value.matches("\\d+(\\.\\d+)?-\\d+(\\.\\d+)?")) {
            throw new BusinessException("规格范围格式必须为数字-数字，例如2.3-2.6");
        }
        String[] parts = value.split("-");
        BigDecimal min = new BigDecimal(parts[0]);
        BigDecimal max = new BigDecimal(parts[1]);
        if (min.compareTo(max) >= 0) {
            throw new BusinessException("规格范围左侧数值必须小于右侧数值");
        }
        return value;
    }

    private boolean existsByNameAndCategory(String name, String category, UUID excludedId) {
        return platformSpecRepository.findAll().stream()
                .filter(spec -> excludedId == null || !excludedId.equals(spec.getId()))
                .anyMatch(spec -> name.equals(spec.getName()) && category.equals(spec.getCategory()));
    }
}
