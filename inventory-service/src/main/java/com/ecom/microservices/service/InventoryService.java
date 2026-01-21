package com.ecom.microservices.service;

import com.ecom.microservices.dto.BatchDTO;
import com.ecom.microservices.dto.InventoryResponseDTO;
import com.ecom.microservices.dto.InventoryUpdateRequestDTO;
import com.ecom.microservices.dto.InventoryUpdateResponseDTO;
import com.ecom.microservices.entity.InventoryBatch;
import com.ecom.microservices.factory.InventoryHandler;
import com.ecom.microservices.factory.InventoryHandlerFactory;
import com.ecom.microservices.repository.InventoryBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryBatchRepository inventoryBatchRepository;
    private final InventoryHandlerFactory inventoryHandlerFactory;

    @Transactional(readOnly = true)
    public InventoryResponseDTO getInventoryByProductId(Long productId) {
        log.info("Fetching inventory for product ID: {}", productId);
        
        List<InventoryBatch> batches = inventoryBatchRepository
                .findByProductIdOrderByExpiryDateAsc(productId);

        if (batches.isEmpty()) {
            log.warn("No inventory found for product ID: {}", productId);
            return null;
        }

        String productName = batches.get(0).getProductName();
        
        List<BatchDTO> batchDTOs = batches.stream()
                .map(batch -> BatchDTO.builder()
                        .batchId(batch.getBatchId())
                        .quantity(batch.getQuantity())
                        .expiryDate(batch.getExpiryDate())
                        .build())
                .collect(Collectors.toList());

        return InventoryResponseDTO.builder()
                .productId(productId)
                .productName(productName)
                .batches(batchDTOs)
                .build();
    }

    @Transactional
    public InventoryUpdateResponseDTO updateInventory(InventoryUpdateRequestDTO request) {
        log.info("Updating inventory for product ID: {}, quantity: {}", 
                request.getProductId(), request.getQuantity());

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            return InventoryUpdateResponseDTO.builder()
                    .success(false)
                    .message("Invalid quantity. Must be greater than zero.")
                    .reservedFromBatchIds(new ArrayList<>())
                    .build();
        }

        List<InventoryBatch> availableBatches = inventoryBatchRepository
                .findAvailableBatchesByProductId(request.getProductId());

        InventoryHandler handler = inventoryHandlerFactory.getDefaultHandler();
        InventoryUpdateResponseDTO response = handler.processUpdate(availableBatches, request);
        
        log.info("Inventory update result - success: {}, message: {}", 
                response.isSuccess(), response.getMessage());

        return response;
    }

    @Transactional(readOnly = true)
    public boolean checkAvailability(Long productId, Integer quantity) {
        Integer totalAvailable = inventoryBatchRepository.getTotalQuantityByProductId(productId);
        return totalAvailable != null && totalAvailable >= quantity;
    }
}
