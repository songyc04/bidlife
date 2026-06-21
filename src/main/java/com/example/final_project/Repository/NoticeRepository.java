package com.example.final_project.Repository;

import com.example.final_project.Entity.NoticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {
    List<NoticeEntity> findAllByOrderByIsImportantDescCreatedAtDesc();
    List<NoticeEntity> findTop5ByOrderByIsImportantDescCreatedAtDesc();

    @Modifying
    @Query("UPDATE NoticeEntity n SET n.viewCount = n.viewCount + 1 WHERE n.id = :id")
    void incrementViewCount(@Param("id") Long id);
}
