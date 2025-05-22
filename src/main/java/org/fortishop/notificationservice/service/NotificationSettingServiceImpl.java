package org.fortishop.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.fortishop.notificationservice.domain.NotificationSetting;
import org.fortishop.notificationservice.domain.NotificationType;
import org.fortishop.notificationservice.dto.request.NotificationSettingRequest;
import org.fortishop.notificationservice.dto.response.NotificationSettingResponse;
import org.fortishop.notificationservice.repository.NotificationSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationSettingServiceImpl implements NotificationSettingService {
    private final NotificationSettingRepository settingRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean isEnabled(Long memberId, NotificationType type) {
        return settingRepository.findById(memberId)
                .map(setting -> setting.isEnabled(type))
                .orElse(true);
    }

    /**
     * 수신 설정 조회 (없으면 기본 생성)
     */
    @Override
    @Transactional(readOnly = true)
    public NotificationSettingResponse get(Long memberId) {
        NotificationSetting setting = settingRepository.findById(memberId)
                .orElse(new NotificationSetting(memberId, true, true, true, true));

        return NotificationSettingResponse.of(setting);
    }

    /**
     * 수신 설정 변경
     */
    @Override
    @Transactional
    public void update(Long memberId, NotificationSettingRequest request) {
        NotificationSetting setting = settingRepository.findById(memberId)
                .orElse(new NotificationSetting(memberId, true, true, true, true));

        setting.update(request.getType(), request.isEnabled());
        settingRepository.save(setting);
    }
}
