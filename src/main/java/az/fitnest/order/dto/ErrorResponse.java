package az.fitnest.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponse(
    @JsonProperty("error") ErrorDetail error
) {
    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
    }

    public static class ErrorResponseBuilder {
        private ErrorDetail error;
        public ErrorResponseBuilder() {}
        public ErrorResponseBuilder error(ErrorDetail error) {
            this.error = error;
            return this;
        }
        public ErrorResponse build() {
            return new ErrorResponse(error);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorDetail(
        String code,
        String message,
        Integer status,
        String path,
        OffsetDateTime timestamp,
        Map<String, Object> details
    ) {
        public static ErrorDetailBuilder builder() {
            return new ErrorDetailBuilder();
        }

        public static class ErrorDetailBuilder {
            private String code;
            private String message;
            private Integer status;
            private String path;
            private OffsetDateTime timestamp;
            private Map<String, Object> details;

            public ErrorDetailBuilder() {}

            public ErrorDetailBuilder code(String code) {
                this.code = code;
                return this;
            }
            public ErrorDetailBuilder message(String message) {
                this.message = message;
                return this;
            }
            public ErrorDetailBuilder status(Integer status) {
                this.status = status;
                return this;
            }
            public ErrorDetailBuilder path(String path) {
                this.path = path;
                return this;
            }
            public ErrorDetailBuilder timestamp(OffsetDateTime timestamp) {
                this.timestamp = timestamp;
                return this;
            }
            public ErrorDetailBuilder details(Map<String, Object> details) {
                this.details = details;
                return this;
            }
            public ErrorDetail build() {
                return new ErrorDetail(code, message, status, path, timestamp, details);
            }
        }
    }
}
