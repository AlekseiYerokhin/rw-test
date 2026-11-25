/**
 * Запись реестра. Байтов картинки нет (NFR-PERF-3) —
 * картинка загружается отдельно через /api/records/{id}/image.
 * dateValue — ISO-строка "yyyy-MM-dd"; на UI отображается ДД.ММ.ГГГГ (этап 6).
 */
export interface RecordDto {
  id: number;
  numValue: number;
  textValue: string;
  dateValue: string;
  imageName: string | null;
  hasImage: boolean;
}
