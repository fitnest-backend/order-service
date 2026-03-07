package az.fitnest.order.exception;

import org.springframework.http.HttpStatus;

public class ServiceException extends BaseException {

    private static final long serialVersionUID = 1L;

    public ServiceException(String message, String errorCode, HttpStatus httpStatus) {
        super(message, errorCode, httpStatus);
    }
}
