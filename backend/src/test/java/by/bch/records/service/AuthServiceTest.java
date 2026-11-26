package by.bch.records.service;

import by.bch.records.entity.AppUser;
import by.bch.records.exception.UnauthorizedException;
import by.bch.records.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/** Юнит-тесты AuthService (FR-AUTH-2, FR-AUTH-4, NFR-SEC-3). */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private AppUser user;

    @BeforeEach
    void setUp() {
        user = new AppUser();
        user.setId(1L);
        user.setLogin("admin");
        user.setPasswordHash(encoder.encode("admin123"));
        user.setDisplayName("Администратор");
    }

    @Test
    @DisplayName("authenticate: верные логин/пароль -> пользователь")
    void authenticateOk() {
        when(userRepository.findByLogin("admin")).thenReturn(Optional.of(user));
        AppUser result = authService.authenticate("admin", "admin123");
        assertEquals("admin", result.getLogin());
        assertEquals("Администратор", result.getDisplayName());
    }

    @Test
    @DisplayName("authenticate: неверный пароль -> 401 с нейтральным сообщением")
    void authenticateWrongPassword() {
        when(userRepository.findByLogin("admin")).thenReturn(Optional.of(user));
        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> authService.authenticate("admin", "wrong"));
        assertEquals("Неверный логин или пароль", ex.getMessage());
    }

    @Test
    @DisplayName("authenticate: несуществующий логин -> то же нейтральное сообщение")
    void authenticateUnknownLogin() {
        when(userRepository.findByLogin("nobody")).thenReturn(Optional.empty());
        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> authService.authenticate("nobody", "admin123"));
        assertEquals("Неверный логин или пароль", ex.getMessage());
    }

    @Test
    @DisplayName("getById: пользователь удалён из БД -> 401")
    void getByIdMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(UnauthorizedException.class, () -> authService.getById(99L));
    }
}
