package com.sevis.inventoryservice.dto.response;

import com.sevis.inventoryservice.model.Part;
import com.sevis.inventoryservice.model.StockItem;
import lombok.Getter;

@Getter
public class StockResponse {

    private final Long id;
    private final Long companyId;
    private final String partNumber;
    private final int quantity;
    private final Double purchasePrice;
    private final String lastUpdated;

    // Denormalized part details for display (avoids extra round-trips)
    private final String description;
    private final double mrpPrice;
    private final String uom;
    private final String productGroup;
    private final String hsnCode;
    private final String taxSlab;

    public StockResponse(StockItem stock, Part part) {
        this.id            = stock.getId();
        this.companyId     = stock.getCompanyId();
        this.partNumber    = stock.getPartNumber();
        this.quantity      = stock.getQuantity();
        this.purchasePrice = stock.getPurchasePrice() != null
                ? stock.getPurchasePrice()
                : (part != null ? part.getPurchasePrice() : null);
        this.lastUpdated   = stock.getLastUpdated() != null ? stock.getLastUpdated().toString() : "";

        this.description  = part != null ? part.getDescription()  : "";
        this.mrpPrice     = part != null ? part.getMrpPrice()     : 0.0;
        this.uom          = part != null ? part.getUom()          : "";
        this.productGroup = part != null ? part.getProductGroup() : "";
        this.hsnCode      = part != null ? part.getHsnCode()      : "";
        this.taxSlab      = part != null ? part.getTaxSlab()      : "";
    }
}
