package pma.common.exception.CustomException;

import org.springframework.http.HttpStatus;
import pma.common.exception.ApiException;

public class ProjectNotFoundException extends ApiException {
    public ProjectNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Không tìm thấy dự án");
    }

    public ProjectNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
