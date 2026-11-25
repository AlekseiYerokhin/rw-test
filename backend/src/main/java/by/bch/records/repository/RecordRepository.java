package by.bch.records.repository;

import by.bch.records.dto.RecordRow;
import by.bch.records.entity.RecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RecordRepository extends JpaRepository<RecordEntity, Long> {

    /**
     * Постраничная выборка списка БЕЗ байтов картинки (NFR-PERF-3).
     * Пагинация и сортировка — на уровне SQL (NFR-PERF-1).
     *
     * Алиасы вместо SELECT new: Hibernate 5.6 не парсит FQN с ключевым
     * словом 'by' (by.bch...) в конструкторных выражениях JPQL — см. RecordRow.
     */
    @Query("SELECT r.id AS id, r.numValue AS numValue, r.textValue AS textValue, " +
           "r.dateValue AS dateValue, r.imageName AS imageName, r.imageMime AS imageMime " +
           "FROM RecordEntity r")
    Page<RecordRow> findAllRows(Pageable pageable);

    /** Только картинка конкретной записи (байты + MIME), без загрузки всей сущности. */
    Optional<ImageView> findProjectedById(@Param("id") Long id);
}
