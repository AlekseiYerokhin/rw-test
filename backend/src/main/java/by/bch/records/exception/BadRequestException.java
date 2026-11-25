package by.bch.records.exception;

import java.util.Map;

/** 400 — ошибка бизнес-валидации (например, недопустимый файл). */
public class BadRequestException extends RuntimeException {

    /** Может быть null. */
    private final Map<String, String> fieldErrors;

    public BadRequestException(String message) {
        super(message);
        this.fieldErrors = null;
    }

    public BadRequestException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
