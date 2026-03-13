package pma.common.exception.CustomException;

import org.springframework.http.HttpStatus;

import pma.common.exception.ApiException;

public class UserNotFoundException extends ApiException {
    public UserNotFoundException() {
        super(HttpStatus.BAD_REQUEST, "Không tìm thấy người dùng");
    }

    public UserNotFoundException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
