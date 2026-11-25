import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * При ответе 401 (кроме самого запроса логина) — очистка состояния
 * и redirect на /login (FR-AUTH-5а).
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const auth = inject(AuthService);

  // CSRF-lite (NFR-SEC-5): backend требует X-Requested-With на мутирующие запросы
  const mutating = !['GET', 'HEAD', 'OPTIONS'].includes(req.method);
  const request = mutating
    ? req.clone({ setHeaders: { 'X-Requested-With': 'XMLHttpRequest' } })
    : req;

  return next(request).pipe(
    catchError((err: unknown) => {
      const status = (err as { status?: number }).status;
      if (status === 401 && !req.url.includes('/api/auth/login')) {
        auth.clear();
        void router.navigate(['/login']);
      }
      return throwError(() => err);
    }),
  );
};
