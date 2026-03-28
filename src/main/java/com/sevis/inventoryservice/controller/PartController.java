package com.sevis.inventoryservice.controller;

import com.sevis.inventoryservice.dto.response.PartResponse;
import com.sevis.inventoryservice.service.CsvImportService;
import com.sevis.inventoryservice.service.PartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
public class PartController {

    private final PartService partService;
    private final CsvImportService csvImportService;

    @GetMapping
    public org.springframework.data.domain.Page<PartResponse> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return partService.getAll(page, size);
    }

    @GetMapping("/{id}")
    public PartResponse getById(@PathVariable Long id) {
        return partService.getById(id);
    }

    @GetMapping("/by-number/{partNumber}")
    public PartResponse getByPartNumber(@PathVariable String partNumber) {
        return partService.getByPartNumber(partNumber);
    }

    @PostMapping("/import")
    public ResponseEntity<?> importCsv(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String userRole) {

        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only ADMIN users can import parts");
        }
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file provided");
        }
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        if (!filename.endsWith(".csv") && !filename.endsWith(".tsv") && !filename.endsWith(".txt")) {
            return ResponseEntity.badRequest().body("Only CSV/TSV files are accepted");
        }
        try {
            CsvImportService.ImportResult result = csvImportService.importCsv(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Import failed: " + e.getMessage());
        }
    }
}
