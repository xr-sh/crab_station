package com.example.service;

import com.example.dto.*;
import com.example.entity.PurchaseItem;
import com.example.entity.PurchaseRecord;
import com.example.entity.PurchaseSpec;
import com.example.repository.PurchaseItemRepository;
import com.example.repository.PurchaseRecordRepository;
import com.example.repository.PurchaseSpecRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRecordRepository purchaseRecordRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final PurchaseSpecRepository purchaseSpecRepository;

    @Transactional(readOnly = true)
    public Page<PurchaseRecord> getPurchaseRecords(String supplier, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return purchaseRecordRepository.findByFilters(supplier, startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public PurchaseRecord getPurchaseRecordById(UUID id) {
        return purchaseRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("进货记录不存在"));
    }

    @Transactional
    public PurchaseRecord createPurchaseRecord(CreatePurchaseRecordRequest request) {
        // 计算总金额和总重量
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreatePurchaseItemRequest item : request.getItems()) {
            BigDecimal itemAmount = item.getWeight().multiply(item.getUnitPrice());
            totalWeight = totalWeight.add(item.getWeight());
            totalAmount = totalAmount.add(itemAmount);
        }

        // 创建主记录
        PurchaseRecord record = PurchaseRecord.builder()
                .purchaseDate(request.getPurchaseDate())
                .supplier(request.getSupplier())
                .totalWeight(totalWeight)
                .totalAmount(totalAmount)
                .remark(request.getRemark())
                .items(new ArrayList<>())
                .build();

        // 保存主记录
        record = purchaseRecordRepository.save(record);

        // 创建明细记录
        for (CreatePurchaseItemRequest itemRequest : request.getItems()) {
            // 查找进货规格
            PurchaseSpec purchaseSpec = purchaseSpecRepository.findById(itemRequest.getPurchaseSpecId())
                    .orElseThrow(() -> new RuntimeException("进货规格不存在: " + itemRequest.getPurchaseSpecId()));
            
            BigDecimal itemAmount = itemRequest.getWeight().multiply(itemRequest.getUnitPrice());
            PurchaseItem item = PurchaseItem.builder()
                    .purchase(record)
                    .purchaseSpec(purchaseSpec)
                    .weight(itemRequest.getWeight())
                    .unitPrice(itemRequest.getUnitPrice())
                    .amount(itemAmount)
                    .build();
            record.getItems().add(item);
        }

        return purchaseRecordRepository.save(record);
    }

    @Transactional
    public PurchaseRecord updatePurchaseRecord(UUID id, UpdatePurchaseRecordRequest request) {
        PurchaseRecord record = getPurchaseRecordById(id);

        // 更新主记录信息
        if (request.getPurchaseDate() != null) {
            record.setPurchaseDate(request.getPurchaseDate());
        }
        if (request.getSupplier() != null) {
            record.setSupplier(request.getSupplier());
        }
        if (request.getRemark() != null) {
            record.setRemark(request.getRemark());
        }

        // 如果提供了明细，则更新明细
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            // 清除旧明细
            record.getItems().clear();

            // 计算新的总金额和总重量
            BigDecimal totalWeight = BigDecimal.ZERO;
            BigDecimal totalAmount = BigDecimal.ZERO;

            // 添加新明细
            for (UpdatePurchaseItemRequest itemRequest : request.getItems()) {
                // 查找进货规格
                PurchaseSpec purchaseSpec = purchaseSpecRepository.findById(itemRequest.getPurchaseSpecId())
                        .orElseThrow(() -> new RuntimeException("进货规格不存在: " + itemRequest.getPurchaseSpecId()));
                
                BigDecimal itemAmount = itemRequest.getWeight().multiply(itemRequest.getUnitPrice());
                totalWeight = totalWeight.add(itemRequest.getWeight());
                totalAmount = totalAmount.add(itemAmount);

                PurchaseItem item = PurchaseItem.builder()
                        .purchase(record)
                        .purchaseSpec(purchaseSpec)
                        .weight(itemRequest.getWeight())
                        .unitPrice(itemRequest.getUnitPrice())
                        .amount(itemAmount)
                        .build();
                record.getItems().add(item);
            }

            record.setTotalWeight(totalWeight);
            record.setTotalAmount(totalAmount);
        }

        return purchaseRecordRepository.save(record);
    }

    @Transactional
    public void deletePurchaseRecord(UUID id) {
        PurchaseRecord record = getPurchaseRecordById(id);
        purchaseRecordRepository.delete(record);
    }
}
