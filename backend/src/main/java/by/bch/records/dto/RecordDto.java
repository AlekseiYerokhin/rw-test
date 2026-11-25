package by.bch.records.dto;

import java.time.LocalDate;

/**
 * Выход: запись реестра. Байтов картинки НЕТ — только метаданные (NFR-PERF-3);
 * байты отдаются отдельным эндпоинтом /api/records/{id}/image.
 * dateValue сериализуется Jackson как "yyyy-MM-dd" (ISO 8601) — дефолт Spring Boot;
 * на UI дата отображается как ДД.ММ.ГГГГ (ТЗ п. 4.6).
 */
public class RecordDto {

    private Long id;
    private Long numValue;
    private String textValue;
    private LocalDate dateValue;
    private String imageName;
    private boolean hasImage;

    public RecordDto() {
    }

    public RecordDto(Long id, Long numValue, String textValue, LocalDate dateValue,
                     String imageName, boolean hasImage) {
        this.id = id;
        this.numValue = numValue;
        this.textValue = textValue;
        this.dateValue = dateValue;
        this.imageName = imageName;
        this.hasImage = hasImage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public boolean isHasImage() {
        return hasImage;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }
}
