package by.bch.records.dto;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Вход: форма создания/редактирования (multipart/form-data, биндинг через @ModelAttribute).
 */
public class RecordForm {

    @NotNull(message = "Число обязательно")
    private Long numValue;

    @NotBlank(message = "Текст обязателен")
    @Size(max = 500, message = "Текст не должен превышать 500 символов")
    private String textValue;

    /** Ожидает yyyy-MM-dd от фронтенда (ISO 8601, ТЗ п. 4.6). */
    @NotNull(message = "Дата обязательна")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateValue;

    /** Картинка: тип/размер/обязательность проверяются в RecordService.validateImage() (FR-VALID-5). */
    private MultipartFile image;

    public Long getNumValue() {
        return numValue;
    }

    public void setNumValue(Long numValue) {
        this.numValue = numValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public LocalDate getDateValue() {
        return dateValue;
    }

    public void setDateValue(LocalDate dateValue) {
        this.dateValue = dateValue;
    }

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }
}
