package by.bch.records.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Запись реестра. Таблица record (ТЗ п. 6.2).
 * Сущность не выходит за пределы service-слоя (ТЗ В.2);
 * байты картинки не выбираются в списке (см. RecordRow, NFR-PERF-3).
 */
@Entity
@Table(name = "record")
public class RecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** «Число» из задания. */
    @Column(name = "num_value", nullable = false)
    private Long numValue;

    /** «Текст» (кириллица, UTF-8 на уровне БД). */
    @Column(name = "text_value", nullable = false, length = 500)
    private String textValue;

    /** «Дата». */
    @Column(name = "date_value", nullable = false)
    private LocalDate dateValue;

    /** «Картинка» — BYTEA. */
    @Column(name = "image")
    private byte[] image;

    @Column(name = "image_name")
    private String imageName;

    /** image/jpeg | image/png | image/gif */
    @Column(name = "image_mime", length = 100)
    private String imageMime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
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

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageMime() {
        return imageMime;
    }

    public void setImageMime(String imageMime) {
        this.imageMime = imageMime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
