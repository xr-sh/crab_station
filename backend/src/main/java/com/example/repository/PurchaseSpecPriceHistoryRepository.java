package com.example.repository;

import com.example.entity.PurchaseSpecPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PurchaseSpecPriceHistoryRepository extends JpaRepository<PurchaseSpecPriceHistory, UUID> {

    List<PurchaseSpecPriceHistory> findAllByOrderByChangedAtDesc();
}
