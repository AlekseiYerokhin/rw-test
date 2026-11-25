package by.bch.records.exception;

/** 401 — неверный логин/пароль или отсутствие сессии. Сообщение всегда нейтральное (FR-AUTH-4). */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("Неверный логин или пароль");
    }
}
