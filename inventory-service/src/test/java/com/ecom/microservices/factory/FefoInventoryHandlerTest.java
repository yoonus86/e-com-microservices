package com.ecom.microservices.factory;

import com.ecom.microservices.dto.InventoryUpdateRequestDTO;
import com.ecom.microservices.dto.InventoryUpdateResponseDTO;
import com.ecom.microservices.entity.InventoryBatch;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FefoInventoryHandlerTest {

    @Mock
    private InventoryBatchRepository inventoryBatchRepository;

    @InjectMocks
    private FefoInventoryHandler fefoInventoryHandler;

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
    @DisplayName("reserve stock - success")
    void processUpdate_Success() {
        InventoryUpdateRequestDTO request = InventoryUpdateRequestDTO.builder()
                .productId(1001L)
                .quantity(20)
                .build();

        when(inventoryBatchRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        InventoryUpdateResponseDTO response = fefoInventoryHandler.processUpdate(batches, request);

        assertTrue(response.isSuccess());
        assertFalse(response.getReservedFromBatchIds().isEmpty());
        verify(inventoryBatchRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("reserve stock - fails when empty")
    void processUpdate_Fail() {
        InventoryUpdateRequestDTO request = InventoryUpdateRequestDTO.builder()
                .productId(1001L)
                .quantity(10)
                .build();

        InventoryUpdateResponseDTO response = fefoInventoryHandler.processUpdate(null, request);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Product not found"));
    }
}
