package org.fortishop.notificationservice.repository;

import java.util.Optional;
import org.fortishop.notificationservice.domain.NotificationTemplate;
import org.fortishop.notificationservice.domain.NotificationType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationTemplateRepository extends MongoRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByType(NotificationType type);
}
