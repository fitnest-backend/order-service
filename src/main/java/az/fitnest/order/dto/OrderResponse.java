package az.fitnest.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Schema(description = "Order response details")
public class OrderResponse {
    @Schema(description = "Order ID")
    private String orderId;
    @Schema(description = "Order type")
    private String type;
    @Schema(description = "Order status")
    private String status;
    @Schema(description = "Order amount")
    private BigDecimal amount;
    @Schema(description = "Order currency")
    private String currency;
    @Schema(description = "Order creation time")
    private Instant createdAt;
}

