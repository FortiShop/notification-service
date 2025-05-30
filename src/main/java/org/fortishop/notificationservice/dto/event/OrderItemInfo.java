package org.fortishop.notificationservice.dto.event;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.fortishop.orderpaymentservice.domain.OrderItem;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemInfo {
    private Long productId;
    private int quantity;
    private BigDecimal price;
}
