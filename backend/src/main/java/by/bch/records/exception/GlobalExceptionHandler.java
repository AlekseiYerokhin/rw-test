package by.bch.records.exception;

import by.bch.records.dto.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;
import java.util.stream.Collectors;

/** Превращает все исключения в ApiError (JSON, UTF-8, сообщения на русском) — ТЗ В.11. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> unauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(401).body(ApiError.of(401, ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(NotFoundException ex) {
        return ResponseEntity.status(404).body(ApiError.of(404, ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> badRequest(BadRequestException ex) {
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, ex.getMessage(), ex.getFieldErrors()));
    }

    /**
     * Ошибки Bean Validation.
     * BindException — для @ModelAttribute (multipart-формы),
     * MethodArgumentNotValidException — для @RequestBody (JSON).
     */
    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ApiError> validation(BindException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        fe -> fe.getDefaultMessage(), (a, b) -> a));
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "Ошибка валидации", fieldErrors));
    }

    /** Превышен multipart-лимит (5 МБ). */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> tooLarge(MaxUploadSizeExceededException ex) {
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "Файл превышает максимальный размер 5 МБ"));
    }

    /** Всё остальное — 500, детали только в лог (не наружу!). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> internal(Exception ex) {
        log.error("Необработанная ошибка", ex);
        return ResponseEntity.status(500)
                .body(ApiError.of(500, "Внутренняя ошибка сервера"));
    }
}
