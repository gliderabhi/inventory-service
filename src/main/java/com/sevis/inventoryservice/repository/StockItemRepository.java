package com.sevis.inventoryservice.repository;

import com.sevis.inventoryservice.model.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {

    List<StockItem> findByCompanyId(Long companyId);

    Optional<StockItem> findByCompanyIdAndPartNumber(Long companyId, String partNumber);

    void deleteByCompanyIdAndPartNumber(Long companyId, String partNumber);
}
