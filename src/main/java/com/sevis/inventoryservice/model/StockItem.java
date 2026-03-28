package com.sevis.inventoryservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
    name = "stock_items",
    uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "part_number"})
)
public class StockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user/company who owns this stock entry */
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    /** References parts.part_number */
    @Column(name = "part_number", nullable = false)
    private String partNumber;

    @Column(nullable = false)
    private int quantity;

    /** Company-specific purchase price (overrides global catalogue price) */
    private Double purchasePrice;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}
