package az.fitnest.order.dto.epoint;

import lombok.Builder;
import java.util.Map;

@Builder
public record EpointResponse(
    String status,
    String transaction,
    String order_id,
    String redirect_url,
    String bank_transaction,
    String rrn,
    String card_name,
    String card_mask,
    Double amount,
    String message,
    String code
) {}
