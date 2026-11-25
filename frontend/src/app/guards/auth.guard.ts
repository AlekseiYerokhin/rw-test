import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

/**
 * Защита роутов (FR-AUTH-5а): без входа — redirect на /login.
 * При первом заходе/перезагрузке состояние восстанавливается через GET /api/auth/me.
 */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.currentUser()) {
    return true;
  }
  const toLogin = router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
  return auth.me().pipe(
    map(() => true),
    catchError(() => of(toLogin)),
  );
};
