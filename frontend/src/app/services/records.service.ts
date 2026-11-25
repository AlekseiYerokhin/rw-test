import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RecordDto } from '../models/record.dto';
import { Page } from '../models/page';

/** CRUD-операции над записями реестра (карта ТЗ п. 6.3). */
@Injectable({ providedIn: 'root' })
export class RecordsService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/records';

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

  /** URL картинки для <img> — байты отдаются отдельным эндпоинтом. */
  imageUrl(id: number): string {
    return `${this.base}/${id}/image`;
  }
}
