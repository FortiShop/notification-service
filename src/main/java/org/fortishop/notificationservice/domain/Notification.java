package org.fortishop.notificationservice.domain;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Notification {

    @Id
    private Long id;

    @Field("memberId")
    private Long memberId;

    @Field("type")
    private NotificationType type;

    @Field("message")
    private String message;

    @Field("status")
    private NotificationStatus status;

    @Field("createdAt")
    private LocalDateTime createdAt;

    @Field("traceId")
    private String traceId;

    public Notification(Long newId, Long memberId, NotificationType type, String message, String traceId) {
        this.id = newId;
        this.memberId = memberId;
        this.type = type;
        this.message = message;
        this.status = NotificationStatus.UNREAD;
        this.createdAt = LocalDateTime.now();
        this.traceId = traceId;
    }

    public void markAsRead() {
        this.status = NotificationStatus.READ;
    }
}
