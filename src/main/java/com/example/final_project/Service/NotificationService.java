package com.example.final_project.Service;

import com.example.final_project.Entity.NotificationEntity;
import com.example.final_project.Repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void createNotification(Long userId, String message, Long itemId, String type) {
        NotificationEntity notification = new NotificationEntity();
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setItemId(itemId);
        notification.setType(type);
        notification.setIsRead(false);
        notificationRepository.save(notification);
    }

    public List<NotificationEntity> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<NotificationEntity> getUnreadNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null) {
            notification.setIsRead(true);
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<NotificationEntity> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        for (NotificationEntity notification : unreadNotifications) {
            notification.setIsRead(true);
        }
    }
}
