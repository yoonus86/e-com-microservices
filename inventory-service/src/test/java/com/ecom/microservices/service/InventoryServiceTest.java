package com.ecom.microservices.service;

import com.ecom.microservices.dto.InventoryResponseDTO;
import com.ecom.microservices.entity.InventoryBatch;
import com.ecom.microservices.factory.FefoInventoryHandler;
import com.ecom.microservices.factory.InventoryHandlerFactory;
import com.ecom.microservices.repository.InventoryBatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryBatchRepository inventoryBatchRepository;

    @Mock
    private InventoryHandlerFactory inventoryHandlerFactory;

    @Mock
    private FefoInventoryHandler fefoInventoryHandler;

    @InjectMocks
    private InventoryService inventoryService;

    private List<InventoryBatch> batches;

    @BeforeEach
    void setUp() {
        InventoryBatch batch1 = InventoryBatch.builder()
                .batchId(1L)
                .productId(1001L)
                .productName("Laptop")
                .quantity(50)
                .expiryDate(LocalDate.of(2026, 6, 25))
                .build();

        batches = Arrays.asList(batch1);
    }

    @Test
    @DisplayName("get inventory - success")
    void getInventory_Success() {
        when(inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(1001L)).thenReturn(batches);

        InventoryResponseDTO response = inventoryService.getInventoryByProductId(1001L);

        assertNotNull(response);
        assertEquals(1001L, response.getProductId());
        assertEquals("Laptop", response.getProductName());
    }

    @Test
    @DisplayName("get inventory - not found")
    void getInventory_NotFound() {
        when(inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(9999L)).thenReturn(Collections.emptyList());

        InventoryResponseDTO response = inventoryService.getInventoryByProductId(9999L);

        assertNull(response);
    }
}
