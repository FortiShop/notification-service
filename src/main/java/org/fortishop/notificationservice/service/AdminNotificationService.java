package org.fortishop.notificationservice.service;

import java.util.List;
import org.fortishop.notificationservice.dto.response.NotificationResponse;

public interface AdminNotificationService {

    List<NotificationResponse> search(Long memberId, String type, String status);

    NotificationResponse resend(Long id);
}
