package org.fortishop.notificationservice;

import org.fortishop.notificationservice.utils.NotificationOrderClient;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class NotificationOrderClientMockConfig {
    @Bean
    public NotificationOrderClient notificationOrderClient() {
        return Mockito.mock(NotificationOrderClient.class);
    }
}
