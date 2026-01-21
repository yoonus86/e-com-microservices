package com.ecom.microservices.factory;

import com.ecom.microservices.dto.InventoryUpdateRequestDTO;
import com.ecom.microservices.dto.InventoryUpdateResponseDTO;
import com.ecom.microservices.entity.InventoryBatch;

import java.util.List;

public interface InventoryHandler {

    String getType();

    InventoryUpdateResponseDTO processUpdate(List<InventoryBatch> batches, InventoryUpdateRequestDTO request);
}
