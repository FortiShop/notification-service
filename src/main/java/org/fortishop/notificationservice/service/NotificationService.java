package org.fortishop.notificationservice.service;

import java.util.List;
import org.fortishop.notificationservice.domain.NotificationType;
import org.fortishop.notificationservice.dto.response.NotificationResponse;

public interface NotificationService {

    void createNotification(Long memberId, NotificationType type, String message);

    List<NotificationResponse> getRecent(Long memberId);

    Long getUnreadCount(Long memberId);

    NotificationResponse getById(Long memberId, Long id);

    void markAsRead(Long memberId, List<Long> ids);

    void delete(Long memberId, Long id);
}
