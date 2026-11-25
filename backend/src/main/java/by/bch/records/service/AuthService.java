package by.bch.records.service;

import by.bch.records.entity.AppUser;
import by.bch.records.exception.UnauthorizedException;
import by.bch.records.repository.AppUserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Аутентификация по таблице пользователей (ТЗ В.7, FR-AUTH-2). */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final AppUserRepository userRepository;

    /** Только крипто-модуль Spring Security, БЕЗ всего Spring Security. */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Проверка логина/пароля.
     * Пароль не логируется и не возвращается наружу (NFR-SEC-3).
     *
     * @return пользователь при успехе
     * @throws UnauthorizedException при неверном логине ИЛИ пароле (одинаковое сообщение, FR-AUTH-4)
     */
    public AppUser authenticate(String login, String rawPassword) {
        AppUser user = userRepository.findByLogin(login)
                .orElseThrow(UnauthorizedException::new);
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new UnauthorizedException();
        }
        return user;
    }

    /** Пользователь по id из сессии (для GET /api/auth/me). */
    public AppUser getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(UnauthorizedException::new);
    }
}
