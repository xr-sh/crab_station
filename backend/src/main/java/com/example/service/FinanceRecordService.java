package com.example.service;

import com.example.dto.CreateFinanceRecordRequest;
import com.example.dto.FinanceStatisticsDTO;
import com.example.dto.UpdateFinanceRecordRequest;
import com.example.entity.FinanceRecord;
import com.example.repository.FinanceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinanceRecordService {

    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("10000");
    private static final String INCOME_TYPE = "收入";
    private static final String EXPENSE_TYPE = "支出";

    private final FinanceRecordRepository financeRecordRepository;

    @Transactional(readOnly = true)
    public Page<FinanceRecord> getFinanceRecords(String type, String keyword, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        String normalizedKeyword = keyword == null || keyword.trim().isEmpty() ? null : keyword.trim();
        return financeRecordRepository.findByFilters(type, normalizedKeyword, startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public FinanceStatisticsDTO getFinanceStatistics(String type, String keyword, LocalDate startDate, LocalDate endDate) {
        String normalizedKeyword = keyword == null || keyword.trim().isEmpty() ? null : keyword.trim();
        Object[] values = unwrapSingleRow(financeRecordRepository.sumByFilters(
                type,
                normalizedKeyword,
                startDate,
                endDate,
                INCOME_TYPE,
                EXPENSE_TYPE));
        BigDecimal income = toBigDecimal(values, 0);
        BigDecimal expense = toBigDecimal(values, 1);

        return FinanceStatisticsDTO.builder()
                .income(income)
                .expense(expense)
                .balance(INITIAL_BALANCE.add(income).subtract(expense))
                .build();
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

    private BigDecimal toBigDecimal(Object[] values, int index) {
        if (values == null || values.length <= index || values[index] == null) {
            return BigDecimal.ZERO;
        }
        if (values[index] instanceof BigDecimal value) {
            return value;
        }
        if (values[index] instanceof Number value) {
            return BigDecimal.valueOf(value.doubleValue());
        }
        return new BigDecimal(values[index].toString());
    }

    private Object[] unwrapSingleRow(Object[] values) {
        if (values != null && values.length == 1 && values[0] instanceof Object[] row) {
            return row;
        }
        return values;
    }
}
