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

import java.util.List;
import java.util.UUID;

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
    public PlatformSpec getPlatformSpecById(UUID id) {
        return platformSpecRepository.findAll().stream()
                .filter(spec -> id.equals(spec.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Platform spec does not exist"));
    }

    @Transactional
    public PlatformSpec createPlatformSpec(CreatePlatformSpecRequest request) {
        String category = normalizeCategory(request.getCategory());
        if (existsByNameAndCategory(request.getName(), category, null)) {
            throw new RuntimeException("Platform spec name already exists");
        }

        PlatformSpec spec = PlatformSpec.builder()
                .name(request.getName())
                .category(category)
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .remark(request.getRemark())
                .build();
        return platformSpecRepository.save(spec);
    }

    @Transactional
    public PlatformSpec updatePlatformSpec(UUID id, UpdatePlatformSpecRequest request) {
        PlatformSpec spec = getPlatformSpecById(id);

        String name = spec.getName();
        String category = spec.getCategory();
        Integer status = spec.getStatus();
        String remark = spec.getRemark();

        if (request.getName() != null && !request.getName().equals(spec.getName())) {
            name = request.getName();
        }

        if (request.getStatus() != null) {
            status = request.getStatus();
        }
        if (request.getCategory() != null) {
            category = normalizeCategory(request.getCategory());
        }
        if (existsByNameAndCategory(name, category, spec.getId())) {
            throw new RuntimeException("Platform spec name already exists");
        }
        if (request.getRemark() != null) {
            remark = request.getRemark();
        }

        int updated = platformSpecRepository.updateByNameAndCategory(spec.getName(), spec.getCategory(), name, category, status, remark);
        if (updated == 0) {
            throw new RuntimeException("Platform spec does not exist");
        }
        return getPlatformSpecById(id);
    }

    @Transactional
    public void deletePlatformSpec(UUID id) {
        PlatformSpec spec = getPlatformSpecById(id);
        if (specificationMappingRepository.findAll().stream()
                .anyMatch(mapping -> id.equals(mapping.getPlatformSpec().getId()))) {
            throw new BusinessException("Platform spec is in use and cannot be deleted");
        }
        int deleted = platformSpecRepository.deleteByNameAndCategory(spec.getName(), spec.getCategory());
        if (deleted == 0) {
            throw new RuntimeException("Platform spec does not exist");
        }
    }

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        String value = category.trim();
        if (!"公".equals(value) && !"母".equals(value)) {
            throw new BusinessException("Category must be 公 or 母");
        }
        return value;
    }

    private boolean existsByNameAndCategory(String name, String category, UUID excludedId) {
        return platformSpecRepository.findAll().stream()
                .filter(spec -> excludedId == null || !excludedId.equals(spec.getId()))
                .anyMatch(spec -> name.equals(spec.getName()) && categoryEquals(category, spec.getCategory()));
    }

    private boolean categoryEquals(String left, String right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }
}
