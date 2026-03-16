package pma.common.exception.CustomException;

import org.springframework.http.HttpStatus;

import pma.common.exception.ApiException;

public class InvalidRefreshTokenException extends ApiException {
    public InvalidRefreshTokenException() {
        super(HttpStatus.BAD_REQUEST, "Invalid refresh token");
    }
}
