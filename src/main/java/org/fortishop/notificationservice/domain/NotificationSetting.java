package org.fortishop.notificationservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class NotificationSetting {

    @Id
    private Long memberId;

    private boolean orderEnabled = true;
    private boolean deliveryEnabled = true;
    private boolean pointEnabled = true;
    private boolean systemEnabled = true;

    public void update(NotificationType type, boolean enabled) {
        switch (type) {
            case ORDER -> this.orderEnabled = enabled;
            case DELIVERY -> this.deliveryEnabled = enabled;
            case POINT -> this.pointEnabled = enabled;
            case SYSTEM -> this.systemEnabled = enabled;
        }
    }

    public boolean isEnabled(NotificationType type) {
        return switch (type) {
            case ORDER -> orderEnabled;
            case DELIVERY -> deliveryEnabled;
            case POINT -> pointEnabled;
            case SYSTEM -> systemEnabled;
        };
    }
}
