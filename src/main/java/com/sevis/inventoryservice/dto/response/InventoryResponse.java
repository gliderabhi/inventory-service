package com.sevis.inventoryservice.dto.response;

import com.sevis.inventoryservice.model.InventoryItem;
import lombok.Getter;

@Getter
public class InventoryResponse {

    private final Long id;
    private final String name;
    private final String sku;
    private final int quantity;
    private final double price;

    public InventoryResponse(InventoryItem item) {
        this.id = item.getId();
        this.name = item.getName();
        this.sku = item.getSku();
        this.quantity = item.getQuantity();
        this.price = item.getPrice();
    }
}
