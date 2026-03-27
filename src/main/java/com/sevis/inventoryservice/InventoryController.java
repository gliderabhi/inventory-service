package com.sevis.inventoryservice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryRepository repository;

    public InventoryController(InventoryRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<InventoryItem> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryItem> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public InventoryItem create(@RequestBody InventoryItem item) {
        return repository.save(item);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryItem> update(@PathVariable Long id, @RequestBody InventoryItem updated) {
        return repository.findById(id).map(item -> {
            item.setName(updated.getName());
            item.setSku(updated.getSku());
            item.setQuantity(updated.getQuantity());
            item.setPrice(updated.getPrice());
            return ResponseEntity.ok(repository.save(item));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
