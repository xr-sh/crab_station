package com.example.service;

import com.example.dto.CreatePurchaseSpecRequest;
import com.example.dto.PurchaseSpecPriceHistoryDTO;
import com.example.dto.UpdatePurchaseSpecRequest;
import com.example.entity.PurchaseSpec;
import com.example.entity.PurchaseSpecPriceHistory;
import com.example.exception.BusinessException;
import com.example.repository.PurchaseItemRepository;
import com.example.repository.PurchaseSpecPriceHistoryRepository;
import com.example.repository.PurchaseSpecRepository;
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
public class PurchaseSpecService {

    private final PurchaseSpecRepository purchaseSpecRepository;
    private final PurchaseSpecPriceHistoryRepository priceHistoryRepository;
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
        return purchaseSpecRepository.findAll().stream()
                .filter(spec -> id.equals(spec.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Purchase spec does not exist"));
    }

    @Transactional
    public PurchaseSpec createPurchaseSpec(CreatePurchaseSpecRequest request) {
        String category = normalizeCategory(request.getCategory());
        if (existsByNameAndCategory(request.getName(), category, null)) {
            throw new RuntimeException("Purchase spec name already exists");
        }

        PurchaseSpec spec = PurchaseSpec.builder()
                .name(request.getName())
                .category(category)
                .price(request.getPrice())
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .remark(request.getRemark())
                .build();
        PurchaseSpec saved = purchaseSpecRepository.save(spec);
        if (saved.getPrice() != null) {
            savePriceHistory(saved, null, saved.getPrice(), request.getPriceChangeReason());
        }
        return saved;
    }

    @Transactional
    public PurchaseSpec updatePurchaseSpec(UUID id, UpdatePurchaseSpecRequest request) {
        PurchaseSpec spec = getPurchaseSpecById(id);

        String name = spec.getName();
        String category = spec.getCategory();
        BigDecimal price = spec.getPrice();
        Integer status = spec.getStatus();
        String remark = spec.getRemark();
        boolean priceChanged = request.getPrice() != null && isPriceChanged(spec.getPrice(), request.getPrice());

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
            throw new RuntimeException("Purchase spec name already exists");
        }
        if (request.getPrice() != null) {
            price = request.getPrice();
        }
        if (request.getRemark() != null) {
            remark = request.getRemark();
        }

        int updated = purchaseSpecRepository.updateByNameAndCategory(spec.getName(), spec.getCategory(), name, category, price, status, remark);
        if (updated == 0) {
            throw new RuntimeException("Purchase spec does not exist");
        }
        PurchaseSpec updatedSpec = getPurchaseSpecById(id);
        if (priceChanged) {
            savePriceHistory(updatedSpec, spec.getPrice(), request.getPrice(), request.getPriceChangeReason());
        }
        return updatedSpec;
    }

    @Transactional(readOnly = true)
    public List<PurchaseSpecPriceHistoryDTO> getPriceHistory(UUID id) {
        PurchaseSpec spec = getPurchaseSpecById(id);
        return priceHistoryRepository.findAllByOrderByChangedAtDesc().stream()
                .filter(history -> id.equals(history.getPurchaseSpec().getId()))
                .map(history -> convertPriceHistoryToDTO(history, spec.getName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePurchaseSpec(UUID id) {
        PurchaseSpec spec = getPurchaseSpecById(id);
        if (purchaseItemRepository.findAll().stream()
                .anyMatch(item -> id.equals(item.getPurchaseSpec().getId()))
                || specificationMappingRepository.findAll().stream()
                .anyMatch(mapping -> id.equals(mapping.getPurchaseSpec().getId()))) {
            throw new BusinessException("Purchase spec is in use and cannot be deleted");
        }
        int deleted = purchaseSpecRepository.deleteByNameAndCategory(spec.getName(), spec.getCategory());
        if (deleted == 0) {
            throw new RuntimeException("Purchase spec does not exist");
        }
    }

    private boolean isPriceChanged(BigDecimal oldPrice, BigDecimal newPrice) {
        if (oldPrice == null) {
            return newPrice != null;
        }
        return newPrice == null || oldPrice.compareTo(newPrice) != 0;
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
        return purchaseSpecRepository.findAll().stream()
                .filter(spec -> excludedId == null || !excludedId.equals(spec.getId()))
                .anyMatch(spec -> name.equals(spec.getName()) && categoryEquals(category, spec.getCategory()));
    }

    private boolean categoryEquals(String left, String right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }

    private void savePriceHistory(PurchaseSpec spec, BigDecimal oldPrice, BigDecimal newPrice, String reason) {
        priceHistoryRepository.save(PurchaseSpecPriceHistory.builder()
                .purchaseSpec(spec)
                .oldPrice(oldPrice)
                .newPrice(newPrice)
                .changeReason(reason)
                .build());
    }

    private PurchaseSpecPriceHistoryDTO convertPriceHistoryToDTO(PurchaseSpecPriceHistory history, String specName) {
        return PurchaseSpecPriceHistoryDTO.builder()
                .id(history.getId())
                .purchaseSpecId(history.getPurchaseSpec().getId())
                .purchaseSpecName(specName)
                .oldPrice(history.getOldPrice())
                .newPrice(history.getNewPrice())
                .changeReason(history.getChangeReason())
                .changedBy(history.getChangedBy())
                .changedAt(history.getChangedAt())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
