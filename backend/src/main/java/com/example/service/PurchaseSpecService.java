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
    private final PlatformPackageService platformPackageService;

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
                .orElseThrow(() -> new BusinessException("进货规格不存在"));
    }

    @Transactional
    public PurchaseSpec createPurchaseSpec(CreatePurchaseSpecRequest request) {
        String name = normalizeSpecRange(request.getName());
        String category = normalizeCategory(request.getCategory());
        if (existsByNameAndCategory(name, category, null)) {
            throw new BusinessException("进货规格名称已存在");
        }

        PurchaseSpec spec = PurchaseSpec.builder()
                .name(name)
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
        BigDecimal oldPrice = spec.getPrice();

        if (request.getName() != null) {
            spec.setName(normalizeSpecRange(request.getName()));
        }
        if (request.getCategory() != null) {
            spec.setCategory(normalizeCategory(request.getCategory()));
        }
        if (request.getPrice() != null) {
            spec.setPrice(request.getPrice());
        }
        if (request.getStatus() != null) {
            spec.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            spec.setRemark(request.getRemark());
        }

        if (existsByNameAndCategory(spec.getName(), spec.getCategory(), spec.getId())) {
            throw new BusinessException("进货规格名称已存在");
        }

        PurchaseSpec saved = purchaseSpecRepository.save(spec);
        if (request.getPrice() != null && isPriceChanged(oldPrice, request.getPrice())) {
            savePriceHistory(saved, oldPrice, request.getPrice(), request.getPriceChangeReason());
            platformPackageService.refreshCostsByPurchaseSpec(saved.getId());
        }
        return saved;
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
        if (purchaseItemRepository.countByPurchaseSpec_Id(id) > 0
                || specificationMappingRepository.countByPurchaseSpec_Id(id) > 0) {
            throw new BusinessException("进货规格已被使用，不能删除");
        }
        purchaseSpecRepository.delete(spec);
    }

    private boolean isPriceChanged(BigDecimal oldPrice, BigDecimal newPrice) {
        if (oldPrice == null) {
            return newPrice != null;
        }
        return newPrice == null || oldPrice.compareTo(newPrice) != 0;
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
        return purchaseSpecRepository.findAll().stream()
                .filter(spec -> excludedId == null || !excludedId.equals(spec.getId()))
                .anyMatch(spec -> name.equals(spec.getName()) && category.equals(spec.getCategory()));
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
