package com.ecom.microservices.service;

import com.ecom.microservices.client.InventoryServiceClient;
import com.ecom.microservices.dto.InventoryUpdateRequestDTO;
import com.ecom.microservices.dto.InventoryUpdateResponseDTO;
import com.ecom.microservices.dto.OrderRequestDTO;
import com.ecom.microservices.dto.OrderResponseDTO;
import com.ecom.microservices.entity.Order;
import com.ecom.microservices.entity.OrderStatus;
import com.ecom.microservices.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryServiceClient inventoryServiceClient;

    @Transactional
    public OrderResponseDTO placeOrder(OrderRequestDTO request) {
        log.info("Placing order for product ID: {}, quantity: {}", 
                request.getProductId(), request.getQuantity());

        if (request.getProductId() == null || request.getQuantity() == null || request.getQuantity() <= 0) {
            return OrderResponseDTO.builder()
                    .message("Invalid order request. Product ID and positive quantity are required.")
                    .reservedFromBatchIds(new ArrayList<>())
                    .build();
        }

        // Call Inventory Service to reserve inventory
        InventoryUpdateRequestDTO inventoryRequest = InventoryUpdateRequestDTO.builder()
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .build();

        InventoryUpdateResponseDTO inventoryResponse = inventoryServiceClient.updateInventory(inventoryRequest);

        if (!inventoryResponse.isSuccess()) {
            log.warn("Failed to reserve inventory: {}", inventoryResponse.getMessage());
            return OrderResponseDTO.builder()
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .status(OrderStatus.CANCELLED.name())
                    .message("Order failed: " + inventoryResponse.getMessage())
                    .reservedFromBatchIds(new ArrayList<>())
                    .build();
        }

        // Create and save order
        Order order = Order.builder()
                .productId(request.getProductId())
                .productName(inventoryResponse.getProductName())
                .quantity(request.getQuantity())
                .status(OrderStatus.PLACED)
                .orderDate(LocalDate.now())
                .reservedFromBatchIds(convertBatchIdsToString(inventoryResponse.getReservedFromBatchIds()))
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order placed successfully with ID: {}", savedOrder.getOrderId());

        return OrderResponseDTO.builder()
                .orderId(savedOrder.getOrderId())
                .productId(savedOrder.getProductId())
                .productName(savedOrder.getProductName())
                .quantity(savedOrder.getQuantity())
                .status(savedOrder.getStatus().name())
                .reservedFromBatchIds(inventoryResponse.getReservedFromBatchIds())
                .message("Order placed. Inventory reserved.")
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<OrderResponseDTO> getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByProductId(Long productId) {
        return orderRepository.findByProductId(productId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    private OrderResponseDTO convertToResponseDTO(Order order) {
        return OrderResponseDTO.builder()
                .orderId(order.getOrderId())
                .productId(order.getProductId())
                .productName(order.getProductName())
                .quantity(order.getQuantity())
                .status(order.getStatus().name())
                .reservedFromBatchIds(convertStringToBatchIds(order.getReservedFromBatchIds()))
                .build();
    }

    private String convertBatchIdsToString(List<Long> batchIds) {
        if (batchIds == null || batchIds.isEmpty()) {
            return null;
        }
        return batchIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private List<Long> convertStringToBatchIds(String batchIdsString) {
        if (batchIdsString == null || batchIdsString.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> batchIds = new ArrayList<>();
        for (String id : batchIdsString.split(",")) {
            try {
                batchIds.add(Long.parseLong(id.trim()));
            } catch (NumberFormatException e) {
                log.warn("Invalid batch ID in string: {}", id);
            }
        }
        return batchIds;
    }
}
