package com.sevis.inventoryservice.controller;

import com.sevis.inventoryservice.dto.request.StockDeductRequest;
import com.sevis.inventoryservice.dto.request.StockRequest;
import com.sevis.inventoryservice.dto.response.StockImportResult;
import com.sevis.inventoryservice.dto.response.StockResponse;
import com.sevis.inventoryservice.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /** Get all stock for the calling company.
     *  ADMIN can pass ?companyId=X to view any company's stock. */
    @GetMapping
    public List<StockResponse> getAll(
            @RequestHeader(value = "X-User-Id",   defaultValue = "0") Long userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role,
            @RequestParam(required = false) Long companyId) {

        Long resolvedCompanyId = resolveCompanyId(userId, role, companyId);
        return stockService.getByCompany(resolvedCompanyId);
    }

    @GetMapping("/{partNumber}")
    public StockResponse getByPartNumber(
            @PathVariable String partNumber,
            @RequestHeader(value = "X-User-Id",   defaultValue = "0") Long userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role,
            @RequestParam(required = false) Long companyId) {

        Long resolvedCompanyId = resolveCompanyId(userId, role, companyId);
        return stockService.getByPartNumber(resolvedCompanyId, partNumber);
    }

    /** Create or update a stock entry (upsert by partNumber). */
    @PostMapping
    public StockResponse upsert(
            @Valid @RequestBody StockRequest req,
            @RequestHeader(value = "X-User-Id",   defaultValue = "0") Long userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {

        return stockService.upsert(userId, req);
    }

    @DeleteMapping("/{partNumber}")
    public ResponseEntity<Void> delete(
            @PathVariable String partNumber,
            @RequestHeader(value = "X-User-Id",   defaultValue = "0") Long userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role,
            @RequestParam(required = false) Long companyId) {

        Long resolvedCompanyId = resolveCompanyId(userId, role, companyId);
        stockService.delete(resolvedCompanyId, partNumber);
        return ResponseEntity.noContent().build();
    }

    /** Upload a Purchase Order xlsx (raw bytes) to increment stock quantities.
     *  Part # validated against catalogue; unrecognised parts are skipped. */
    @PostMapping("/update-inventory")
    public StockImportResult importXlsx(
            @RequestBody byte[] fileBytes,
            @RequestHeader(value = "X-User-Id",   defaultValue = "0") Long userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role,
            @RequestParam(required = false) Long companyId) {

        Long resolvedCompanyId = resolveCompanyId(userId, role, companyId);
        return stockService.importXlsx(fileBytes, resolvedCompanyId);
    }

    @PostMapping("/deduct")
    public ResponseEntity<Void> deductBatch(
            @RequestBody List<StockDeductRequest> requests,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") Long userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role,
            @RequestParam(required = false) Long companyId) {

        Long resolvedCompanyId = resolveCompanyId(userId, role, companyId);
        stockService.deductBatch(resolvedCompanyId, requests);
        return ResponseEntity.ok().build();
    }

    /** ADMIN can target any companyId via query param; others always use their own userId. */
    private Long resolveCompanyId(Long userId, String role, Long requestedCompanyId) {
        if ("ADMIN".equals(role) && requestedCompanyId != null) {
            return requestedCompanyId;
        }
        if (userId == 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing user identity");
        }
        return userId;
    }
}
