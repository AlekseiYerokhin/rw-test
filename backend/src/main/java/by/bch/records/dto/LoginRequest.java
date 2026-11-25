package by.bch.records.dto;

import javax.validation.constraints.NotBlank;

/** Вход: логин. POST /api/auth/login */
public class LoginRequest {

    @NotBlank(message = "Логин обязателен")
    private String login;

    @NotBlank(message = "Пароль обязателен")
    private String password;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
