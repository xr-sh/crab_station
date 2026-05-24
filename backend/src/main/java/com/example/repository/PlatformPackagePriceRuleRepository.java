package com.example.repository;

import com.example.entity.PlatformPackagePriceRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PlatformPackagePriceRuleRepository extends JpaRepository<PlatformPackagePriceRule, UUID> {

    List<PlatformPackagePriceRule> findByPlatformPackage_IdOrderBySortNoAscCreatedAtAsc(UUID packageId);

    void deleteByPlatformPackage_Id(UUID packageId);

    @Modifying
    @Query("DELETE FROM PlatformPackagePriceRule rule WHERE rule.platformPackage.id = :packageId")
    int deleteByPackageIdBulk(@Param("packageId") UUID packageId);

    @Modifying
    @Query(value = "DELETE rule FROM platform_package_price_rules rule INNER JOIN platform_packages pkg ON rule.package_id = pkg.id WHERE pkg.name = :packageName AND pkg.created_at = :createdAt", nativeQuery = true)
    int deleteByPackageNameAndCreatedAt(@Param("packageName") String packageName, @Param("createdAt") LocalDateTime createdAt);
}
