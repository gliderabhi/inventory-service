package com.sevis.inventoryservice.controller;

import com.sevis.inventoryservice.dto.request.InventoryRequest;
import com.sevis.inventoryservice.dto.response.InventoryResponse;
import com.sevis.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public List<InventoryResponse> getAll() {
        return inventoryService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse create(@Valid @RequestBody InventoryRequest request) {
        return inventoryService.create(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.ok(inventoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        inventoryService.delete(id);
    }
}
