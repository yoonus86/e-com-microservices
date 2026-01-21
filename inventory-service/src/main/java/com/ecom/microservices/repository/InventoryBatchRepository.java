package com.ecom.microservices.repository;

import com.ecom.microservices.entity.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {

    List<InventoryBatch> findByProductIdOrderByExpiryDateAsc(Long productId);

    @Query("SELECT ib FROM InventoryBatch ib WHERE ib.productId = :productId AND ib.quantity > 0 ORDER BY ib.expiryDate ASC")
    List<InventoryBatch> findAvailableBatchesByProductId(@Param("productId") Long productId);

    @Query("SELECT COALESCE(SUM(ib.quantity), 0) FROM InventoryBatch ib WHERE ib.productId = :productId")
    Integer getTotalQuantityByProductId(@Param("productId") Long productId);
}
