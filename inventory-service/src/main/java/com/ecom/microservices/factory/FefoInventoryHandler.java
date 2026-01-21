package com.ecom.microservices.factory;

import com.ecom.microservices.dto.InventoryUpdateRequestDTO;
import com.ecom.microservices.dto.InventoryUpdateResponseDTO;
import com.ecom.microservices.entity.InventoryBatch;
import com.ecom.microservices.repository.InventoryBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FefoInventoryHandler implements InventoryHandler {

    public static final String TYPE = "FEFO";

    private final InventoryBatchRepository inventoryBatchRepository;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public InventoryUpdateResponseDTO processUpdate(List<InventoryBatch> batches, InventoryUpdateRequestDTO request) {
        if (batches == null || batches.isEmpty()) {
            return InventoryUpdateResponseDTO.builder()
                    .success(false)
                    .message("Product not found in inventory.")
                    .reservedFromBatchIds(new ArrayList<>())
                    .build();
        }

        String productName = batches.get(0).getProductName();
        int remainingQuantity = request.getQuantity();
        List<Long> reservedBatchIds = new ArrayList<>();

        // total stock
        int totalAvailable = batches.stream()
                .mapToInt(InventoryBatch::getQuantity)
                .sum();

        if (totalAvailable < remainingQuantity) {
            return InventoryUpdateResponseDTO.builder()
                    .success(false)
                    .productName(productName)
                    .message("Insufficient inventory. Available: " + totalAvailable + ", Requested: " + remainingQuantity)
                    .reservedFromBatchIds(new ArrayList<>())
                    .build();
        }

        // reserve using FEFO
        for (InventoryBatch batch : batches) {
            if (remainingQuantity <= 0) {
                break;
            }

            int batchQuantity = batch.getQuantity();
            if (batchQuantity > 0) {
                int quantityToReserve = Math.min(batchQuantity, remainingQuantity);
                int newQuantity = batchQuantity - quantityToReserve;
                batch.setQuantity(newQuantity);
                
                if (newQuantity == 0) {
                    inventoryBatchRepository.delete(batch);
                } else {
                    inventoryBatchRepository.save(batch);
                }
                
                reservedBatchIds.add(batch.getBatchId());
                remainingQuantity -= quantityToReserve;
            }
        }

        return InventoryUpdateResponseDTO.builder()
                .success(true)
                .productName(productName)
                .reservedFromBatchIds(reservedBatchIds)
                .message("Inventory reserved successfully.")
                .build();
    }
}
