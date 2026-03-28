package com.sevis.inventoryservice.service;

import com.sevis.inventoryservice.model.Part;
import com.sevis.inventoryservice.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CsvImportService {

    private final PartRepository partRepository;

    private static final int BATCH_SIZE = 500;

    public ImportResult importCsv(MultipartFile file) throws Exception {
        Charset charset = detectCharset(file.getBytes());
        List<String[]> rows = parseTsv(file, charset);

        if (rows.isEmpty()) {
            return new ImportResult(0, 0, 0, "File is empty");
        }

        // Pre-load all existing parts for fast lookup
        Map<String, Part> existingByPartNumber = partRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Part::getPartNumber, Function.identity()));

        List<Part> toSave = new ArrayList<>();
        int inserted = 0, updated = 0, skipped = 0;

        for (String[] row : rows) {
            if (row.length < 2) { skipped++; continue; }

            String partNumber   = clean(row[0]);
            String description  = clean(row[1]);

            // Skip rows without a valid part number or description
            if (partNumber.isEmpty() || description.isEmpty()
                    || !partNumber.matches("[A-Za-z0-9\\-/]+")) {
                skipped++;
                continue;
            }

            double mrpPrice      = parsePrice(row.length > 2 ? row[2] : "");
            double purchasePrice = parsePrice(row.length > 3 ? row[3] : "");
            String uom           = row.length > 6 ? clean(row[6]) : "";
            String productGroup  = row.length > 7 ? clean(row[7]) : "";
            String hsnCode       = row.length > 8 ? clean(row[8]) : "";
            String taxSlab       = row.length > 9 ? clean(row[9]) : "";

            Part part = existingByPartNumber.get(partNumber);
            if (part != null) {
                part.setDescription(description);
                part.setMrpPrice(mrpPrice);
                part.setPurchasePrice(purchasePrice);
                part.setUom(uom);
                part.setProductGroup(productGroup);
                part.setHsnCode(hsnCode);
                part.setTaxSlab(taxSlab);
                updated++;
            } else {
                part = new Part();
                part.setPartNumber(partNumber);
                part.setDescription(description);
                part.setMrpPrice(mrpPrice);
                part.setPurchasePrice(purchasePrice);
                part.setUom(uom);
                part.setProductGroup(productGroup);
                part.setHsnCode(hsnCode);
                part.setTaxSlab(taxSlab);
                existingByPartNumber.put(partNumber, part);
                inserted++;
            }

            toSave.add(part);

            if (toSave.size() >= BATCH_SIZE) {
                partRepository.saveAll(toSave);
                toSave.clear();
            }
        }

        if (!toSave.isEmpty()) {
            partRepository.saveAll(toSave);
        }

        return new ImportResult(inserted, updated, skipped, "Import complete");
    }

    private List<String[]> parseTsv(MultipartFile file, Charset charset) throws Exception {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), charset))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) { header = false; continue; }
                rows.add(line.split("\t", -1));
            }
        }
        return rows;
    }

    private Charset detectCharset(byte[] bytes) {
        if (bytes.length >= 2 && bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFE)
            return StandardCharsets.UTF_16LE;
        if (bytes.length >= 2 && bytes[0] == (byte) 0xFE && bytes[1] == (byte) 0xFF)
            return StandardCharsets.UTF_16BE;
        return StandardCharsets.UTF_8;
    }

    private String clean(String s) {
        return s == null ? "" : s.trim().replaceAll("^\"|\"$", "").trim();
    }

    private double parsePrice(String s) {
        if (s == null || s.isBlank()) return 0.0;
        try {
            return Double.parseDouble(
                    clean(s).replace("Rs.", "").replace(",", "").trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public record ImportResult(int inserted, int updated, int skipped, String message) {}
}
