package com.sevis.inventoryservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class InventoryRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String sku;

    @Min(0)
    private int quantity;

    @Positive
    private double price;
}
