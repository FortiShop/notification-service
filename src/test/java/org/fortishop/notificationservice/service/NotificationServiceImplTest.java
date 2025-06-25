package org.fortishop.notificationservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.fortishop.notificationservice.domain.Notification;
import org.fortishop.notificationservice.domain.NotificationStatus;
import org.fortishop.notificationservice.domain.NotificationType;
import org.fortishop.notificationservice.dto.response.NotificationResponse;
import org.fortishop.notificationservice.exception.NotificationException;
import org.fortishop.notificationservice.global.SequenceGenerator;
import org.fortishop.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Mock
    private SequenceGenerator sequenceGenerator;

    private Notification notification;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        notification = new Notification(1L, 1L, NotificationType.POINT, "포인트가 적립되었습니다.", "123123123");
    }

    @Test
    @DisplayName("알림 생성 - 성공")
    void createNotification_success() {
        when(sequenceGenerator.generateSequence("notifications_sequence")).thenReturn(1L);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        assertThatCode(() -> notificationService.createNotification(1L, NotificationType.POINT, "테스트 메시지", "123123123"))
                .doesNotThrowAnyException();

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("최근 알림 20개 조회 - 성공")
    void getRecent_success() {
        when(notificationRepository.findTop20ByMemberIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        List<NotificationResponse> result = notificationService.getRecent(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMessage()).isEqualTo(notification.getMessage());
    }

    @Test
    @DisplayName("읽지 않은 알림 개수 조회")
    void getUnreadCount_success() {
        when(notificationRepository.countByMemberIdAndStatus(1L, NotificationStatus.UNREAD)).thenReturn(3L);

        Long count = notificationService.getUnreadCount(1L);

        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("알림 단건 조회 - 성공")
    void getById_success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        NotificationResponse response = notificationService.getById(1L, 1L);

        assertThat(response.getMessage()).isEqualTo(notification.getMessage());
    }

    @Test
    @DisplayName("알림 단건 조회 - 다른 사용자 접근")
    void getById_forbidden() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.getById(999L, 1L))
                .isInstanceOf(NotificationException.class);
    }

    @Test
    @DisplayName("알림 읽음 처리 - ID 목록 비어있음")
    void markAsRead_empty() {
        assertThatThrownBy(() -> notificationService.markAsRead(1L, Collections.emptyList()))
                .isInstanceOf(NotificationException.class);
    }

    @Test
    @DisplayName("알림 삭제 - 성공")
    void delete_success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.delete(1L, 1L);

        verify(notificationRepository, times(1)).delete(notification);
    }

    @Test
    @DisplayName("알림 삭제 - 다른 사용자")
    void delete_forbidden() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.delete(999L, 1L))
                .isInstanceOf(NotificationException.class);
    }
}
