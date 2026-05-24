package com.example.service;

import com.example.dto.CreatePlatformPackageRequest;
import com.example.dto.PlatformPackageDTO;
import com.example.dto.PlatformPackageItemDTO;
import com.example.dto.PlatformPackagePriceRuleDTO;
import com.example.dto.UpdatePlatformPackageRequest;
import com.example.entity.PlatformPackage;
import com.example.entity.PlatformPackageItem;
import com.example.entity.PlatformPackagePriceRule;
import com.example.entity.PlatformSpec;
import com.example.entity.PurchaseSpec;
import com.example.entity.SpecificationMapping;
import com.example.exception.BusinessException;
import com.example.repository.PlatformPackageRepository;
import com.example.repository.PlatformSpecRepository;
import com.example.repository.SpecificationMappingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformPackageService {

    private final PlatformPackageRepository platformPackageRepository;
    private final PlatformSpecRepository platformSpecRepository;
    private final SpecificationMappingRepository specificationMappingRepository;
    private final SystemConfigService systemConfigService;

    @Transactional(readOnly = true)
    public Page<PlatformPackage> getPlatformPackages(String name, Integer status, Pageable pageable) {
        return platformPackageRepository.findByFilters(name, status, pageable);
    }

    @Transactional(readOnly = true)
    public List<PlatformPackageDTO> getAllPlatformPackages() {
        return platformPackageRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlatformPackageDTO getPlatformPackageById(UUID id) {
        return convertToDTO(getEntityById(id));
    }

    @Transactional
    public PlatformPackageDTO createPlatformPackage(CreatePlatformPackageRequest request) {
        PlatformPackage platformPackage = PlatformPackage.builder()
                .price(request.getPrice())
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .remark(request.getRemark())
                .build();
        return savePackage(platformPackage, request.getSelections());
    }

    @Transactional
    public PlatformPackageDTO updatePlatformPackage(UUID id, UpdatePlatformPackageRequest request) {
        PlatformPackage platformPackage = getEntityById(id);
        if (request.getPrice() != null) {
            platformPackage.setPrice(request.getPrice());
        }
        if (request.getStatus() != null) {
            platformPackage.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            platformPackage.setRemark(request.getRemark());
        }
        return savePackage(platformPackage, request.getSelections());
    }

    @Transactional
    public void deletePlatformPackage(UUID id) {
        PlatformPackage platformPackage = getEntityById(id);
        platformPackageRepository.delete(platformPackage);
    }

    @Transactional
    public int refreshCostsByPurchaseSpec(UUID purchaseSpecId) {
        List<SpecificationMapping> mappings = specificationMappingRepository.findAll().stream()
                .filter(mapping -> mapping.getStatus() != null && mapping.getStatus() == 1)
                .filter(mapping -> isMappedToPurchaseSpec(mapping, purchaseSpecId))
                .collect(Collectors.toList());
        if (mappings.isEmpty()) {
            return 0;
        }

        Map<UUID, PurchaseSpec> purchaseSpecByPlatformSpecId = mappings.stream()
                .filter(mapping -> resolveMappingPlatformSpecId(mapping).isPresent())
                .collect(Collectors.toMap(
                        mapping -> resolveMappingPlatformSpecId(mapping).orElseThrow(),
                        SpecificationMapping::getPurchaseSpec,
                        (left, right) -> left));
        Set<UUID> platformSpecIds = purchaseSpecByPlatformSpecId.keySet();
        if (platformSpecIds.isEmpty()) {
            return 0;
        }

        int refreshedCount = 0;
        for (PlatformPackage platformPackage : platformPackageRepository.findAll()) {
            List<PlatformPackageItem> items = sortPackageItems(platformPackage);
            List<PlatformPackagePriceRule> priceRules = sortPackagePriceRules(platformPackage);

            for (int index = 0; index < items.size(); index++) {
                PlatformPackageItem item = items.get(index);
                Optional<UUID> itemPlatformSpecId = resolvePlatformSpecIdOptional(item);
                if (itemPlatformSpecId.isEmpty() || !platformSpecIds.contains(itemPlatformSpecId.get())) {
                    continue;
                }

                PlatformPackagePriceRule rule = findMatchingPriceRule(priceRules, item, index);
                if (rule == null || "MANUAL".equalsIgnoreCase(rule.getCostMode())) {
                    continue;
                }

                PurchaseSpec purchaseSpec = purchaseSpecByPlatformSpecId.get(itemPlatformSpecId.get());
                rule.setCostPrice(calculatePurchaseSpecUnitCost(purchaseSpec)
                        .multiply(BigDecimal.valueOf(rule.getQty()))
                        .setScale(2, RoundingMode.HALF_UP));
                rule.setCostMode("AUTO");
                refreshedCount++;
            }
        }

        log.info("Refreshed platform package costs by purchase spec, purchaseSpecId={}, refreshedRules={}",
                purchaseSpecId, refreshedCount);
        return refreshedCount;
    }

    private PlatformPackageDTO savePackage(PlatformPackage platformPackage,
                                           List<CreatePlatformPackageRequest.SelectionRequest> selections) {
        validateSelections(selections);
        List<CreatePlatformPackageRequest.SelectionRequest> normalizedSelections = selections.stream()
                .map(this::normalizeSelection)
                .collect(Collectors.toList());

        platformPackage.setCategory(null);
        platformPackage.setName(generatePackageName(normalizedSelections));
        platformPackage.setSpecCount(normalizedSelections.size());
        platformPackage.getItems().clear();
        platformPackage.getPriceRules().clear();

        for (int index = 0; index < normalizedSelections.size(); index++) {
            CreatePlatformPackageRequest.SelectionRequest selection = normalizedSelections.get(index);
            PlatformSpec spec = getEnabledPlatformSpec(selection.getPlatformSpecId());
            BigDecimal costPrice = calculatePackageItemCost(spec, selection.getQty());

            platformPackage.getItems().add(PlatformPackageItem.builder()
                    .platformPackage(platformPackage)
                    .platformSpec(spec)
                    .platformSpecName(spec.getName())
                    .platformSpecCategory(selection.getCategory())
                    .sortNo(index)
                    .build());

            platformPackage.getPriceRules().add(PlatformPackagePriceRule.builder()
                    .platformPackage(platformPackage)
                    .qty(selection.getQty())
                    .salePrice(platformPackage.getPrice())
                    .costPrice(costPrice)
                    .costMode("AUTO")
                    .sortNo(index)
                    .build());
        }

        return convertToDTO(platformPackageRepository.save(platformPackage));
    }

    private void validateSelections(List<CreatePlatformPackageRequest.SelectionRequest> selections) {
        if (selections == null || selections.isEmpty() || selections.size() > 2) {
            throw new BusinessException("套餐规格数量只能为1到2个");
        }
        if (selections.stream().anyMatch(selection -> selection.getQty() == null)) {
            throw new BusinessException("只数不能为空");
        }
        if (selections.stream().anyMatch(selection -> selection.getQty() <= 0)) {
            throw new BusinessException("只数必须大于0");
        }
    }

    private CreatePlatformPackageRequest.SelectionRequest normalizeSelection(CreatePlatformPackageRequest.SelectionRequest selection) {
        if (selection.getCategory() == null || selection.getCategory().isBlank()) {
            throw new BusinessException("类别不能为空");
        }
        if (selection.getPlatformSpecId() == null) {
            throw new BusinessException("平台规格不能为空");
        }
        selection.setCategory(selection.getCategory().trim());
        return selection;
    }

    private PlatformSpec getEnabledPlatformSpec(UUID id) {
        PlatformSpec spec = platformSpecRepository.findById(id)
                .orElseGet(() -> platformSpecRepository.findAll().stream()
                        .filter(item -> id.equals(item.getId()))
                        .findFirst()
                        .orElse(null));
        if (spec == null) {
            throw new BusinessException("平台规格不存在");
        }
        if (spec.getStatus() == null || spec.getStatus() != 1) {
            throw new BusinessException("只能选择启用状态的平台规格");
        }
        return spec;
    }

    private BigDecimal calculatePackageItemCost(PlatformSpec platformSpec, Integer qty) {
        SpecificationMapping mapping = getEnabledMapping(platformSpec);
        PurchaseSpec purchaseSpec = mapping.getPurchaseSpec();
        if (purchaseSpec == null) {
            throw new BusinessException("平台规格未配置进货规格映射，无法计算成本");
        }
        if (purchaseSpec.getStatus() == null || purchaseSpec.getStatus() != 1) {
            throw new BusinessException("映射的进货规格未启用，无法计算成本");
        }
        if (purchaseSpec.getPrice() == null) {
            throw new BusinessException("映射的进货规格未配置价格，无法计算成本");
        }
        BigDecimal unitCost = calculatePurchaseSpecUnitCost(purchaseSpec);
        return unitCost.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);
    }

    private SpecificationMapping getEnabledMapping(PlatformSpec platformSpec) {
        return specificationMappingRepository.findAll().stream()
                .filter(mapping -> mapping.getStatus() != null && mapping.getStatus() == 1)
                .filter(mapping -> {
                    try {
                        return mapping.getPlatformSpec() != null && platformSpec.getId().equals(mapping.getPlatformSpec().getId());
                    } catch (EntityNotFoundException error) {
                        return false;
                    }
                })
                .findFirst()
                .orElseThrow(() -> new BusinessException("平台规格未配置进货规格映射，无法计算成本"));
    }

    private BigDecimal calculatePurchaseSpecUnitCost(PurchaseSpec purchaseSpec) {
        BigDecimal midpoint = parseSpecRangeMidpoint(purchaseSpec.getName());
        return midpoint.multiply(purchaseSpec.getPrice())
                .divide(BigDecimal.TEN, 4, RoundingMode.HALF_UP);
    }

    private boolean isMappedToPurchaseSpec(SpecificationMapping mapping, UUID purchaseSpecId) {
        try {
            return mapping.getPurchaseSpec() != null && purchaseSpecId.equals(mapping.getPurchaseSpec().getId());
        } catch (EntityNotFoundException error) {
            return false;
        }
    }

    private Optional<UUID> resolveMappingPlatformSpecId(SpecificationMapping mapping) {
        try {
            return mapping.getPlatformSpec() == null ? Optional.empty() : Optional.of(mapping.getPlatformSpec().getId());
        } catch (EntityNotFoundException error) {
            return Optional.empty();
        }
    }

    private List<PlatformPackageItem> sortPackageItems(PlatformPackage platformPackage) {
        return new ArrayList<>(platformPackage.getItems()).stream()
                .sorted(Comparator
                        .comparing((PlatformPackageItem item) -> item.getSortNo() == null ? 0 : item.getSortNo())
                        .thenComparing(PlatformPackageItem::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    private List<PlatformPackagePriceRule> sortPackagePriceRules(PlatformPackage platformPackage) {
        return new ArrayList<>(platformPackage.getPriceRules()).stream()
                .sorted(Comparator
                        .comparing((PlatformPackagePriceRule rule) -> rule.getSortNo() == null ? 0 : rule.getSortNo())
                        .thenComparing(PlatformPackagePriceRule::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    private PlatformPackagePriceRule findMatchingPriceRule(List<PlatformPackagePriceRule> priceRules,
                                                           PlatformPackageItem item,
                                                           int index) {
        if (item.getSortNo() != null) {
            Optional<PlatformPackagePriceRule> rule = priceRules.stream()
                    .filter(priceRule -> item.getSortNo().equals(priceRule.getSortNo()))
                    .findFirst();
            if (rule.isPresent()) {
                return rule.get();
            }
        }
        return index < priceRules.size() ? priceRules.get(index) : null;
    }

    private BigDecimal parseSpecRangeMidpoint(String specName) {
        if (specName == null || !specName.matches("\\d+(\\.\\d+)?-\\d+(\\.\\d+)?")) {
            throw new BusinessException("进货规格范围格式异常，无法计算成本");
        }
        String[] parts = specName.split("-");
        BigDecimal min = new BigDecimal(parts[0]);
        BigDecimal max = new BigDecimal(parts[1]);
        if (min.compareTo(max) >= 0) {
            throw new BusinessException("进货规格范围左侧数值必须小于右侧数值");
        }
        return min.add(max).divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
    }

    private String generatePackageName(List<CreatePlatformPackageRequest.SelectionRequest> selections) {
        return selections.stream()
                .map(selection -> {
                    PlatformSpec spec = getEnabledPlatformSpec(selection.getPlatformSpecId());
                    return spec.getName() + selection.getCategory() + selection.getQty() + "只";
                })
                .collect(Collectors.joining(", "));
    }

    private PlatformPackage getEntityById(UUID id) {
        return platformPackageRepository.findById(id)
                .orElseGet(() -> platformPackageRepository.findAll().stream()
                        .filter(item -> id.equals(item.getId()))
                        .findFirst()
                        .orElseThrow(() -> new BusinessException("平台套餐不存在")));
    }

    private PlatformPackageDTO convertToDTO(PlatformPackage platformPackage) {
        List<PlatformPackageItemDTO> items = new ArrayList<>(platformPackage.getItems()).stream()
                .sorted(Comparator
                        .comparing((PlatformPackageItem item) -> item.getSortNo() == null ? 0 : item.getSortNo())
                        .thenComparing(PlatformPackageItem::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(item -> PlatformPackageItemDTO.builder()
                        .id(item.getId())
                        .platformSpecId(resolvePlatformSpecId(item))
                        .platformSpecName(resolvePlatformSpecName(item))
                        .platformSpecCategory(resolvePlatformSpecCategory(item))
                        .sortNo(item.getSortNo())
                        .createdAt(item.getCreatedAt())
                        .updatedAt(item.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        List<PlatformPackagePriceRuleDTO> priceRules = new ArrayList<>(platformPackage.getPriceRules()).stream()
                .sorted(Comparator
                        .comparing((PlatformPackagePriceRule rule) -> rule.getSortNo() == null ? 0 : rule.getSortNo())
                        .thenComparing(PlatformPackagePriceRule::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(rule -> PlatformPackagePriceRuleDTO.builder()
                        .id(rule.getId())
                        .qty(rule.getQty())
                        .salePrice(rule.getSalePrice())
                        .costPrice(rule.getCostPrice())
                        .costMode(rule.getCostMode())
                        .sortNo(rule.getSortNo())
                        .createdAt(rule.getCreatedAt())
                        .updatedAt(rule.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return PlatformPackageDTO.builder()
                .id(platformPackage.getId())
                .name(platformPackage.getName())
                .price(platformPackage.getPrice())
                .fixedCost(systemConfigService.getPlatformPackageFixedCost())
                .totalCost(calculateTotalCost(priceRules))
                .status(platformPackage.getStatus())
                .remark(platformPackage.getRemark())
                .items(items)
                .priceRules(priceRules)
                .specCount(platformPackage.getSpecCount() == null ? 0 : platformPackage.getSpecCount())
                .createdAt(platformPackage.getCreatedAt())
                .updatedAt(platformPackage.getUpdatedAt())
                .build();
    }

    private BigDecimal calculateTotalCost(List<PlatformPackagePriceRuleDTO> priceRules) {
        BigDecimal purchaseCost = priceRules.stream()
                .map(PlatformPackagePriceRuleDTO::getCostPrice)
                .filter(costPrice -> costPrice != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return purchaseCost.add(systemConfigService.getPlatformPackageFixedCost())
                .setScale(2, RoundingMode.HALF_UP);
    }

    private UUID resolvePlatformSpecId(PlatformPackageItem item) {
        return resolvePlatformSpecIdOptional(item).orElse(null);
    }

    private Optional<UUID> resolvePlatformSpecIdOptional(PlatformPackageItem item) {
        try {
            return item.getPlatformSpec() == null ? Optional.empty() : Optional.of(item.getPlatformSpec().getId());
        } catch (EntityNotFoundException error) {
            return Optional.empty();
        }
    }

    private String resolvePlatformSpecName(PlatformPackageItem item) {
        if (item.getPlatformSpecName() != null && !item.getPlatformSpecName().isBlank()) {
            return item.getPlatformSpecName();
        }
        return resolvePlatformSpec(item)
                .map(PlatformSpec::getName)
                .orElse("平台规格不存在");
    }

    private String resolvePlatformSpecCategory(PlatformPackageItem item) {
        if (item.getPlatformSpecCategory() != null && !item.getPlatformSpecCategory().isBlank()) {
            return item.getPlatformSpecCategory();
        }
        return resolvePlatformSpec(item)
                .map(PlatformSpec::getCategory)
                .orElse("");
    }

    private Optional<PlatformSpec> resolvePlatformSpec(PlatformPackageItem item) {
        try {
            return Optional.ofNullable(item.getPlatformSpec());
        } catch (EntityNotFoundException error) {
            return Optional.empty();
        }
    }
}
