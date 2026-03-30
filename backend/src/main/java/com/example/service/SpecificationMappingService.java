package com.example.service;

import com.example.dto.CreateSpecificationMappingRequest;
import com.example.dto.UpdateSpecificationMappingRequest;
import com.example.entity.PlatformSpec;
import com.example.entity.PurchaseSpec;
import com.example.entity.SpecificationMapping;
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
                .orElseThrow(() -> new RuntimeException("规格映射不存在"));
    }

    @Transactional
    public SpecificationMapping createSpecificationMapping(CreateSpecificationMappingRequest request) {
        PurchaseSpec purchaseSpec = purchaseSpecRepository.findById(request.getPurchaseSpecId())
                .orElseThrow(() -> new RuntimeException("进货规格不存在"));
        PlatformSpec platformSpec = platformSpecRepository.findById(request.getPlatformSpecId())
                .orElseThrow(() -> new RuntimeException("平台规格不存在"));

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

        if (request.getPurchaseSpecId() != null) {
            PurchaseSpec purchaseSpec = purchaseSpecRepository.findById(request.getPurchaseSpecId())
                    .orElseThrow(() -> new RuntimeException("进货规格不存在"));
            mapping.setPurchaseSpec(purchaseSpec);
        }
        if (request.getPlatformSpecId() != null) {
            PlatformSpec platformSpec = platformSpecRepository.findById(request.getPlatformSpecId())
                    .orElseThrow(() -> new RuntimeException("平台规格不存在"));
            mapping.setPlatformSpec(platformSpec);
        }
        if (request.getStatus() != null) {
            mapping.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            mapping.setRemark(request.getRemark());
        }

        return specificationMappingRepository.save(mapping);
    }

    @Transactional
    public void deleteSpecificationMapping(UUID id) {
        SpecificationMapping mapping = getSpecificationMappingById(id);
        specificationMappingRepository.delete(mapping);
    }
}