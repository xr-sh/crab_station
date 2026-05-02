package com.example.service;

import com.example.dto.CreateFinanceRecordRequest;
import com.example.dto.UpdateFinanceRecordRequest;
import com.example.entity.FinanceRecord;
import com.example.repository.FinanceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinanceRecordService {

    private final FinanceRecordRepository financeRecordRepository;

    @Transactional(readOnly = true)
    public Page<FinanceRecord> getFinanceRecords(String type, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return financeRecordRepository.findByFilters(type, startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public FinanceRecord getFinanceRecordById(UUID id) {
        return financeRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Finance record does not exist"));
    }

    @Transactional
    public FinanceRecord createFinanceRecord(CreateFinanceRecordRequest request) {
        FinanceRecord record = FinanceRecord.builder()
                .recordDate(request.getRecordDate())
                .amount(request.getAmount())
                .type(request.getType())
                .remark(request.getRemark())
                .build();
        return financeRecordRepository.save(record);
    }

    @Transactional
    public FinanceRecord updateFinanceRecord(UUID id, UpdateFinanceRecordRequest request) {
        FinanceRecord record = getFinanceRecordById(id);

        if (request.getRecordDate() != null) {
            record.setRecordDate(request.getRecordDate());
        }
        if (request.getAmount() != null) {
            record.setAmount(request.getAmount());
        }
        if (request.getType() != null) {
            record.setType(request.getType());
        }
        if (request.getRemark() != null) {
            record.setRemark(request.getRemark());
        }

        return financeRecordRepository.save(record);
    }

    @Transactional
    public void deleteFinanceRecord(UUID id) {
        financeRecordRepository.delete(getFinanceRecordById(id));
    }
}
