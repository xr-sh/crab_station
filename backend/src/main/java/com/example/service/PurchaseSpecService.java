package com.example.service;

import com.example.dto.CreatePurchaseSpecRequest;
import com.example.dto.UpdatePurchaseSpecRequest;
import com.example.entity.PurchaseSpec;
import com.example.repository.PurchaseSpecRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseSpecService {

    private final PurchaseSpecRepository purchaseSpecRepository;

    @Transactional(readOnly = true)
    public Page<PurchaseSpec> getPurchaseSpecs(String name, Integer status, Pageable pageable) {
        return purchaseSpecRepository.findByFilters(name, status, pageable);
    }

    @Transactional(readOnly = true)
    public List<PurchaseSpec> getAllPurchaseSpecs() {
        return purchaseSpecRepository.findByStatusOrderByCreatedAtDesc(1);
    }

    @Transactional(readOnly = true)
    public PurchaseSpec getPurchaseSpecById(UUID id) {
        return purchaseSpecRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("进货规格不存在"));
    }

    @Transactional
    public PurchaseSpec createPurchaseSpec(CreatePurchaseSpecRequest request) {
        if (purchaseSpecRepository.existsByName(request.getName())) {
            throw new RuntimeException("进货规格名称已存在");
        }

        PurchaseSpec spec = PurchaseSpec.builder()
                .name(request.getName())
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .remark(request.getRemark())
                .build();
        return purchaseSpecRepository.save(spec);
    }

    @Transactional
    public PurchaseSpec updatePurchaseSpec(UUID id, UpdatePurchaseSpecRequest request) {
        PurchaseSpec spec = getPurchaseSpecById(id);

        if (request.getName() != null && !request.getName().equals(spec.getName())) {
            if (purchaseSpecRepository.existsByName(request.getName())) {
                throw new RuntimeException("进货规格名称已存在");
            }
            spec.setName(request.getName());
        }

        if (request.getStatus() != null) {
            spec.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            spec.setRemark(request.getRemark());
        }

        return purchaseSpecRepository.save(spec);
    }

    @Transactional
    public void deletePurchaseSpec(UUID id) {
        PurchaseSpec spec = getPurchaseSpecById(id);
        purchaseSpecRepository.delete(spec);
    }
}