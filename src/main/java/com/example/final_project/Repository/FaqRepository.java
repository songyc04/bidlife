package com.example.final_project.Repository;

import com.example.final_project.Entity.FaqEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaqRepository extends JpaRepository<FaqEntity, Long> {
    List<FaqEntity> findAllByOrderByDisplayOrderAsc();
    List<FaqEntity> findByCategoryOrderByDisplayOrderAsc(String category);
}
