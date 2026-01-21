package com.ecom.microservices.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdateResponseDTO {
    
    private boolean success;
    private String productName;
    private List<Long> reservedFromBatchIds;
    private String message;
}
