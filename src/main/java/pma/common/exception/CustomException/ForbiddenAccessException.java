package pma.common.exception.CustomException;

import org.springframework.http.HttpStatus;
import pma.common.exception.ApiException;

public class ForbiddenAccessException extends ApiException {
    public ForbiddenAccessException() {
        super(HttpStatus.FORBIDDEN, "You do not have permission to modify this resource.");
    }
    
    public ForbiddenAccessException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
