package com.example.final_project.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "items")
@Getter
@Setter
public class ItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer startPrice;

    @Column
    private Integer currentPrice;

    @Column
    private Integer buyNowPrice;

    @Column(nullable = false)
    private Integer bidUnit;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "image_paths", columnDefinition = "TEXT")
    private String imagePaths;

    @Column(name = "winner_id")
    private Long winnerId;

    @Column(name = "final_price")
    private Integer finalPrice;

    @Column(name = "payment_status", length = 20)
    private String paymentStatus;

    @Column(name = "shipping_status", length = 20)
    private String shippingStatus;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "buyer_paid_at")
    private LocalDateTime buyerPaidAt;

    @Column(name = "seller_confirmed_at")
    private LocalDateTime sellerConfirmedAt;

    @Column(name = "payment_key", length = 200)
    private String paymentKey;

    @Column(name = "order_id", length = 100)
    private String orderId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (currentPrice == null) {
            currentPrice = startPrice;
        }
        if (paymentStatus == null) {
            paymentStatus = "pending";
        }
        if (shippingStatus == null) {
            shippingStatus = "pending";
        }
        if (startTime != null && endTime != null) {
            updateTimeBasedStatus();
        } else if (status == null) {
            status = "upcoming";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        updateTimeBasedStatus();
    }

    public void updateTimeBasedStatus() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            status = "upcoming";
        } else if (now.isAfter(endTime)) {
            status = "ended";
        } else {
            status = "bidding";
        }
    }
}
