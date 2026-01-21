package com.ecom.microservices.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchDTO {
    
    private Long batchId;
    private Integer quantity;
    private LocalDate expiryDate;
}
