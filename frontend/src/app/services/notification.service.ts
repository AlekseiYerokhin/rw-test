import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

/** Snackbar-уведомления об успехе/ошибке операций (ТЗ 7.2). */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly snackBar = inject(MatSnackBar);

  success(message: string): void {
    this.snackBar.open(message, 'ОК', { duration: 3000 });
  }

  error(message: string): void {
    this.snackBar.open(message, 'ОК', { duration: 5000 });
  }
}
