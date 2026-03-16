package pma.common.exception.CustomException;

import org.springframework.http.HttpStatus;
import pma.common.exception.ApiException;

/**
 * Ném ra khi seed data Role không tồn tại trong DB.
 * Map sang HTTP 500 Internal Server Error vì đây là lỗi cấu hình hệ thống.
 */
public class RoleNotFoundException extends ApiException {
    public RoleNotFoundException(String roleName) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "System role not found: " + roleName + ". Please check database seed data.");
    }
}
