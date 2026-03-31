package com.sevis.inventoryservice.repository;

import com.sevis.inventoryservice.model.Part;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PartRepository extends JpaRepository<Part, Long> {
    Optional<Part> findByPartNumber(String partNumber);
    List<Part> findAllByPartNumberIn(Collection<String> partNumbers);
    List<Part> findByPartNumberContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String partNumber, String description, Pageable pageable);
}
