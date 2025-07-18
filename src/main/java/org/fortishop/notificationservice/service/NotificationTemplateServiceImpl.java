package org.fortishop.notificationservice.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.fortishop.notificationservice.domain.NotificationTemplate;
import org.fortishop.notificationservice.domain.NotificationType;
import org.fortishop.notificationservice.dto.request.NotificationTemplateRequest;
import org.fortishop.notificationservice.dto.response.NotificationTemplateResponse;
import org.fortishop.notificationservice.exception.NotificationException;
import org.fortishop.notificationservice.exception.NotificationExceptionType;
import org.fortishop.notificationservice.global.SequenceGenerator;
import org.fortishop.notificationservice.repository.NotificationTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationTemplateServiceImpl implements NotificationTemplateService {
    private final NotificationTemplateRepository templateRepository;
    private final SequenceGenerator sequenceGenerator;

    /**
     * 템플릿 등록
     */
    @Override
    @Transactional
    public void create(NotificationTemplateRequest request) {
        if (templateRepository.findByType(request.getType()).isPresent()) {
            throw new NotificationException(NotificationExceptionType.TEMPLATE_ALREADY_EXIST);
        }

        long newId = sequenceGenerator.generateSequence("notification_templates_sequence");

        validateMessageTemplate(request.getType(), request.getMessage());

        NotificationTemplate template = new NotificationTemplate(
                newId,
                request.getType(),
                request.getTitle(),
                request.getMessage(),
                null
        );
        templateRepository.save(template);
    }

    /**
     * 템플릿 수정
     */
    @Override
    @Transactional
    public void update(Long id, NotificationTemplateRequest request) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new NotificationException(NotificationExceptionType.TEMPLATE_NOT_FOUND));
        validateMessageTemplate(request.getType(), request.getMessage());
        template.update(request.getTitle(), request.getMessage());
        templateRepository.save(template);
    }

    /**
     * 템플릿 삭제
     */
    @Override
    @Transactional
    public void delete(Long id) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new NotificationException(NotificationExceptionType.TEMPLATE_NOT_FOUND));
        templateRepository.delete(template);
    }

    /**
     * 전체 템플릿 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<NotificationTemplateResponse> getAll() {
        return templateRepository.findAll()
                .stream()
                .map(NotificationTemplateResponse::of)
                .toList();
    }

    private void validateMessageTemplate(NotificationType type, String message) {
        switch (type) {
            case ORDER:
            case POINT:
                if (!message.contains("{orderId}") || !message.contains("{amount}")) {
                    throw new NotificationException(NotificationExceptionType.INVALID_REQUEST);
                }
                break;
            case DELIVERY:
                if (!message.contains("{orderId}")) {
                    throw new NotificationException(NotificationExceptionType.INVALID_REQUEST);
                }
                break;
            default:
                break;
        }
    }
}
