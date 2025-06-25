package org.fortishop.notificationservice.service;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fortishop.notificationservice.domain.Notification;
import org.fortishop.notificationservice.domain.NotificationStatus;
import org.fortishop.notificationservice.domain.NotificationType;
import org.fortishop.notificationservice.dto.response.NotificationResponse;
import org.fortishop.notificationservice.exception.NotificationException;
import org.fortishop.notificationservice.exception.NotificationExceptionType;
import org.fortishop.notificationservice.global.SequenceGenerator;
import org.fortishop.notificationservice.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final SequenceGenerator sequenceGenerator;

    @Override
    @Transactional
    public void createNotification(Long memberId, NotificationType type, String message, String traceId) {
        long newId = sequenceGenerator.generateSequence("notifications_sequence");
        Notification notification = new Notification(newId, memberId, type, message, traceId);
        notificationRepository.save(notification);
        log.info("알림 생성 완료 - memberId={}, type={}, message={}, traceId={}", memberId, type, message, traceId);
    }

    /**
     * 사용자 최근 알림 20개 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getRecent(Long memberId) {
        return notificationRepository.findTop20ByMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(NotificationResponse::of)
                .toList();
    }

    /**
     * 안 읽은 알림 개수
     */
    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long memberId) {
        return notificationRepository.countByMemberIdAndStatus(memberId, NotificationStatus.UNREAD);
    }

    /**
     * 알림 단건 조회
     */
    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getById(Long memberId, Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationException(NotificationExceptionType.NOTIFICATION_NOT_FOUND));

        if (!notification.getMemberId().equals(memberId)) {
            throw new NotificationException(NotificationExceptionType.WRONG_ROLE);
        }

        return NotificationResponse.of(notification);
    }

    /**
     * 읽음 처리
     */
    @Transactional
    public void markAsRead(Long memberId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new NotificationException(NotificationExceptionType.ID_IS_EMPTY);
        }

        List<Notification> notifications = notificationRepository.findAllById(ids).stream()
                .filter(n -> Objects.equals(n.getMemberId(), memberId))
                .filter(n -> NotificationStatus.UNREAD.equals(n.getStatus()))
                .toList();

        notifications.forEach(Notification::markAsRead);

        notificationRepository.saveAll(notifications);
    }

    /**
     * 단건 삭제
     */
    @Override
    @Transactional
    public void delete(Long memberId, Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationException(NotificationExceptionType.NOTIFICATION_NOT_FOUND));

        if (!notification.getMemberId().equals(memberId)) {
            throw new NotificationException(NotificationExceptionType.WRONG_ROLE);
        }

        notificationRepository.delete(notification);
    }
}
