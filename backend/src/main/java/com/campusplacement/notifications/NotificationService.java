package com.campusplacement.notifications;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.notifications.dto.NotificationDTO;
import com.campusplacement.users.User;
import com.campusplacement.users.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing notifications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Get all notifications for a user (including system-wide notifications).
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findUserAndSystemNotifications(userId);
        return notifications.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get unread notifications for a user.
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findUserAndSystemNotifications(userId);
        return notifications.stream()
                .filter(n -> !n.getIsRead())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get count of unread notifications.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadUserAndSystemNotifications(userId);
    }

    /**
     * Mark a notification as read.
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        @SuppressWarnings("null")
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        // Verify ownership (user-specific or system-wide)
        if (notification.getUser() != null && !notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Cannot mark this notification as read");
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
        log.debug("Marked notification {} as read for user {}", notificationId, userId);
    }

    /**
     * Mark all notifications as read for a user.
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findUserAndSystemNotifications(userId);
        LocalDateTime now = LocalDateTime.now();
        notifications.stream()
                .filter(n -> !n.getIsRead())
                .forEach(n -> {
                    n.setIsRead(true);
                    n.setReadAt(now);
                });
        notificationRepository.saveAll(notifications);
        log.debug("Marked all notifications as read for user {}", userId);
    }

    /**
     * Create a notification for a specific user.
     */
    @Transactional
    public NotificationDTO createUserNotification(Long userId, String type, String title, String message, String link) {
        @SuppressWarnings("null")
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .link(link)
                .isRead(false)
                .build();

        @SuppressWarnings("null")
        Notification saved = notificationRepository.save(notification);
        log.debug("Created notification {} for user {}", saved.getId(), userId);
        return toDTO(saved);
    }

    /**
     * Create a system-wide notification (for super admin).
     */
    @Transactional
    public NotificationDTO createSystemNotification(String type, String title, String message, String link) {
        Notification notification = Notification.builder()
                .user(null) // System-wide notification
                .type(type)
                .title(title)
                .message(message)
                .link(link)
                .isRead(false)
                .build();

        @SuppressWarnings("null")
        Notification saved = notificationRepository.save(notification);
        log.debug("Created system notification {}", saved.getId());
        return toDTO(saved);
    }

    /**
     * Convert Notification entity to DTO.
     */
    private NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .link(notification.getLink())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
