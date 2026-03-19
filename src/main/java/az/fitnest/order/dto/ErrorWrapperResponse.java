package az.fitnest.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record ErrorWrapperResponse(
        @JsonProperty("error") ErrorDetail error
) {
    public static ErrorWrapperResponse of(String code, String message, int status, String path) {
        return new ErrorWrapperResponse(new ErrorDetail(code, message, status, path, LocalDateTime.now()));
    }
    public static ErrorWrapperResponse of(String code, String message, int status, String path, LocalDateTime timestamp) {
        return new ErrorWrapperResponse(new ErrorDetail(code, message, status, path, timestamp));
    }
    public record ErrorDetail(
            String code,
            String message,
            int status,
            String path,
            LocalDateTime timestamp
    ) {}
}
