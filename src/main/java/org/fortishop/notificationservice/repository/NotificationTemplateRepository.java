package org.fortishop.notificationservice.repository;

import java.util.Optional;
import org.fortishop.notificationservice.domain.NotificationTemplate;
import org.fortishop.notificationservice.domain.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByType(NotificationType type);
}
