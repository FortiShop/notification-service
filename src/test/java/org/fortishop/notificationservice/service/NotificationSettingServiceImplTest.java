package org.fortishop.notificationservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.fortishop.notificationservice.domain.NotificationSetting;
import org.fortishop.notificationservice.domain.NotificationType;
import org.fortishop.notificationservice.dto.request.NotificationSettingRequest;
import org.fortishop.notificationservice.dto.response.NotificationSettingResponse;
import org.fortishop.notificationservice.repository.NotificationSettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class NotificationSettingServiceImplTest {

    @Mock
    private NotificationSettingRepository settingRepository;

    @InjectMocks
    private NotificationSettingServiceImpl settingService;

    private NotificationSetting defaultSetting;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        defaultSetting = new NotificationSetting(1L, true, true, true, true);
    }

    @Test
    @DisplayName("알림 설정 조회 - 기존 설정 존재")
    void get_existing_setting() {
        when(settingRepository.findById(1L)).thenReturn(Optional.of(defaultSetting));

        NotificationSettingResponse response = settingService.get(1L);

        assertThat(response.isOrderEnabled()).isTrue();
        assertThat(response.isDeliveryEnabled()).isTrue();
    }

    @Test
    @DisplayName("알림 설정 조회 - 설정 없음 → 기본 반환")
    void get_default_setting() {
        when(settingRepository.findById(1L)).thenReturn(Optional.empty());

        NotificationSettingResponse response = settingService.get(1L);

        assertThat(response.isPointEnabled()).isTrue();
        assertThat(response.isSystemEnabled()).isTrue();
    }

    @Test
    @DisplayName("알림 설정 변경 - 기존 설정 수정")
    void update_existing_setting() {
        when(settingRepository.findById(1L)).thenReturn(Optional.of(defaultSetting));

        NotificationSettingRequest request = new NotificationSettingRequest(NotificationType.ORDER, false);

        settingService.update(1L, request);

        assertThat(defaultSetting.isOrderEnabled()).isFalse();
        verify(settingRepository).save(defaultSetting);
    }

    @Test
    @DisplayName("알림 설정 변경 - 새 설정 생성")
    void update_create_setting() {
        when(settingRepository.findById(1L)).thenReturn(Optional.empty());

        NotificationSettingRequest request = new NotificationSettingRequest(NotificationType.DELIVERY, false);

        settingService.update(1L, request);

        verify(settingRepository).save(argThat(setting ->
                setting.getMemberId().equals(1L) &&
                        !setting.isDeliveryEnabled()
        ));
    }

    @Test
    @DisplayName("알림 수신 여부 확인 - 설정 존재")
    void isEnabled_existing() {
        defaultSetting.update(NotificationType.POINT, false);
        when(settingRepository.findById(1L)).thenReturn(Optional.of(defaultSetting));

        boolean result = settingService.isEnabled(1L, NotificationType.POINT);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("알림 수신 여부 확인 - 설정 없음 (기본 허용)")
    void isEnabled_default_true() {
        when(settingRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = settingService.isEnabled(1L, NotificationType.SYSTEM);
        assertThat(result).isTrue();
    }
}
