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
        return platformSpecRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Platform spec does not exist"));
    }

    @Transactional
    public PlatformSpec createPlatformSpec(CreatePlatformSpecRequest request) {
        if (platformSpecRepository.existsByName(request.getName())) {
            throw new RuntimeException("Platform spec name already exists");
        }

        PlatformSpec spec = PlatformSpec.builder()
                .name(request.getName())
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .remark(request.getRemark())
                .build();
        return platformSpecRepository.save(spec);
    }

    @Transactional
    public PlatformSpec updatePlatformSpec(UUID id, UpdatePlatformSpecRequest request) {
        PlatformSpec spec = getPlatformSpecById(id);

        if (request.getName() != null && !request.getName().equals(spec.getName())) {
            if (platformSpecRepository.existsByName(request.getName())) {
                throw new RuntimeException("Platform spec name already exists");
            }
            spec.setName(request.getName());
        }

        if (request.getStatus() != null) {
            spec.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            spec.setRemark(request.getRemark());
        }

        return platformSpecRepository.save(spec);
    }

    @Transactional
    public void deletePlatformSpec(UUID id) {
        PlatformSpec spec = getPlatformSpecById(id);
        if (specificationMappingRepository.countByPlatformSpec_Id(id) > 0) {
            throw new BusinessException("Platform spec is in use and cannot be deleted");
        }
        platformSpecRepository.delete(spec);
    }
}
