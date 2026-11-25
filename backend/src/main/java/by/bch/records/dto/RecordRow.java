package by.bch.records.dto;

import java.time.LocalDate;

/**
 * Лёгкая строка списка (проекция JPQL, см. RecordRepository.findAllRows).
 * Поля image нет — список не загружает байты картинок (NFR-PERF-3).
 */
public class RecordRow {

    private final Long id;
    private final Long numValue;
    private final String textValue;
    private final LocalDate dateValue;
    private final String imageName;
    private final String imageMime;

    public RecordRow(Long id, Long numValue, String textValue,
                     LocalDate dateValue, String imageName, String imageMime) {
        this.id = id;
        this.numValue = numValue;
        this.textValue = textValue;
        this.dateValue = dateValue;
        this.imageName = imageName;
        this.imageMime = imageMime;
    }

    public Long getId() {
        return id;
    }

    public Long getNumValue() {
        return numValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public LocalDate getDateValue() {
        return dateValue;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageMime() {
        return imageMime;
    }
}
