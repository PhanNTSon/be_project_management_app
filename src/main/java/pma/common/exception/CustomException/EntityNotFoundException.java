package pma.common.exception.CustomException;

import org.springframework.http.HttpStatus;
import pma.common.exception.ApiException;

/**
 * Ném ra khi không tìm thấy một entity bất kỳ (Usecase, Actor, VisionScope, v.v.).
 * Map sang HTTP 404 Not Found.
 */
public class EntityNotFoundException extends ApiException {
    public EntityNotFoundException(String entityType, Integer id) {
        super(HttpStatus.NOT_FOUND, entityType + " not found with id: " + id);
    }
    
    public EntityNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
