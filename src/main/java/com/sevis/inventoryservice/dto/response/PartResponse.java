package com.sevis.inventoryservice.dto.response;

import com.sevis.inventoryservice.model.Part;
import lombok.Getter;

@Getter
public class PartResponse {

    private final Long id;
    private final String partNumber;
    private final String description;
    private final double mrpPrice;
    private final double purchasePrice;
    private final String uom;
    private final String productGroup;
    private final String hsnCode;
    private final String taxSlab;

    public PartResponse(Part part) {
        this.id            = part.getId();
        this.partNumber    = part.getPartNumber();
        this.description   = part.getDescription();
        this.mrpPrice      = part.getMrpPrice();
        this.purchasePrice = part.getPurchasePrice();
        this.uom           = part.getUom();
        this.productGroup  = part.getProductGroup();
        this.hsnCode       = part.getHsnCode();
        this.taxSlab       = part.getTaxSlab();
    }
}
