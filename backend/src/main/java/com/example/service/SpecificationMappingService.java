package com.example.service;

import com.example.dto.CreateSpecificationMappingRequest;
import com.example.dto.UpdateSpecificationMappingRequest;
import com.example.entity.PlatformSpec;
import com.example.entity.PurchaseSpec;
import com.example.entity.SpecificationMapping;
import com.example.exception.BusinessException;
import com.example.repository.PlatformSpecRepository;
import com.example.repository.PurchaseSpecRepository;
import com.example.repository.SpecificationMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpecificationMappingService {

    private final SpecificationMappingRepository specificationMappingRepository;
    private final PurchaseSpecRepository purchaseSpecRepository;
    private final PlatformSpecRepository platformSpecRepository;

    @Transactional(readOnly = true)
    public Page<SpecificationMapping> getSpecificationMappings(String purchaseSpecName, String platformSpecName, Integer status, Pageable pageable) {
        return specificationMappingRepository.findByFilters(purchaseSpecName, platformSpecName, status, pageable);
    }

    @Transactional(readOnly = true)
    public SpecificationMapping getSpecificationMappingById(UUID id) {
        return specificationMappingRepository.findById(id)
                .or(() -> specificationMappingRepository.findAll().stream()
                        .filter(mapping -> id.equals(mapping.getId()))
                        .findFirst())
                .orElseThrow(() -> new BusinessException("规格映射不存在"));
    }

    @Transactional
    public SpecificationMapping createSpecificationMapping(CreateSpecificationMappingRequest request) {
        log.info("Creating specification mapping, category={}, purchaseSpecId={}, platformSpecId={}",
                request.getCategory(), request.getPurchaseSpecId(), request.getPlatformSpecId());
        PurchaseSpec purchaseSpec = getPurchaseSpec(request.getPurchaseSpecId());
        PlatformSpec platformSpec = getPlatformSpec(request.getPlatformSpecId());
        validateCategoryMatch(request.getCategory(), purchaseSpec, platformSpec);

        if (mappingExists(purchaseSpec.getId(), platformSpec.getId(), null)) {
            throw new BusinessException("规格映射已存在");
        }

        SpecificationMapping mapping = SpecificationMapping.builder()
                .purchaseSpec(purchaseSpec)
                .platformSpec(platformSpec)
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .remark(request.getRemark())
                .build();
        return specificationMappingRepository.save(mapping);
    }

    @Transactional
    public SpecificationMapping updateSpecificationMapping(UUID id, UpdateSpecificationMappingRequest request) {
        SpecificationMapping mapping = getSpecificationMappingById(id);
        String category = request.getCategory() != null
                ? normalizeCategory(request.getCategory())
                : mapping.getPurchaseSpec().getCategory();

        if (request.getPurchaseSpecId() != null) {
            mapping.setPurchaseSpec(getPurchaseSpec(request.getPurchaseSpecId()));
        }
        if (request.getPlatformSpecId() != null) {
            mapping.setPlatformSpec(getPlatformSpec(request.getPlatformSpecId()));
        }
        if (request.getStatus() != null) {
            mapping.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            mapping.setRemark(request.getRemark());
        }
        validateCategoryMatch(category, mapping.getPurchaseSpec(), mapping.getPlatformSpec());

        if (mappingExists(mapping.getPurchaseSpec().getId(), mapping.getPlatformSpec().getId(), mapping.getId())) {
            throw new BusinessException("规格映射已存在");
        }

        return specificationMappingRepository.save(mapping);
    }

    @Transactional
    public void deleteSpecificationMapping(UUID id) {
        specificationMappingRepository.delete(getSpecificationMappingById(id));
    }

    private PurchaseSpec getPurchaseSpec(UUID id) {
        return purchaseSpecRepository.findById(id).orElseGet(() -> {
            log.warn("Purchase spec not found by id={}, existingIds={}", id,
                    purchaseSpecRepository.findAll().stream()
                            .map(PurchaseSpec::getId)
                            .collect(Collectors.toList()));
            return purchaseSpecRepository.findAll().stream()
                    .filter(spec -> id.equals(spec.getId()))
                    .findFirst()
                    .orElse(null);
        });
    }

    private PlatformSpec getPlatformSpec(UUID id) {
        return platformSpecRepository.findById(id).orElseGet(() -> {
            log.warn("Platform spec not found by id={}, existingIds={}", id,
                    platformSpecRepository.findAll().stream()
                            .map(PlatformSpec::getId)
                            .collect(Collectors.toList()));
            return platformSpecRepository.findAll().stream()
                    .filter(spec -> id.equals(spec.getId()))
                    .findFirst()
                    .orElse(null);
        });
    }

    private boolean mappingExists(UUID purchaseSpecId, UUID platformSpecId, UUID excludedMappingId) {
        return specificationMappingRepository.findAll().stream()
                .filter(mapping -> excludedMappingId == null || !excludedMappingId.equals(mapping.getId()))
                .anyMatch(mapping -> purchaseSpecId.equals(mapping.getPurchaseSpec().getId())
                        && platformSpecId.equals(mapping.getPlatformSpec().getId()));
    }

    private void validateCategoryMatch(String requestedCategory, PurchaseSpec purchaseSpec, PlatformSpec platformSpec) {
        if (purchaseSpec == null) {
            throw new BusinessException("进货规格不存在");
        }
        if (platformSpec == null) {
            throw new BusinessException("平台规格不存在");
        }
        String category = normalizeCategory(requestedCategory);
        if (!category.equals(purchaseSpec.getCategory()) || !category.equals(platformSpec.getCategory())) {
            throw new BusinessException("规格映射类别必须和进货规格、平台规格类别一致");
        }
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
}
