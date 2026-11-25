package by.bch.records.controller;

import by.bch.records.dto.RecordDto;
import by.bch.records.dto.RecordForm;
import by.bch.records.repository.ImageView;
import by.bch.records.service.RecordService;
import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.concurrent.TimeUnit;

/** REST-эндпоинты записей (ТЗ В.8, карта п. 6.3). Без бизнес-логики — только HTTP (NFR-CODE-1). */
@RestController
@RequestMapping("/api/records")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    /** GET /api/records?page=0&size=10&sort=dateValue&dir=desc */
    @GetMapping
    public Page<RecordDto> list(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(defaultValue = "id") String sort,
                                @RequestParam(defaultValue = "asc") String dir) {
        return recordService.getPage(page, size, sort, dir);
    }

    @GetMapping("/{id}")
    public RecordDto get(@PathVariable Long id) {
        return recordService.getById(id);
    }

    /** Создание: multipart/form-data. 201 | 400 */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RecordDto create(@Valid @ModelAttribute RecordForm form) {
        return recordService.create(form);
    }

    /** Редактирование: multipart/form-data, поле image опционально. 200 | 400 | 404 */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RecordDto update(@PathVariable Long id, @Valid @ModelAttribute RecordForm form) {
        return recordService.update(id, form);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        recordService.delete(id);
    }

    /** Отдача байтов картинки с корректным Content-Type и кэшированием (ТЗ 6.4). */
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> image(@PathVariable Long id) {
        ImageView view = recordService.getImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(view.getImageMime()))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate())
                .body(view.getImage());
    }
}
