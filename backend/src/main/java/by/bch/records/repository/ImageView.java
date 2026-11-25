package by.bch.records.repository;

/** Проекция Spring Data: только картинка записи (для GET /api/records/{id}/image). */
public interface ImageView {

    byte[] getImage();

    String getImageMime();
}
