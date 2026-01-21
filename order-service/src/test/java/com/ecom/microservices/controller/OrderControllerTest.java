package com.ecom.microservices.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecom.microservices.dto.OrderRequestDTO;
import com.ecom.microservices.dto.OrderResponseDTO;
import com.ecom.microservices.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("POST /order - success")
    void placeOrder_Success() throws Exception {
        OrderRequestDTO request = OrderRequestDTO.builder()
                .productId(1001L)
                .quantity(10)
                .build();

        OrderResponseDTO response = OrderResponseDTO.builder()
                .orderId(1L)
                .productId(1001L)
                .productName("Laptop")
                .quantity(10)
                .status("PLACED")
                .reservedFromBatchIds(Arrays.asList(1L))
                .message("Order placed. Inventory reserved.")
                .build();

        when(orderService.placeOrder(any())).thenReturn(response);

        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.status").value("PLACED"));
    }

    @Test
    @DisplayName("POST /order - 400 on failure")
    void placeOrder_Fail() throws Exception {
        OrderRequestDTO request = OrderRequestDTO.builder()
                .productId(1001L)
                .quantity(1000)
                .build();

        OrderResponseDTO response = OrderResponseDTO.builder()
                .productId(1001L)
                .quantity(1000)
                .status("CANCELLED")
                .message("Insufficient inventory")
                .reservedFromBatchIds(new ArrayList<>())
                .build();

        when(orderService.placeOrder(any())).thenReturn(response);

        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
