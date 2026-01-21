package com.ecom.microservices.client;

import com.ecom.microservices.dto.InventoryUpdateRequestDTO;
import com.ecom.microservices.dto.InventoryUpdateResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceClient {

    private final WebClient inventoryServiceWebClient;

    public InventoryUpdateResponseDTO updateInventory(InventoryUpdateRequestDTO request) {
        log.info("Reserving inventory for product {}", request.getProductId());

        try {
            InventoryUpdateResponseDTO response = inventoryServiceWebClient.post()
                    .uri("/inventory/update")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(InventoryUpdateResponseDTO.class)
                    .block();

            log.debug("Inventory update result: {}", response != null && response.isSuccess());
            return response;

        } catch (WebClientResponseException e) {
            log.error("Inventory service error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return InventoryUpdateResponseDTO.builder()
                    .success(false)
                    .message("Error communicating with Inventory Service: " + e.getMessage())
                    .reservedFromBatchIds(new ArrayList<>())
                    .build();
        } catch (Exception e) {
            log.error("Inventory service unavailable: {}", e.getMessage());
            return InventoryUpdateResponseDTO.builder()
                    .success(false)
                    .message("Inventory Service unavailable. Please try again later.")
                    .reservedFromBatchIds(new ArrayList<>())
                    .build();
        }
    }

}
