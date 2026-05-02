package com.example.service;

import com.example.dto.CreatePurchaseItemRequest;
import com.example.dto.CreatePurchaseRecordRequest;
import com.example.dto.UpdatePurchaseItemRequest;
import com.example.dto.UpdatePurchaseRecordRequest;
import com.example.entity.PurchaseItem;
import com.example.entity.PurchaseRecord;
import com.example.entity.PurchaseSpec;
import com.example.exception.BusinessException;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRecordRepository purchaseRecordRepository;
    private final PurchaseSpecRepository purchaseSpecRepository;

    @Transactional(readOnly = true)
    public Page<PurchaseRecord> getPurchaseRecords(String supplier, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return purchaseRecordRepository.findByFilters(supplier, startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public PurchaseRecord getPurchaseRecordById(UUID id) {
        return purchaseRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Purchase record does not exist"));
    }

    @Transactional
    public PurchaseRecord createPurchaseRecord(CreatePurchaseRecordRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("Purchase items cannot be empty");
        }

        Totals totals = calculateCreateTotals(request);
        PurchaseRecord record = PurchaseRecord.builder()
                .purchaseDate(request.getPurchaseDate())
                .supplier(request.getSupplier())
                .totalWeight(totals.totalWeight())
                .totalAmount(totals.totalAmount())
                .remark(request.getRemark())
                .items(new ArrayList<>())
                .build();

        record = purchaseRecordRepository.save(record);

        for (CreatePurchaseItemRequest itemRequest : request.getItems()) {
            PurchaseSpec purchaseSpec = getPurchaseSpec(itemRequest.getPurchaseSpecId());
            PurchaseItem item = PurchaseItem.builder()
                    .purchase(record)
                    .purchaseSpec(purchaseSpec)
                    .weight(itemRequest.getWeight())
                    .unitPrice(itemRequest.getUnitPrice())
                    .amount(itemRequest.getWeight().multiply(itemRequest.getUnitPrice()))
                    .build();
            record.getItems().add(item);
        }

        return purchaseRecordRepository.save(record);
    }

    @Transactional
    public PurchaseRecord updatePurchaseRecord(UUID id, UpdatePurchaseRecordRequest request) {
        PurchaseRecord record = getPurchaseRecordById(id);

        if (request.getPurchaseDate() != null) {
            record.setPurchaseDate(request.getPurchaseDate());
        }
        if (request.getSupplier() != null) {
            record.setSupplier(request.getSupplier());
        }
        if (request.getRemark() != null) {
            record.setRemark(request.getRemark());
        }

        if (request.getItems() != null) {
            if (request.getItems().isEmpty()) {
                throw new BusinessException("Purchase items cannot be empty");
            }

            record.getItems().clear();
            BigDecimal totalWeight = BigDecimal.ZERO;
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (UpdatePurchaseItemRequest itemRequest : request.getItems()) {
                PurchaseSpec purchaseSpec = getPurchaseSpec(itemRequest.getPurchaseSpecId());
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
        purchaseRecordRepository.delete(getPurchaseRecordById(id));
    }

    private PurchaseSpec getPurchaseSpec(UUID id) {
        return purchaseSpecRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Purchase spec does not exist: " + id));
    }

    private Totals calculateCreateTotals(CreatePurchaseRecordRequest request) {
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreatePurchaseItemRequest item : request.getItems()) {
            BigDecimal itemAmount = item.getWeight().multiply(item.getUnitPrice());
            totalWeight = totalWeight.add(item.getWeight());
            totalAmount = totalAmount.add(itemAmount);
        }
        return new Totals(totalWeight, totalAmount);
    }

    private record Totals(BigDecimal totalWeight, BigDecimal totalAmount) {
    }
}
