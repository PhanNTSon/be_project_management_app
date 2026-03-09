package pma.common.exception.CustomException;

import org.springframework.http.HttpStatus;

import pma.common.exception.ApiException;

public class EmailAlreadyExistException extends ApiException {
    public EmailAlreadyExistException() {
        super(HttpStatus.BAD_REQUEST, "Email đã tồn tại");
    }
}
