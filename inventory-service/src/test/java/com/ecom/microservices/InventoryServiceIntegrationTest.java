package com.ecom.microservices;

import com.ecom.microservices.repository.InventoryBatchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InventoryServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventoryBatchRepository inventoryBatchRepository;

    @Test
    @DisplayName("get inventory - success")
    void getInventory_Success() throws Exception {
        mockMvc.perform(get("/inventory/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1001))
                .andExpect(jsonPath("$.productName").value("Laptop"));
    }

    @Test
    @DisplayName("get inventory - 404")
    void getInventory_NotFound() throws Exception {
        mockMvc.perform(get("/inventory/9999"))
                .andExpect(status().isNotFound());
    }
}
