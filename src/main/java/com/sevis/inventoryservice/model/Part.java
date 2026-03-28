package com.sevis.inventoryservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "parts")
public class Part {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String partNumber;

    @Column(nullable = false)
    private String description;

    /** MRP / selling price (UMRP) */
    private double mrpPrice;

    /** Cost / purchase price */
    private double purchasePrice;

    /** Unit of measure (e.g. Each, Set) */
    private String uom;

    /** Product group code */
    private String productGroup;

    /** HSN code for tax/customs */
    private String hsnCode;

    /** Tax slab (e.g. SLAB 18%) */
    private String taxSlab;
}
