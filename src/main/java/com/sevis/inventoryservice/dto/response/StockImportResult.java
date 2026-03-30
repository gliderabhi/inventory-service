package com.sevis.inventoryservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StockImportResult {
    private final int created;
    private final int updated;
    private final int skipped;
    private final int total;
    private final String message;
}
