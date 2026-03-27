package com.sevis.inventoryservice.service;

import com.sevis.inventoryservice.dto.request.InventoryRequest;
import com.sevis.inventoryservice.dto.response.InventoryResponse;
import com.sevis.inventoryservice.model.mapper.InventoryMapper;
import com.sevis.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public List<InventoryResponse> getAll() {
        return inventoryRepository.findAll()
                .stream()
                .map(InventoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    public InventoryResponse getById(Long id) {
        return inventoryRepository.findById(id)
                .map(InventoryMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found: " + id));
    }

    public InventoryResponse create(InventoryRequest request) {
        return InventoryMapper.toResponse(inventoryRepository.save(InventoryMapper.toEntity(request)));
    }

    public InventoryResponse update(Long id, InventoryRequest request) {
        return inventoryRepository.findById(id).map(item -> {
            item.setName(request.getName());
            item.setSku(request.getSku());
            item.setQuantity(request.getQuantity());
            item.setPrice(request.getPrice());
            return InventoryMapper.toResponse(inventoryRepository.save(item));
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found: " + id));
    }

    public void delete(Long id) {
        if (!inventoryRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found: " + id);
        }
        inventoryRepository.deleteById(id);
    }
}
