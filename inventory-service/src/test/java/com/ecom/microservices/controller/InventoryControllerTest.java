package com.ecom.microservices.controller;

import com.ecom.microservices.dto.BatchDTO;
import com.ecom.microservices.dto.InventoryResponseDTO;
import com.ecom.microservices.service.InventoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Test
    @DisplayName("GET /inventory/{id} - success")
    void getInventory_Success() throws Exception {
        InventoryResponseDTO response = InventoryResponseDTO.builder()
                .productId(1001L)
                .productName("Laptop")
                .batches(Arrays.asList(
                        BatchDTO.builder()
                                .batchId(1L)
                                .quantity(50)
                                .expiryDate(LocalDate.of(2026, 6, 25))
                                .build()
                ))
                .build();

        when(inventoryService.getInventoryByProductId(1001L)).thenReturn(response);

        mockMvc.perform(get("/inventory/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1001))
                .andExpect(jsonPath("$.productName").value("Laptop"));
    }

    @Test
    @DisplayName("GET /inventory/{id} - 404")
    void getInventory_NotFound() throws Exception {
        when(inventoryService.getInventoryByProductId(9999L)).thenReturn(null);

        mockMvc.perform(get("/inventory/9999"))
                .andExpect(status().isNotFound());
    }
}
