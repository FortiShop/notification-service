package org.fortishop.notificationservice.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.fortishop.notificationservice.domain.NotificationSetting;

@Getter
@AllArgsConstructor
public class NotificationSettingResponse {

    private boolean orderEnabled;
    private boolean deliveryEnabled;
    private boolean pointEnabled;
    private boolean systemEnabled;

    public static NotificationSettingResponse of(NotificationSetting setting) {
        return new NotificationSettingResponse(
                setting.isOrderEnabled(),
                setting.isDeliveryEnabled(),
                setting.isPointEnabled(),
                setting.isSystemEnabled()
        );
    }
}
