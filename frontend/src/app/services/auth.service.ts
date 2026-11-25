import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { UserDto } from '../models/user.dto';

/** Аутентификация и состояние текущего пользователя. */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  /** Текущий пользователь; null — не аутентифицирован. */
  readonly currentUser = signal<UserDto | null>(null);

  login(login: string, password: string): Observable<UserDto> {
    return this.http.post<UserDto>('/api/auth/login', { login, password }).pipe(
      tap((user) => this.currentUser.set(user)),
    );
  }

  logout(): Observable<void> {
    return this.http.post<void>('/api/auth/logout', {}).pipe(
      tap(() => this.currentUser.set(null)),
    );
  }

  /** Восстановление состояния входа при перезагрузке страницы (FR-AUTH-7). */
  me(): Observable<UserDto> {
    return this.http.get<UserDto>('/api/auth/me').pipe(
      tap((user) => this.currentUser.set(user)),
    );
  }

  /** Локальная очистка состояния (например, при 401 от interceptor'а). */
  clear(): void {
    this.currentUser.set(null);
  }
}
