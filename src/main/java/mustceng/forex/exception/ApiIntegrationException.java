package mustceng.forex.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ApiIntegrationException extends RuntimeException {
    public ApiIntegrationException(String message) {
        super(message);
    }

    public ApiIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
