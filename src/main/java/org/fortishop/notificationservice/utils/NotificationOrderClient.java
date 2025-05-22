package org.fortishop.notificationservice.utils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class NotificationOrderClient {

    private final RestTemplate restTemplate;

    public NotificationOrderClient(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .requestFactory(() -> {
                    var factory = new SimpleClientHttpRequestFactory();
                    factory.setConnectTimeout(3000); // milliseconds
                    factory.setReadTimeout(5000);
                    return factory;
                })
                .build();
    }

    public Long getMemberIdByOrderId(Long orderId) {
        String url = UriComponentsBuilder
                .fromUriString("http://order-payment-service")
                .path("/api/orders/{orderId}")
                .build(orderId)
                .toString();

        try {
            ResponseEntity<OrderResponse> response = restTemplate.getForEntity(url, OrderResponse.class);
            response.getBody();
            return response.getBody().getMemberId();
        } catch (RestClientException e) {
            log.warn("orderId={} 에 대한 주문 정보 조회 실패: {}", orderId, e.getMessage());
            return null;
        }
    }

    @Getter
    @NoArgsConstructor
    private static class OrderResponse {
        private Long orderId;
        private Long memberId;
    }
}
