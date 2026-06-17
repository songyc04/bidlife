package com.example.final_project.Repository;

import com.example.final_project.Entity.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, Long> {
    List<ItemEntity> findAllByOrderByCreatedAtDesc();
    List<ItemEntity> findAllBySellerIdNotOrderByCreatedAtDesc(Long sellerId);
    List<ItemEntity> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
    long countByStatus(String status);
}
