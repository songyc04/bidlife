package com.example.final_project.Repository;

import com.example.final_project.Entity.InspectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InspectionRepository extends JpaRepository<InspectionEntity, Long> {
    Optional<InspectionEntity> findByItemId(Long itemId);
    List<InspectionEntity> findAllByOrderByStartedAtDesc();
    List<InspectionEntity> findByStatusOrderByStartedAtDesc(String status);
}
