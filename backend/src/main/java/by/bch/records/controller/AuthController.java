package by.bch.records.controller;

import by.bch.records.dto.ApiError;
import by.bch.records.dto.LoginRequest;
import by.bch.records.dto.UserDto;
import by.bch.records.entity.AppUser;
import by.bch.records.security.SessionKeys;
import by.bch.records.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

/** Эндпоинты аутентификации (ТЗ В.8): /api/auth/login, /logout, /me. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Вход. 200 + UserDto | 401 (FR-AUTH-3, FR-AUTH-4) */
    @PostMapping("/login")
    public UserDto login(@Valid @RequestBody LoginRequest req, HttpServletRequest request) {
        AppUser user = authService.authenticate(req.getLogin(), req.getPassword());
        // Сначала гарантируем сессию: changeSessionId() без существующей сессии
        // бросает IllegalStateException (первая авторизация!), затем ротация id
        // против session fixation — id меняется, объект сессии сохраняется.
        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute(SessionKeys.USER_ID, user.getId());
        return new UserDto(user.getLogin(), user.getDisplayName());
    }

    /** Выход. 204 (FR-AUTH-6) */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * Текущий пользователь (восстановление входа после F5, FR-AUTH-7).
     * 200 UserDto | 401. До появления AuthInterceptor (#13) сессию проверяем здесь;
     * после #13 проверка будет дублироваться интерсептором — поведение ответа то же.
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionKeys.USER_ID) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiError.of(401, "Требуется аутентификация"));
        }
        Long userId = (Long) session.getAttribute(SessionKeys.USER_ID);
        AppUser user = authService.getById(userId);
        return ResponseEntity.ok(new UserDto(user.getLogin(), user.getDisplayName()));
    }
}
