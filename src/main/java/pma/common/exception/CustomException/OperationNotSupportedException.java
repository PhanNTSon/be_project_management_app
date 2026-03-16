package pma.common.exception.CustomException;

import org.springframework.http.HttpStatus;
import pma.common.exception.ApiException;

/**
 * Ném ra khi một operation chưa được implement hoặc không được hỗ trợ.
 * Map sang HTTP 501 Not Implemented.
 */
public class OperationNotSupportedException extends ApiException {
    public OperationNotSupportedException(String operation, String entityType) {
        super(HttpStatus.NOT_IMPLEMENTED, "Operation '" + operation + "' is not yet supported for entity type: " + entityType);
    }
    
    public OperationNotSupportedException(String message) {
        super(HttpStatus.NOT_IMPLEMENTED, message);
    }
}
