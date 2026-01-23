package com.campusplacement.notifications;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Notification entities.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a specific user, ordered by creation date (newest first).
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find all unread notifications for a specific user.
     */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    /**
     * Count unread notifications for a specific user.
     */
    long countByUserIdAndIsReadFalse(Long userId);

    /**
     * Find notifications by type for a specific user.
     */
    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, String type);

    /**
     * Find all notifications (for super admin - system-wide notifications).
     */
    @Query("SELECT n FROM Notification n WHERE n.user IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findSystemNotifications();

    /**
     * Count unread system notifications.
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user IS NULL AND n.isRead = false")
    long countUnreadSystemNotifications();

    /**
     * Find all notifications for a user or system-wide notifications.
     */
    @Query("SELECT n FROM Notification n WHERE (n.user.id = :userId OR n.user IS NULL) ORDER BY n.createdAt DESC")
    List<Notification> findUserAndSystemNotifications(@Param("userId") Long userId);

    /**
     * Count unread notifications for a user or system-wide.
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE ((n.user IS NOT NULL AND n.user.id = :userId) OR n.user IS NULL) AND n.isRead = false")
    long countUnreadUserAndSystemNotifications(@Param("userId") Long userId);
}
