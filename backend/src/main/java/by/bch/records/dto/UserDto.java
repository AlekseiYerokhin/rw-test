package by.bch.records.dto;

/**
 * Выход: текущий пользователь.
 * Пароль (даже хэш) сюда не входит — NFR-SEC-3.
 */
public class UserDto {

    private String login;
    private String displayName;

    public UserDto(String login, String displayName) {
        this.login = login;
        this.displayName = displayName;
    }

    public String getLogin() {
        return login;
    }

    public String getDisplayName() {
        return displayName;
    }
}
