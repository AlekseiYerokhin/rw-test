package by.bch.records.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Единый формат ошибки API (ТЗ п. 6.3, В.11):
 * { "status": ..., "message": "...", "fieldErrors": {...}, "timestamp": ... }
 */
public class ApiError {

    private final int status;
    private final String message;
    /** Ошибки валидации {поле: сообщение}; null, если не ошибка валидации. */
    private final Map<String, String> fieldErrors;
    private final Instant timestamp = Instant.now();

    private ApiError(int status, String message, Map<String, String> fieldErrors) {
        this.status = status;
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

    public static ApiError of(int status, String message) {
        return new ApiError(status, message, null);
    }

    public static ApiError of(int status, String message, Map<String, String> fieldErrors) {
        return new ApiError(status, message, fieldErrors);
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
