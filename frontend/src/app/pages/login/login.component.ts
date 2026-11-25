import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../services/auth.service';

/** Страница входа (ТЗ 7.1, FR-AUTH-1..4). */
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly form = new FormGroup({
    login: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  readonly errorMessage = signal('');
  readonly submitting = signal(false);

  submit(): void {
    if (this.form.invalid || this.submitting()) {
      return;
    }
    this.submitting.set(true);
    this.errorMessage.set('');
    const { login, password } = this.form.getRawValue();

    this.auth.login(login, password).subscribe({
      next: () => {
        this.submitting.set(false);
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/records';
        void this.router.navigateByUrl(returnUrl);
      },
      error: (err: { status?: number }) => {
        this.submitting.set(false);
        // Одинаковое сообщение для неверного логина и пароля (FR-AUTH-4)
        this.errorMessage.set(
          err.status === 401
            ? 'Неверный логин или пароль'
            : 'Ошибка сервера. Попробуйте позже.',
        );
      },
    });
  }
}
