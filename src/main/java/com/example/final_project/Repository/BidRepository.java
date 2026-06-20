package com.example.final_project.Repository;

import com.example.final_project.Entity.BidEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<BidEntity, Long> {
    List<BidEntity> findByItemIdOrderByCreatedAtDesc(Long itemId);
    List<BidEntity> findByBidderIdOrderByCreatedAtDesc(Long bidderId);
    List<BidEntity> findByItemIdAndStatusOrderByBidAmountDesc(Long itemId, String status);
    void deleteByItemId(Long itemId);

    @Query("SELECT COUNT(DISTINCT b.bidderId) FROM BidEntity b WHERE b.itemId = :itemId")
    long countDistinctBidderByItemId(@Param("itemId") Long itemId);

    @Query("SELECT b FROM BidEntity b WHERE b.itemId = :itemId AND b.status = 'active' ORDER BY b.bidAmount DESC LIMIT 1")
    Optional<BidEntity> findHighestActiveBidByItemId(@Param("itemId") Long itemId);

    List<BidEntity> findByItemId(Long itemId);

    @Modifying
    @Query("UPDATE BidEntity b SET b.status = 'outbid' " +
           "WHERE b.itemId = :itemId AND b.status = 'active' AND b.bidderId <> :excludeBidderId")
    int markAllAsOutbidExcluding(@Param("itemId") Long itemId, @Param("excludeBidderId") Long excludeBidderId);

    @Modifying
    @Query("UPDATE BidEntity b SET b.status = 'outbid' " +
           "WHERE b.itemId = :itemId AND b.status = 'active'")
    int markAllAsOutbid(@Param("itemId") Long itemId);
}
