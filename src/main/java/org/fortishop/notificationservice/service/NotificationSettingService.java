package org.fortishop.notificationservice.service;

import org.fortishop.notificationservice.domain.NotificationType;
import org.fortishop.notificationservice.dto.request.NotificationSettingRequest;
import org.fortishop.notificationservice.dto.response.NotificationSettingResponse;

public interface NotificationSettingService {

    boolean isEnabled(Long memberId, NotificationType type);

    NotificationSettingResponse get(Long memberId);

    void update(Long memberId, NotificationSettingRequest request);
}
