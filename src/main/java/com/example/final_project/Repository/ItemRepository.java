package com.example.final_project.Repository;

import com.example.final_project.Entity.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<ItemEntity, Long> {
    List<ItemEntity> findAllByOrderByCreatedAtDesc();
    List<ItemEntity> findAllBySellerIdNotOrderByCreatedAtDesc(Long sellerId);
    List<ItemEntity> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
    List<ItemEntity> findByWinnerIdOrderByTransactionDateDesc(Long winnerId);
    List<ItemEntity> findBySellerIdAndStatusOrderByTransactionDateDesc(Long sellerId, String status);
    List<ItemEntity> findByPaymentStatusOrderByFinalPriceDesc(String paymentStatus);
    long countByStatus(String status);
    Optional<ItemEntity> findByOrderId(String orderId);
}
