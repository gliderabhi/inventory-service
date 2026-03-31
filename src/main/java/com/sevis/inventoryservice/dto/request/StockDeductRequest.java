package com.sevis.inventoryservice.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockDeductRequest {
    private String partNumber;
    private int quantity;
}
