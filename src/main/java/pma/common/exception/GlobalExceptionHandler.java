package pma.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.jsonwebtoken.JwtException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = JwtException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Token không hợp lệ hoặc đã hết hạn");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {

        ErrorResponse error = new ErrorResponse(
                ex.getStatus().value(),
                ex.getMessage());

        return ResponseEntity
                .status(ex.getStatus())
                .body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

}
