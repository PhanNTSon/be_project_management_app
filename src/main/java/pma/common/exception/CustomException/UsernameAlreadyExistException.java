package pma.common.exception.CustomException;

import org.springframework.http.HttpStatus;

import pma.common.exception.ApiException;

public class UsernameAlreadyExistException extends ApiException {
    public UsernameAlreadyExistException() {
        super(HttpStatus.BAD_REQUEST, "Username already exists");
    }
}
