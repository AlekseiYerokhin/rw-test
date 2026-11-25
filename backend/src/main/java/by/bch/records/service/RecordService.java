package by.bch.records.service;

import by.bch.records.dto.RecordDto;
import by.bch.records.dto.RecordForm;
import by.bch.records.dto.RecordRow;
import by.bch.records.entity.RecordEntity;
import by.bch.records.exception.BadRequestException;
import by.bch.records.exception.NotFoundException;
import by.bch.records.repository.ImageView;
import by.bch.records.repository.RecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/** Бизнес-логика записей (ТЗ В.7). Контроллер остаётся тонким (NFR-CODE-1). */
@Service
@Transactional
public class RecordService {

    private static final Set<String> ALLOWED_MIME =
            Set.of("image/jpeg", "image/png", "image/gif");
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(".jpg", ".jpeg", ".png", ".gif");
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;   // 5 МБ

    /** Белый список полей сортировки — защита от подстановки произвольных полей. */
    private static final Set<String> SORTABLE =
            Set.of("id", "numValue", "textValue", "dateValue");

    private final RecordRepository recordRepository;

    public RecordService(RecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    /** Список: серверная пагинация + сортировка (FR-LIST-5, FR-LIST-6). */
    @Transactional(readOnly = true)
    public Page<RecordDto> getPage(int page, int size, String sort, String dir) {
        String field = SORTABLE.contains(sort) ? sort : "id";
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, field));
        return recordRepository.findAllRows(pageable).map(this::toDto);
    }

    /** Одна запись или 404. */
    @Transactional(readOnly = true)
    public RecordDto getById(Long id) {
        return toDto(recordRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Запись не найдена")));
    }

    /** Создание: картинка обязательна (FR-CREATE-3). */
    public RecordDto create(RecordForm form) {
        validateImage(form.getImage(), true);
        validateDateRange(form);
        RecordEntity entity = new RecordEntity();
        fill(entity, form);
        return toDto(recordRepository.save(entity));
    }

    /** Обновление: картинку можно не менять (FR-UPDATE-4). */
    public RecordDto update(Long id, RecordForm form) {
        RecordEntity entity = recordRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Запись не найдена"));
        boolean replaceImage = form.getImage() != null && !form.getImage().isEmpty();
        if (replaceImage) {
            validateImage(form.getImage(), true);
        }
        validateDateRange(form);
        fill(entity, form);   // внутри: image меняется только если replaceImage
        return toDto(recordRepository.save(entity));
    }

    /** Удаление: несуществующий id → 404. */
    public void delete(Long id) {
        if (!recordRepository.existsById(id)) {
            throw new NotFoundException("Запись не найдена");
        }
        recordRepository.deleteById(id);
    }

    /** Картинка записи или 404. */
    @Transactional(readOnly = true)
    public ImageView getImage(Long id) {
        ImageView view = recordRepository.findProjectedById(id)
                .orElseThrow(() -> new NotFoundException("Запись не найдена"));
        if (view.getImage() == null || view.getImage().length == 0) {
            throw new NotFoundException("У записи нет картинки");
        }
        return view;
    }

    /** Серверная проверка файла: наличие, тип (MIME + расширение), размер (FR-VALID-5). */
    private void validateImage(MultipartFile file, boolean required) {
        if (file == null || file.isEmpty()) {
            if (required) {
                throw new BadRequestException("Ошибка валидации",
                        Map.of("image", "Картинка обязательна"));
            }
            return;
        }
        String mime = file.getContentType();
        String name = file.getOriginalFilename() == null
                ? "" : file.getOriginalFilename().toLowerCase();
        boolean extensionOk = ALLOWED_EXTENSIONS.stream().anyMatch(name::endsWith);
        if (mime == null || !ALLOWED_MIME.contains(mime) || !extensionOk) {
            throw new BadRequestException("Ошибка валидации",
                    Map.of("image", "Допустимы только JPEG, PNG, GIF"));
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BadRequestException("Ошибка валидации",
                    Map.of("image", "Файл превышает максимальный размер 5 МБ"));
        }
    }

    /** Разумный диапазон дат: год 1900–2100 (FR-VALID-4). */
    private void validateDateRange(RecordForm form) {
        int year = form.getDateValue().getYear();
        if (year < 1900 || year > 2100) {
            throw new BadRequestException("Ошибка валидации",
                    Map.of("dateValue", "Дата должна быть в диапазоне 1900–2100 годов"));
        }
    }

    /** Перенос полей формы в сущность + чтение байтов файла (только если файл выбран). */
    private void fill(RecordEntity entity, RecordForm form) {
        entity.setNumValue(form.getNumValue());
        entity.setTextValue(form.getTextValue());
        entity.setDateValue(form.getDateValue());
        MultipartFile file = form.getImage();
        if (file != null && !file.isEmpty()) {
            try {
                entity.setImage(file.getBytes());
            } catch (IOException e) {
                throw new BadRequestException("Ошибка валидации",
                        Map.of("image", "Не удалось прочитать файл"));
            }
            entity.setImageName(file.getOriginalFilename());
            entity.setImageMime(file.getContentType());
        }
    }

    private RecordDto toDto(RecordEntity e) {
        return new RecordDto(e.getId(), e.getNumValue(), e.getTextValue(),
                e.getDateValue(), e.getImageName(),
                e.getImage() != null && e.getImage().length > 0);
    }

    private RecordDto toDto(RecordRow r) {
        return new RecordDto(r.getId(), r.getNumValue(), r.getTextValue(),
                r.getDateValue(), r.getImageName(), r.getImageName() != null);
    }
}
