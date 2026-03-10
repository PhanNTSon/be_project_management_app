package pma.common.exception.CustomException;

import org.springframework.http.HttpStatus;

import pma.common.exception.ApiException;

public class ExpiredRefreshTokenException extends ApiException{
    public ExpiredRefreshTokenException(){
        super(HttpStatus.BAD_REQUEST, "Refresh token has expired");
    }
}
