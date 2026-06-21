package com.example.final_project.Repository;

import com.example.final_project.Entity.InquiryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<InquiryEntity, Long> {
    List<InquiryEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
