package com.example.final_project.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "inspections")
@Getter
@Setter
public class InspectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_id", nullable = false, unique = true)
    private Long itemId;

    @Column(nullable = false, length = 20)
    private String status = "IN_PROGRESS";

    @Column(nullable = false, length = 20)
    private String result;

    @Column(length = 10)
    private String grade;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "evidence_images", columnDefinition = "TEXT")
    private String evidenceImages;

    @Column(length = 50)
    private String inspector;

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "IN_PROGRESS";
        }
    }
}
