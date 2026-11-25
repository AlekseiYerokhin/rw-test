package by.bch.records.security;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Проверяет каждый запрос к /api/** (кроме /api/auth/login):
 *  1) мутирующие методы (POST/PUT/DELETE) требуют заголовок X-Requested-With: XMLHttpRequest
 *     — базовая защита от CSRF при куки-сессии (NFR-SEC-5); Angular HttpClient ставит его автоматически;
 *  2) наличие авторизованной сессии, иначе 401 в формате ApiError (FR-AUTH-5б).
 */
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler)
            throws IOException {

        String method = req.getMethod();
        boolean mutating = !"GET".equals(method) && !"HEAD".equals(method) && !"OPTIONS".equals(method);
        if (mutating && !"XMLHttpRequest".equals(req.getHeader("X-Requested-With"))) {
            writeJsonError(res, 403, "Отсутствует заголовок X-Requested-With");
            return false;
        }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SessionKeys.USER_ID) == null) {
            writeJsonError(res, 401, "Требуется аутентификация");
            return false;
        }
        return true;
    }

    private void writeJsonError(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(
            "{\"status\":" + status + ",\"message\":\"" + message + "\"}");
    }
}
