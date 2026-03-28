package com.sevis.inventoryservice.service;

import com.sevis.inventoryservice.dto.request.StockRequest;
import com.sevis.inventoryservice.dto.response.StockResponse;
import com.sevis.inventoryservice.model.StockItem;
import com.sevis.inventoryservice.repository.PartRepository;
import com.sevis.inventoryservice.repository.StockItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
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
}
