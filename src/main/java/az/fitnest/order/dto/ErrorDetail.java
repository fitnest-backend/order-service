package az.fitnest.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record ErrorDetail(
    @JsonProperty("code") String code,
    @JsonProperty("message") String message,
    @JsonProperty("status") int status,
    @JsonProperty("path") String path,
    @JsonProperty("timestamp") LocalDateTime timestamp
) {}
