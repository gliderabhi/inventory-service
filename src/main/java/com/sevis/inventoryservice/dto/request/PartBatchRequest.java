package com.sevis.inventoryservice.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PartBatchRequest {
    private String partNumber;
    private String description;
    private double mrpPrice;
    private double purchasePrice;
    private String uom;
    private String productGroup;
    private String hsnCode;
    private String taxSlab;
}
