package by.bch.records.service;

import by.bch.records.dto.RecordDto;
import by.bch.records.dto.RecordForm;
import by.bch.records.entity.RecordEntity;
import by.bch.records.exception.BadRequestException;
import by.bch.records.exception.NotFoundException;
import by.bch.records.repository.ImageView;
import by.bch.records.repository.RecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Юнит-тесты RecordService (FR-VALID-*, FR-LIST-5/6, FR-UPDATE-4, FR-DELETE-3). */
@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @Mock
    private RecordRepository recordRepository;

    @InjectMocks
    private RecordService recordService;

    private static RecordForm form(Long num, String text, LocalDate date, MultipartFile image) {
        RecordForm f = new RecordForm();
        f.setNumValue(num);
        f.setTextValue(text);
        f.setDateValue(date);
        f.setImage(image);
        return f;
    }

    private static MultipartFile png(String name) {
        return new MockMultipartFile("image", name, "image/png", new byte[]{1, 2, 3});
    }

    private static RecordEntity entity() {
        RecordEntity e = new RecordEntity();
        e.setId(1L);
        e.setNumValue(42L);
        e.setTextValue("Привет, БЧ! Ёжик");
        e.setDateValue(LocalDate.of(2026, 1, 15));
        e.setImage(new byte[]{9});
        e.setImageName("флаг.png");
        e.setImageMime("image/png");
        return e;
    }

    // ---------- create: валидация картинки ----------

    @Test
    @DisplayName("create: без картинки -> 400 fieldErrors.image")
    void createWithoutImage() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> recordService.create(form(1L, "Текст", LocalDate.of(2026, 1, 1), null)));
        assertEquals("Картинка обязательна", ex.getFieldErrors().get("image"));
        verifyNoInteractions(recordRepository);
    }

    @Test
    @DisplayName("create: файл .txt с MIME text/plain отклоняется")
    void createWithTextFile() {
        MultipartFile file = new MockMultipartFile("image", "evil.txt", "text/plain", "abc".getBytes());
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> recordService.create(form(1L, "Текст", LocalDate.of(2026, 1, 1), file)));
        assertEquals("Допустимы только JPEG, PNG, GIF", ex.getFieldErrors().get("image"));
    }

    @Test
    @DisplayName("create: PNG с расширением .bmp отклоняется (проверка и MIME, и расширения)")
    void createWithMimeExtensionMismatch() {
        MultipartFile file = new MockMultipartFile("image", "image.bmp", "image/png", new byte[]{1});
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> recordService.create(form(1L, "Текст", LocalDate.of(2026, 1, 1), file)));
        assertEquals("Допустимы только JPEG, PNG, GIF", ex.getFieldErrors().get("image"));
    }

    @Test
    @DisplayName("create: файл больше 5 МБ отклоняется")
    void createWithTooBigFile() {
        MultipartFile file = new MockMultipartFile("image", "big.png", "image/png",
                new byte[5 * 1024 * 1024 + 1]);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> recordService.create(form(1L, "Текст", LocalDate.of(2026, 1, 1), file)));
        assertEquals("Файл превышает максимальный размер 5 МБ", ex.getFieldErrors().get("image"));
    }

    @Test
    @DisplayName("create: год 1850 вне диапазона 1900–2100 -> 400")
    void createWithDateOutOfRange() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> recordService.create(form(1L, "Текст", LocalDate.of(1850, 1, 1), png("ok.png"))));
        assertEquals("Дата должна быть в диапазоне 1900–2100 годов", ex.getFieldErrors().get("dateValue"));
    }

    @Test
    @DisplayName("create: валидная запись сохраняется с байтами и MIME")
    void createSavesValidRecord() {
        when(recordRepository.save(any(RecordEntity.class))).thenAnswer(inv -> {
            RecordEntity e = inv.getArgument(0);
            e.setId(10L);
            return e;
        });
        RecordDto dto = recordService.create(form(77L, "Привет, БЧ! Ёжик",
                LocalDate.of(2026, 3, 5), png("фото.png")));

        assertEquals(10L, dto.getId());
        assertEquals(77L, dto.getNumValue());
        assertEquals("фото.png", dto.getImageName());
        assertTrue(dto.isHasImage());
        assertEquals(LocalDate.of(2026, 3, 5), dto.getDateValue());

        ArgumentCaptor<RecordEntity> captor = ArgumentCaptor.forClass(RecordEntity.class);
        verify(recordRepository).save(captor.capture());
        assertArrayEquals(new byte[]{1, 2, 3}, captor.getValue().getImage());
        assertEquals("image/png", captor.getValue().getImageMime());
    }

    // ---------- getPage: пагинация и сортировка ----------

    @Test
    @DisplayName("getPage: мусорное поле сортировки заменяется на id, SQL-инъекция не проходит")
    void getPageSanitizesSort() {
        when(recordRepository.findAllRows(any(Pageable.class))).thenReturn(Page.empty());
        recordService.getPage(0, 10, "evil; DROP TABLE", "desc");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(recordRepository).findAllRows(captor.capture());
        Sort sort = captor.getValue().getSort();
        assertNotNull(sort.getOrderFor("id"));
        assertEquals(Sort.Direction.DESC, sort.getOrderFor("id").getDirection());
        assertTrue(sort.getOrderFor("evil; DROP TABLE") == null);
    }

    @Test
    @DisplayName("getPage: допустимое поле сортировки применяется")
    void getPageUsesAllowedSort() {
        when(recordRepository.findAllRows(any(Pageable.class))).thenReturn(Page.empty());
        recordService.getPage(1, 25, "numValue", "asc");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(recordRepository).findAllRows(captor.capture());
        assertEquals(1, captor.getValue().getPageNumber());
        assertEquals(25, captor.getValue().getPageSize());
        assertNotNull(captor.getValue().getSort().getOrderFor("numValue"));
        assertEquals(Sort.Direction.ASC, captor.getValue().getSort().getOrderFor("numValue").getDirection());
    }

    // ---------- update ----------

    @Test
    @DisplayName("update: без нового файла картинка остаётся прежней")
    void updateKeepsOldImage() {
        RecordEntity existing = entity();
        when(recordRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(recordRepository.save(any(RecordEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        MultipartFile empty = new MockMultipartFile("image", "", "application/octet-stream", new byte[0]);
        RecordDto dto = recordService.update(1L,
                form(43L, "Обновлено", LocalDate.of(2026, 1, 15), empty));

        assertEquals(43L, dto.getNumValue());
        assertEquals("Обновлено", dto.getTextValue());
        assertEquals("флаг.png", dto.getImageName());
        assertTrue(dto.isHasImage());
        verify(recordRepository).save(existing);
    }

    @Test
    @DisplayName("update: новый файл заменяет картинку")
    void updateReplacesImage() {
        RecordEntity existing = entity();
        when(recordRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(recordRepository.save(any(RecordEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        RecordDto dto = recordService.update(1L,
                form(42L, "Привет, БЧ! Ёжик", LocalDate.of(2026, 1, 15), png("новая.png")));

        assertEquals("новая.png", dto.getImageName());
        assertArrayEquals(new byte[]{1, 2, 3}, existing.getImage());
    }

    @Test
    @DisplayName("update: несуществующая запись -> 404")
    void updateNotFound() {
        when(recordRepository.findById(99L)).thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> recordService.update(99L, form(1L, "x", LocalDate.of(2026, 1, 1), png("ok.png"))));
        assertEquals("Запись не найдена", ex.getMessage());
    }

    // ---------- delete / image ----------

    @Test
    @DisplayName("delete: несуществующая запись -> 404, deleteById не вызывается")
    void deleteNotFound() {
        when(recordRepository.existsById(5L)).thenReturn(false);
        NotFoundException ex = assertThrows(NotFoundException.class, () -> recordService.delete(5L));
        assertEquals("Запись не найдена", ex.getMessage());
        verify(recordRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("getImage: у записи нет картинки -> 404")
    void getImageEmpty() {
        ImageView view = mock(ImageView.class);
        when(view.getImage()).thenReturn(null);
        when(recordRepository.findProjectedById(1L)).thenReturn(Optional.of(view));
        NotFoundException ex = assertThrows(NotFoundException.class, () -> recordService.getImage(1L));
        assertEquals("У записи нет картинки", ex.getMessage());
    }

    @Test
    @DisplayName("getById: несуществующая запись -> 404")
    void getByIdNotFound() {
        when(recordRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> recordService.getById(99L));
    }
}
