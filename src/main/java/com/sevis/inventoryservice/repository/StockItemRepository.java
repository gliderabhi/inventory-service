package com.sevis.inventoryservice.repository;

import com.sevis.inventoryservice.model.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {

    List<StockItem> findByCompanyId(Long companyId);

    Optional<StockItem> findByCompanyIdAndPartNumber(Long companyId, String partNumber);

    List<StockItem> findByCompanyIdAndPartNumberIn(Long companyId, Collection<String> partNumbers);

    void deleteByCompanyIdAndPartNumber(Long companyId, String partNumber);

    @Query("SELECT COALESCE(SUM(s.quantity * COALESCE(s.purchasePrice, 0.0)), 0.0) FROM StockItem s WHERE s.companyId = :companyId")
    double sumStockValueByCompanyId(@Param("companyId") Long companyId);
}
