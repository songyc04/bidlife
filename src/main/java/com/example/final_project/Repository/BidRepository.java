package com.example.final_project.Repository;

import com.example.final_project.Entity.BidEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<BidEntity, Long> {
    List<BidEntity> findByItemIdOrderByCreatedAtDesc(Long itemId);
    List<BidEntity> findByBidderIdOrderByCreatedAtDesc(Long bidderId);
    List<BidEntity> findByItemIdAndStatusOrderByBidAmountDesc(Long itemId, String status);
}
