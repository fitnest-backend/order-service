package az.fitnest.order.exception;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import az.fitnest.order.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException exception, WebRequest request) {

        ErrorResponse.ErrorDetail.ErrorDetailBuilder detailBuilder = ErrorResponse.ErrorDetail.builder()
                .message(exception.getMessage())
                .code(exception.getErrorCode())
                .status(exception.getHttpStatus().value())
                .timestamp(OffsetDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""));

        if (exception instanceof ValidationException) {
            ValidationException validationException = (ValidationException) exception;
            BindingResult result = validationException.getBindingResult();
            if (result != null) {
                Map<String, Object> details = new HashMap<>();
                Map<String, String> validationErrors = new HashMap<>();
                for (FieldError error : result.getFieldErrors()) {
                    validationErrors.put(error.getField(), error.getDefaultMessage());
                }
                details.put("validationErrors", validationErrors);
                detailBuilder.details(details);
            }
        }

        ErrorResponse errorResponse = ErrorResponse.builder().error(detailBuilder.build()).build();
        return ResponseEntity.status(exception.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception, WebRequest request) {

        BindingResult result = exception.getBindingResult();
        Map<String, Object> details = new HashMap<>();
        Map<String, String> validationErrors = new HashMap<>();

        for (FieldError error : result.getFieldErrors()) {
            validationErrors.put(error.getField(), error.getDefaultMessage());
        }
        details.put("validationErrors", validationErrors);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(ErrorResponse.ErrorDetail.builder()
                        .message("Doğrulama xətası")
                        .code("VALIDATION_ERROR")
                        .status(HttpStatus.BAD_REQUEST.value())
                        .timestamp(OffsetDateTime.now())
                        .path(request.getDescription(false).replace("uri=", ""))
                        .details(details)
                        .build())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(ErrorResponse.ErrorDetail.builder()
                        .message("Yanlış sorğu formatı")
                        .code("HTTP_MESSAGE_NOT_READABLE")
                        .status(HttpStatus.BAD_REQUEST.value())
                        .timestamp(OffsetDateTime.now())
                        .path(request.getDescription(false).replace("uri=", ""))
                        .build())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(ErrorResponse.ErrorDetail.builder()
                        .message("Daxili server xətası")
                        .code("RUNTIME_EXCEPTION")
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .timestamp(OffsetDateTime.now())
                        .path(request.getDescription(false).replace("uri=", ""))
                        .build())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(ErrorResponse.ErrorDetail.builder()
                        .message("Internal server error")
                        .code("INTERNAL_SERVER_ERROR")
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .timestamp(OffsetDateTime.now())
                        .path(request.getDescription(false).replace("uri=", ""))
                        .build())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
