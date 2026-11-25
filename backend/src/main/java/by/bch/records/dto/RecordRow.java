package by.bch.records.dto;

import java.time.LocalDate;

/**
 * Лёгкая строка списка (проекция, см. RecordRepository.findAllRows).
 * Поля image нет — список не загружает байты картинок (NFR-PERF-3).
 *
 * ВАЖНО: это interface-проекция (алиасы в JPQL), а не класс с конструктором,
 * как в черновике ТЗ В.6. Причина: Hibernate 5.6 падает с ClassCastException
 * (SqlNode -> FromReferenceNode) на SELECT new, если полное имя класса
 * содержит ключевое слово JPQL — а by.bch.records... начинается с 'by'.
 * Проверено экспериментально на #7; для сервиса интерфейс геттеров тот же.
 */
public interface RecordRow {

    Long getId();

    Long getNumValue();

    String getTextValue();

    LocalDate getDateValue();

    String getImageName();

    String getImageMime();
}
