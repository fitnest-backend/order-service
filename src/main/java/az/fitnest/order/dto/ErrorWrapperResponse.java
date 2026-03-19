package az.fitnest.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ErrorWrapperResponse(
    @JsonProperty("error") ErrorDetail error
) {
    public static ErrorWrapperResponse of(String code, String message, int status, String path) {
        return new ErrorWrapperResponse(
            new ErrorDetail(code, message, status, path, java.time.LocalDateTime.now())
        );
    }
}
