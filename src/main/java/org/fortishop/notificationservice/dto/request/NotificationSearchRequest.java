package org.fortishop.notificationservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.fortishop.notificationservice.domain.NotificationStatus;
import org.fortishop.notificationservice.domain.NotificationType;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationSearchRequest {
    private Long memberId;
    private NotificationType type;
    private NotificationStatus status;
}
