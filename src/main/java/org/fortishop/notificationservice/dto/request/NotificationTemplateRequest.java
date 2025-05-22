package org.fortishop.notificationservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.fortishop.notificationservice.domain.NotificationType;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationTemplateRequest {
    private NotificationType type;
    private String title;
    private String message;
}
