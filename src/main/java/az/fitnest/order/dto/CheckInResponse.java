package az.fitnest.order.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record CheckInResponse(
    boolean success,
    String message,
    Integer remainingVisits,
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "dd/MM/yyyy")
    LocalDateTime checkedInAt
) {}
