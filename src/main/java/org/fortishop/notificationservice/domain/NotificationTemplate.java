package org.fortishop.notificationservice.domain;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "notification_templates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class NotificationTemplate {

    @Id
    private Long id;

    @Field("type")
    private NotificationType type;

    @Field("title")
    private String title;

    @Field("message")
    private String message;

    private LocalDateTime createdAt = LocalDateTime.now();

    public void update(String title, String message) {
        this.title = title;
        this.message = message;
    }
}
