package com.example.service;

import com.example.dto.CreatePurchaseSpecRequest;
import com.example.dto.UpdatePurchaseSpecRequest;
import com.example.entity.PurchaseSpec;
import com.example.exception.BusinessException;
import com.example.repository.PurchaseItemRepository;
import com.example.repository.PurchaseSpecRepository;
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
public class PurchaseSpecService {

    private final PurchaseSpecRepository purchaseSpecRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final SpecificationMappingRepository specificationMappingRepository;

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
                .orElseThrow(() -> new RuntimeException("Purchase spec does not exist"));
    }

    @Transactional
    public PurchaseSpec createPurchaseSpec(CreatePurchaseSpecRequest request) {
        if (purchaseSpecRepository.existsByName(request.getName())) {
            throw new RuntimeException("Purchase spec name already exists");
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
                throw new RuntimeException("Purchase spec name already exists");
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
        if (purchaseItemRepository.countByPurchaseSpec_Id(id) > 0
                || specificationMappingRepository.countByPurchaseSpec_Id(id) > 0) {
            throw new BusinessException("Purchase spec is in use and cannot be deleted");
        }
        purchaseSpecRepository.delete(spec);
    }
}
