import { Injectable, signal } from '@angular/core';

/** Счётчик активных HTTP-запросов для глобального индикатора загрузки (ТЗ 7.5). */
@Injectable({ providedIn: 'root' })
export class LoadingService {
  private readonly count = signal(0);

  /** true, пока идёт хотя бы один запрос к API. */
  readonly active = signal(false);

  begin(): void {
    this.count.update((n) => n + 1);
    this.active.set(this.count() > 0);
  }

  end(): void {
    this.count.update((n) => Math.max(0, n - 1));
    this.active.set(this.count() > 0);
  }
}
