package com.sevis.inventoryservice.service;

import com.sevis.inventoryservice.dto.response.PartResponse;
import com.sevis.inventoryservice.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PartService {

    private final PartRepository partRepository;

    public Page<PartResponse> getAll(int page, int size) {
        return partRepository
                .findAll(PageRequest.of(page, size, Sort.by("partNumber")))
                .map(PartResponse::new);
    }

    public PartResponse getById(Long id) {
        return partRepository.findById(id)
                .map(PartResponse::new)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Part not found: " + id));
    }

    public PartResponse getByPartNumber(String partNumber) {
        return partRepository.findByPartNumber(partNumber)
                .map(PartResponse::new)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Part not found: " + partNumber));
    }
}
