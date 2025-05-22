package org.fortishop.notificationservice.service;

import java.util.List;
import org.fortishop.notificationservice.dto.request.NotificationTemplateRequest;
import org.fortishop.notificationservice.dto.response.NotificationTemplateResponse;

public interface NotificationTemplateService {

    void create(NotificationTemplateRequest request);

    void update(Long id, NotificationTemplateRequest request);

    void delete(Long id);

    List<NotificationTemplateResponse> getAll();
}
