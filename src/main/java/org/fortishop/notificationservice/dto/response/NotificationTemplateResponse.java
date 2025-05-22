package org.fortishop.notificationservice.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.fortishop.notificationservice.domain.NotificationTemplate;

@Getter
@AllArgsConstructor
public class NotificationTemplateResponse {
    private Long id;
    private String type;
    private String title;
    private String message;
    private LocalDateTime createdAt;

    public static NotificationTemplateResponse of(NotificationTemplate t) {
        return new NotificationTemplateResponse(
                t.getId(),
                t.getType().name(),
                t.getTitle(),
                t.getMessage(),
                t.getCreatedAt()
        );
    }
}
