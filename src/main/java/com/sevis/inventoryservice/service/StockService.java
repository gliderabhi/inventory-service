package com.sevis.inventoryservice.service;

import com.sevis.inventoryservice.dto.request.StockDeductRequest;
import com.sevis.inventoryservice.dto.request.StockRequest;
import com.sevis.inventoryservice.dto.response.StockImportResult;
import com.sevis.inventoryservice.dto.response.StockResponse;
import com.sevis.inventoryservice.model.StockItem;
import com.sevis.inventoryservice.repository.PartRepository;
import com.sevis.inventoryservice.repository.StockItemRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockItemRepository stockRepository;
    private final PartRepository partRepository;

    public List<StockResponse> getByCompany(Long companyId) {
        return stockRepository.findByCompanyId(companyId)
                .stream()
                .map(s -> new StockResponse(s, partRepository.findByPartNumber(s.getPartNumber()).orElse(null)))
                .collect(Collectors.toList());
    }

    public StockResponse getByPartNumber(Long companyId, String partNumber) {
        StockItem stock = stockRepository.findByCompanyIdAndPartNumber(companyId, partNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No stock entry for part: " + partNumber));
        return new StockResponse(stock, partRepository.findByPartNumber(partNumber).orElse(null));
    }

    @Transactional
    public StockResponse upsert(Long companyId, StockRequest req) {
        if (!partRepository.findByPartNumber(req.getPartNumber()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Part not found in catalogue: " + req.getPartNumber());
        }

        StockItem stock = stockRepository
                .findByCompanyIdAndPartNumber(companyId, req.getPartNumber())
                .orElseGet(StockItem::new);

        stock.setCompanyId(companyId);
        stock.setPartNumber(req.getPartNumber());
        stock.setQuantity(req.getQuantity());
        if (req.getPurchasePrice() != null) {
            stock.setPurchasePrice(req.getPurchasePrice());
        }

        StockItem saved = stockRepository.save(stock);
        return new StockResponse(saved, partRepository.findByPartNumber(req.getPartNumber()).orElse(null));
    }

    @Transactional
    public void delete(Long companyId, String partNumber) {
        stockRepository.deleteByCompanyIdAndPartNumber(companyId, partNumber);
    }

    @Transactional
    public void deductBatch(Long companyId, List<StockDeductRequest> requests) {
        for (StockDeductRequest req : requests) {
            if (req.getPartNumber() == null || req.getQuantity() <= 0) continue;
            stockRepository.findByCompanyIdAndPartNumber(companyId, req.getPartNumber())
                    .ifPresent(item -> {
                        int newQty = Math.max(0, item.getQuantity() - req.getQuantity());
                        item.setQuantity(newQty);
                        stockRepository.save(item);
                    });
            // If part not in stock → silently skip (per business rule)
        }
    }

    @Transactional
    public void restoreBatch(Long companyId, List<StockDeductRequest> requests) {
        for (StockDeductRequest req : requests) {
            if (req.getPartNumber() == null || req.getQuantity() <= 0) continue;
            stockRepository.findByCompanyIdAndPartNumber(companyId, req.getPartNumber())
                    .ifPresent(item -> {
                        item.setQuantity(item.getQuantity() + req.getQuantity());
                        stockRepository.save(item);
                    });
        }
    }

    public double getTotalStockValue(Long companyId) {
        return stockRepository.sumStockValueByCompanyId(companyId);
    }

    // ── XLSX PO Import ────────────────────────────────────────────────────────
    // XLSX columns (0-indexed): 7 = Received Qty, 10 = Part #

    private static final int COL_PART_NUMBER   = 10;
    private static final int COL_RECEIVED_QTY  = 7;
    private static final int BATCH_SIZE        = 100;

    // xlsx files are ZIP-based and start with PK magic bytes 50 4B 03 04
    private static boolean isXlsx(byte[] bytes) {
        return bytes.length > 4
                && bytes[0] == 0x50 && bytes[1] == 0x4B
                && bytes[2] == 0x03 && bytes[3] == 0x04;
    }

    @Transactional
    public StockImportResult importXlsx(byte[] fileBytes, Long companyId) {
        if (!isXlsx(fileBytes)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid file. Please upload a valid .xlsx file.");
        }
        try (var workbook = WorkbookFactory.create(new ByteArrayInputStream(fileBytes))) {
            Sheet sheet = workbook.getSheetAt(0);

            // Collect all rows (skip header row 0)
            record PoRow(String partNumber, int qty) {}
            List<PoRow> rows = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String partNo = cellString(row.getCell(COL_PART_NUMBER)).trim();
                if (partNo.isBlank()) continue;
                int qty = (int) cellNumeric(row.getCell(COL_RECEIVED_QTY));
                if (qty <= 0) continue;
                rows.add(new PoRow(partNo, qty));
            }

            int created = 0, updated = 0, skipped = 0;

            // Process in batches
            for (int start = 0; start < rows.size(); start += BATCH_SIZE) {
                List<PoRow> batch = rows.subList(start, Math.min(start + BATCH_SIZE, rows.size()));

                Set<String> partNums = batch.stream().map(PoRow::partNumber).collect(Collectors.toSet());

                // Validate against catalogue — only process known parts
                Set<String> validParts = partRepository.findAllByPartNumberIn(partNums)
                        .stream().map(p -> p.getPartNumber()).collect(Collectors.toSet());

                // Load existing stock for this company + batch of parts
                Map<String, StockItem> existingStock = stockRepository
                        .findByCompanyIdAndPartNumberIn(companyId, validParts)
                        .stream().collect(Collectors.toMap(StockItem::getPartNumber, Function.identity()));

                List<StockItem> toSave = new ArrayList<>();
                for (PoRow row : batch) {
                    if (!validParts.contains(row.partNumber())) {
                        skipped++;
                        continue;
                    }
                    StockItem item = existingStock.getOrDefault(row.partNumber(), null);
                    if (item == null) {
                        item = new StockItem();
                        item.setCompanyId(companyId);
                        item.setPartNumber(row.partNumber());
                        item.setQuantity(row.qty());
                        created++;
                    } else {
                        item.setQuantity(item.getQuantity() + row.qty());
                        updated++;
                    }
                    toSave.add(item);
                }
                stockRepository.saveAll(toSave);
            }

            int total = created + updated + skipped;
            return new StockImportResult(created, updated, skipped, total,
                    String.format("Processed %d rows: %d created, %d updated, %d skipped", total, created, updated, skipped));

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to parse xlsx: " + e.getMessage());
        }
    }

    private static String cellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case FORMULA -> cell.getCachedFormulaResultType() == CellType.NUMERIC
                    ? String.valueOf((long) cell.getNumericCellValue())
                    : cell.getStringCellValue();
            default -> "";
        };
    }

    private static double cellNumeric(Cell cell) {
        if (cell == null) return 0;
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING  -> {
                try { yield Double.parseDouble(cell.getStringCellValue().trim()); }
                catch (NumberFormatException e) { yield 0; }
            }
            default -> 0;
        };
    }
}
