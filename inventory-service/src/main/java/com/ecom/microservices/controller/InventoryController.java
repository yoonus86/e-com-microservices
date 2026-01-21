package com.ecom.microservices.controller;

import com.ecom.microservices.dto.InventoryResponseDTO;
import com.ecom.microservices.dto.InventoryUpdateRequestDTO;
import com.ecom.microservices.dto.InventoryUpdateResponseDTO;
import com.ecom.microservices.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory", description = "Inventory Management API")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    @Operation(summary = "Get inventory for a product",
               description = "Returns inventory batches sorted by expiry date (FEFO)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = InventoryResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<InventoryResponseDTO> getInventoryByProductId(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        log.info("GET /inventory/{}", productId);
        
        InventoryResponseDTO response = inventoryService.getInventoryByProductId(productId);
        
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update")
    @Operation(summary = "Update inventory", 
               description = "Deducts stock using FEFO (first expiry will be first out)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = InventoryUpdateResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Insufficient stock or bad request")
    })
    public ResponseEntity<InventoryUpdateResponseDTO> updateInventory(
            @RequestBody InventoryUpdateRequestDTO request) {
        log.info("POST /inventory/update - productId: {}, quantity: {}", 
                request.getProductId(), request.getQuantity());
        
        InventoryUpdateResponseDTO response = inventoryService.updateInventory(request);
        
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check/{productId}")
    @Operation(summary = "Check stock availability", 
               description = "Returns true if requested quantity is in stock")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    public ResponseEntity<Boolean> checkAvailability(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Required quantity") @RequestParam Integer quantity) {
        log.info("GET /inventory/check/{} - quantity: {}", productId, quantity);
        
        boolean available = inventoryService.checkAvailability(productId, quantity);
        return ResponseEntity.ok(available);
    }
}
