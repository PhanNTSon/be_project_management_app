package pma.common.exception.CustomException;

import org.springframework.http.HttpStatus;
import pma.common.exception.ApiException;

/**
 * Ném ra khi không tìm thấy ChangeRequest.
 * Map sang HTTP 404 Not Found.
 */
public class ChangeRequestNotFoundException extends ApiException {
    public ChangeRequestNotFoundException(Integer requestId) {
        super(HttpStatus.NOT_FOUND, "ChangeRequest not found with id: " + requestId);
    }
    
    public ChangeRequestNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
