package pma.common.exception.CustomException;

import org.springframework.http.HttpStatus;

import pma.common.exception.ApiException;

public class InvalidPasswordException extends ApiException{
    public InvalidPasswordException(){
        super(HttpStatus.BAD_REQUEST, "Sai mật khẩu");
    }
}
