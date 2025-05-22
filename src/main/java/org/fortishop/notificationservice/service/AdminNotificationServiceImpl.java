package org.fortishop.notificationservice.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fortishop.notificationservice.domain.Notification;
import org.fortishop.notificationservice.dto.response.NotificationResponse;
import org.fortishop.notificationservice.exception.NotificationException;
import org.fortishop.notificationservice.exception.NotificationExceptionType;
import org.fortishop.notificationservice.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminNotificationServiceImpl implements AdminNotificationService {
    private final NotificationRepository notificationRepository;

    /**
     * 검색 조건에 따른 알림 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> search(Long memberId, String type, String status) {
        // QueryDSL로 대체 가능. 지금은 간단한 조합만 지원
        return notificationRepository.findAll().stream()
                .filter(n -> (memberId == null || n.getMemberId().equals(memberId)) &&
                        (type == null || n.getType().name().equalsIgnoreCase(type)) &&
                        (status == null || n.getStatus().name().equalsIgnoreCase(status)))
                .map(NotificationResponse::of)
                .toList();
    }

    /**
     * 알림 재전송 (SSE 연동 시 실시간 전파 포함 예정)
     */
    @Override
    public NotificationResponse resend(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationException(NotificationExceptionType.NOTIFICATION_NOT_FOUND));

        log.info("[관리자] 알림 재전송: memberId={}, message={}", notification.getMemberId(), notification.getMessage());

        return NotificationResponse.of(notification);
    }
}
