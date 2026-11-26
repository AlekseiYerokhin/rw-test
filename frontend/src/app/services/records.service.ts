import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RecordDto } from '../models/record.dto';
import { Page } from '../models/page';

/** CRUD-операции над записями реестра (карта ТЗ п. 6.3). */
@Injectable({ providedIn: 'root' })
export class RecordsService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/records';

  /**
   * Версии картинок для cache-busting: backend отдаёт image с max-age=3600
   * (ТЗ 6.4), поэтому после замены картинки URL должен измениться,
   * иначе браузер покажет старую из кэша.
   */
  private readonly imageVersions = signal<Record<number, number>>({});

  /** Серверная пагинация + сортировка (FR-LIST-5/6). */
  getPage(page = 0, size = 10, sort = 'id', dir = 'asc'): Observable<Page<RecordDto>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort)
      .set('dir', dir);
    return this.http.get<Page<RecordDto>>(this.base, { params });
  }

  get(id: number): Observable<RecordDto> {
    return this.http.get<RecordDto>(`${this.base}/${id}`);
  }

  /** Создание: multipart/form-data (поля + файл image). */
  create(data: FormData): Observable<RecordDto> {
    return this.http.post<RecordDto>(this.base, data);
  }

  /** Редактирование: multipart/form-data, поле image опционально. */
  update(id: number, data: FormData): Observable<RecordDto> {
    return this.http.put<RecordDto>(`${this.base}/${id}`, data);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  /** Инвалидирует кэш картинки записи (после замены изображения). */
  bumpImage(id: number): void {
    this.imageVersions.update((v) => ({ ...v, [id]: Date.now() }));
  }

  /** URL картинки для <img> — байты отдаются отдельным эндпоинтом. */
  imageUrl(id: number): string {
    const version = this.imageVersions()[id];
    return version ? `${this.base}/${id}/image?v=${version}` : `${this.base}/${id}/image`;
  }
}
