package org.fortishop.notificationservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.fortishop.notificationservice.domain.NotificationTemplate;
import org.fortishop.notificationservice.domain.NotificationType;
import org.fortishop.notificationservice.dto.request.NotificationTemplateRequest;
import org.fortishop.notificationservice.exception.NotificationException;
import org.fortishop.notificationservice.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class NotificationTemplateServiceImplTest {

    @Mock
    private NotificationTemplateRepository templateRepository;

    @InjectMocks
    private NotificationTemplateServiceImpl templateService;

    private NotificationTemplate template;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        template = new NotificationTemplate(1L, NotificationType.ORDER, "주문 알림", "{orderId}번 주문이 결제되었습니다. 결제 금액: {amount}원입니다.", LocalDateTime.now());
    }

    @Test
    @DisplayName("템플릿 등록 - 성공")
    void create_success() {
        NotificationTemplateRequest request = new NotificationTemplateRequest(NotificationType.ORDER, "제목", "{orderId}번 주문이 결제되었습니다. 결제 금액: {amount}원입니다.");

        templateService.create(request);

        verify(templateRepository).save(any(NotificationTemplate.class));
    }

    @Test
    @DisplayName("템플릿 수정 - 성공")
    void update_success() {
        when(templateRepository.findById(1L)).thenReturn(Optional.of(template));

        NotificationTemplateRequest request = new NotificationTemplateRequest(NotificationType.ORDER, "수정된 제목",
                "수정 {orderId}번 주문이 결제되었습니다. 결제 금액: {amount}원입니다.");

        templateService.update(1L, request);

        assertThat(template.getTitle()).isEqualTo("수정된 제목");
        assertThat(template.getMessage()).isEqualTo("수정 {orderId}번 주문이 결제되었습니다. 결제 금액: {amount}원입니다.");
    }

    @Test
    @DisplayName("템플릿 수정 - 존재하지 않음")
    void update_notFound() {
        when(templateRepository.findById(1L)).thenReturn(Optional.empty());

        NotificationTemplateRequest request = new NotificationTemplateRequest(NotificationType.ORDER, "제목", "메시지");

        assertThatThrownBy(() -> templateService.update(1L, request))
                .isInstanceOf(NotificationException.class);
    }

    @Test
    @DisplayName("템플릿 삭제 - 성공")
    void delete_success() {
        when(templateRepository.findById(1L)).thenReturn(Optional.of(template));

        templateService.delete(1L);

        verify(templateRepository).delete(template);
    }

    @Test
    @DisplayName("템플릿 삭제 - 존재하지 않음")
    void delete_notFound() {
        when(templateRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> templateService.delete(1L))
                .isInstanceOf(NotificationException.class);
    }

    @Test
    @DisplayName("전체 템플릿 목록 조회")
    void getAll_success() {
        when(templateRepository.findAll()).thenReturn(List.of(template));

        assertThat(templateService.getAll()).hasSize(1);
    }
}
