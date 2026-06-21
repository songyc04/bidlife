package com.example.final_project.Repository;

import com.example.final_project.Entity.DisputeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisputeRepository extends JpaRepository<DisputeEntity, Long> {
    List<DisputeEntity> findByItemIdOrderByCreatedAtDesc(Long itemId);
    List<DisputeEntity> findByReporterIdOrderByCreatedAtDesc(Long reporterId);
    List<DisputeEntity> findAllByOrderByCreatedAtDesc();
    List<DisputeEntity> findByStatusOrderByCreatedAtDesc(String status);
}
