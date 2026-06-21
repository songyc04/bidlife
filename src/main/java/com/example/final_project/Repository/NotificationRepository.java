package com.example.final_project.Repository;

import com.example.final_project.Entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<NotificationEntity> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    @Query("SELECT COUNT(n) FROM NotificationEntity n WHERE n.userId = :userId AND (n.isRead = false OR n.isRead IS NULL)")
    long countUnreadByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.id = :id")
    int markAsReadById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.userId = :userId AND (n.isRead = false OR n.isRead IS NULL)")
    int markAllAsReadByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM NotificationEntity n WHERE n.userId = :userId AND n.isRead = true")
    int deleteByUserIdAndIsReadTrue(@Param("userId") Long userId);

    void deleteByItemId(Long itemId);
}
