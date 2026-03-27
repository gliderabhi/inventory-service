package com.sevis.inventoryservice.model.mapper;

import com.sevis.inventoryservice.dto.request.InventoryRequest;
import com.sevis.inventoryservice.dto.response.InventoryResponse;
import com.sevis.inventoryservice.model.InventoryItem;

public class InventoryMapper {

    private InventoryMapper() {}

    public static InventoryItem toEntity(InventoryRequest request) {
        InventoryItem item = new InventoryItem();
        item.setName(request.getName());
        item.setSku(request.getSku());
        item.setQuantity(request.getQuantity());
        item.setPrice(request.getPrice());
        return item;
    }

    public static InventoryResponse toResponse(InventoryItem item) {
        return new InventoryResponse(item);
    }
}
