package com.example.service;

import com.example.dto.CreatePlatformSpecRequest;
import com.example.dto.UpdatePlatformSpecRequest;
import com.example.entity.PlatformSpec;
import com.example.repository.PlatformSpecRepository;
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
                .orElseThrow(() -> new RuntimeException("平台规格不存在"));
    }

    @Transactional
    public PlatformSpec createPlatformSpec(CreatePlatformSpecRequest request) {
        if (platformSpecRepository.existsByName(request.getName())) {
            throw new RuntimeException("平台规格名称已存在");
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
                throw new RuntimeException("平台规格名称已存在");
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
        platformSpecRepository.delete(spec);
    }
}