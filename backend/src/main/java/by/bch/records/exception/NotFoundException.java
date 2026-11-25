package by.bch.records.exception;

/** 404 — запись/картинка не найдена. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
