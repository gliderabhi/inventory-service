package com.sevis.inventoryservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockRequest {

    @NotBlank
    private String partNumber;

    @Min(0)
    private int quantity;

    /** Optional: company's own purchase price for this part */
    private Double purchasePrice;
}
