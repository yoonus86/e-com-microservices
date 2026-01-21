package com.ecom.microservices.service;

import com.ecom.microservices.client.InventoryServiceClient;
import com.ecom.microservices.dto.InventoryUpdateResponseDTO;
import com.ecom.microservices.dto.OrderRequestDTO;
import com.ecom.microservices.dto.OrderResponseDTO;
import com.ecom.microservices.entity.Order;
import com.ecom.microservices.entity.OrderStatus;
import com.ecom.microservices.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryServiceClient inventoryServiceClient;

    @InjectMocks
    private OrderService orderService;

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        sampleOrder = Order.builder()
                .orderId(1L)
                .productId(1001L)
                .productName("Laptop")
                .quantity(10)
                .status(OrderStatus.PLACED)
                .orderDate(LocalDate.now())
                .reservedFromBatchIds("1,2")
                .build();
    }

    @Test
    @DisplayName("place order - success")
    void placeOrder_Success() {
        OrderRequestDTO request = OrderRequestDTO.builder()
                .productId(1001L)
                .quantity(10)
                .build();

        InventoryUpdateResponseDTO inventoryResponse = InventoryUpdateResponseDTO.builder()
                .success(true)
                .productName("Laptop")
                .reservedFromBatchIds(Arrays.asList(1L))
                .message("Inventory reserved successfully.")
                .build();

        when(inventoryServiceClient.updateInventory(any())).thenReturn(inventoryResponse);
        when(orderRepository.save(any())).thenReturn(sampleOrder);

        OrderResponseDTO response = orderService.placeOrder(request);

        assertNotNull(response);
        assertEquals(1L, response.getOrderId());
        assertEquals("PLACED", response.getStatus());
        verify(orderRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("place order - fails when no stock")
    void placeOrder_Fail() {
        OrderRequestDTO request = OrderRequestDTO.builder()
                .productId(1001L)
                .quantity(1000)
                .build();

        InventoryUpdateResponseDTO inventoryResponse = InventoryUpdateResponseDTO.builder()
                .success(false)
                .message("Insufficient inventory")
                .reservedFromBatchIds(new ArrayList<>())
                .build();

        when(inventoryServiceClient.updateInventory(any())).thenReturn(inventoryResponse);

        OrderResponseDTO response = orderService.placeOrder(request);

        assertNull(response.getOrderId());
        assertEquals("CANCELLED", response.getStatus());
        verify(orderRepository, never()).save(any());
    }
}
