package com.sevis.inventoryservice.dto.response;

import com.sevis.inventoryservice.model.InventoryItem;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class InventoryResponse {

    private final Long id;
    private final String sku;
    private final String name;
    private final int quantity;
    private final double price;
    private final LocalDate purchasedAt;

    public InventoryResponse(InventoryItem item) {
        this.id          = item.getId();
        this.sku         = item.getSku();
        this.name        = item.getName();
        this.quantity    = item.getQuantity();
        this.price       = item.getPrice();
        this.purchasedAt = item.getPurchasedAt();
    }
}
