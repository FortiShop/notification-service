package org.fortishop.notificationservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.fortishop.notificationservice.domain.Notification;
import org.fortishop.notificationservice.domain.NotificationType;
import org.fortishop.notificationservice.dto.response.NotificationResponse;
import org.fortishop.notificationservice.exception.NotificationException;
import org.fortishop.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AdminNotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private AdminNotificationServiceImpl adminService;

    private Notification notification;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        notification = new Notification(1L, NotificationType.ORDER, "주문 완료");
    }

    @Test
    @DisplayName("알림 검색 - 필터 조건 적용")
    void search_filtered() {
        Notification another = new Notification(2L, NotificationType.POINT, "포인트 지급");
        another.markAsRead();
        when(notificationRepository.findAll()).thenReturn(List.of(notification, another));

        List<NotificationResponse> result = adminService.search(1L, "ORDER", "UNREAD");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo("ORDER");
    }

    @Test
    @DisplayName("알림 검색 - 조건 없음 (전체 조회)")
    void search_all() {
        when(notificationRepository.findAll()).thenReturn(List.of(notification));

        List<NotificationResponse> result = adminService.search(null, null, null);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("알림 재전송 - 성공")
    void resend_success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        NotificationResponse response = adminService.resend(1L);

        assertThat(response.getMessage()).isEqualTo(notification.getMessage());
        verify(notificationRepository).findById(1L);
    }

    @Test
    @DisplayName("알림 재전송 - 실패 (존재하지 않음)")
    void resend_notFound() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.resend(1L))
                .isInstanceOf(NotificationException.class);
    }
}
