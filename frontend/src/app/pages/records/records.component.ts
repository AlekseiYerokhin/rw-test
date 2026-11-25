import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../services/auth.service';

/**
 * Страница списка записей. Шапка с именем пользователя и выходом (ТЗ 7.2).
 * Таблица, пагинация и сортировка — этап 5 (#19).
 */
@Component({
  selector: 'app-records',
  imports: [MatToolbarModule, MatButtonModule],
  templateUrl: './records.component.html',
  styleUrl: './records.component.css',
})
export class RecordsComponent {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  logout(): void {
    this.auth.logout().subscribe(() => {
      void this.router.navigate(['/login']);
    });
  }
}
