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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
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
                .orElseThrow(() -> new BusinessException("Specification mapping does not exist"));
    }

    @Transactional
    public SpecificationMapping createSpecificationMapping(CreateSpecificationMappingRequest request) {
        if (specificationMappingRepository.existsByPurchaseSpec_IdAndPlatformSpec_Id(
                request.getPurchaseSpecId(), request.getPlatformSpecId())) {
            throw new BusinessException("Specification mapping already exists");
        }

        SpecificationMapping mapping = SpecificationMapping.builder()
                .purchaseSpec(getPurchaseSpec(request.getPurchaseSpecId()))
                .platformSpec(getPlatformSpec(request.getPlatformSpecId()))
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .remark(request.getRemark())
                .build();
        return specificationMappingRepository.save(mapping);
    }

    @Transactional
    public SpecificationMapping updateSpecificationMapping(UUID id, UpdateSpecificationMappingRequest request) {
        SpecificationMapping mapping = getSpecificationMappingById(id);

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

        specificationMappingRepository.findByPurchaseSpec_IdAndPlatformSpec_Id(
                        mapping.getPurchaseSpec().getId(), mapping.getPlatformSpec().getId())
                .filter(existing -> !existing.getId().equals(mapping.getId()))
                .ifPresent(existing -> {
                    throw new BusinessException("Specification mapping already exists");
                });

        return specificationMappingRepository.save(mapping);
    }

    @Transactional
    public void deleteSpecificationMapping(UUID id) {
        specificationMappingRepository.delete(getSpecificationMappingById(id));
    }

    private PurchaseSpec getPurchaseSpec(UUID id) {
        return purchaseSpecRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Purchase spec does not exist"));
    }

    private PlatformSpec getPlatformSpec(UUID id) {
        return platformSpecRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Platform spec does not exist"));
    }
}
