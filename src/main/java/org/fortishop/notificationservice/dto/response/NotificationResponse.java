package org.fortishop.notificationservice.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.fortishop.notificationservice.domain.Notification;

@Getter
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String type;
    private String message;
    private String status;
    private LocalDateTime createdAt;

    public static NotificationResponse of(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType().name(),
                n.getMessage(),
                n.getStatus().name(),
                n.getCreatedAt()
        );
    }
}
