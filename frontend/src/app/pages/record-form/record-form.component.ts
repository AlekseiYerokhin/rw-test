import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { RecordsService } from '../../services/records.service';
import { NotificationService } from '../../services/notification.service';
import { ApiError } from '../../models/api-error';

/**
 * Форма записи: создание (/records/new) и редактирование (/records/:id/edit).
 * ТЗ 7.3, FR-CREATE-*, FR-UPDATE-*.
 */
@Component({
  selector: 'app-record-form',
  imports: [
    ReactiveFormsModule, MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatDatepickerModule,
  ],
  templateUrl: './record-form.component.html',
  styleUrl: './record-form.component.css',
})
export class RecordFormComponent implements OnInit {
  private readonly recordsService = inject(RecordsService);
  private readonly notify = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly recordId: number | null;
  readonly isEdit: boolean;

  readonly form = new FormGroup({
    numValue: new FormControl<number | null>(null, Validators.required),
    textValue: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(500)],
    }),
    dateValue: new FormControl<Date | null>(null, Validators.required),
  });

  readonly selectedFile = signal<File | null>(null);
  readonly fileName = signal('');
  readonly previewUrl = signal<string | null>(null);
  readonly currentImageName = signal<string | null>(null);
  readonly hasCurrentImage = signal(false);
  readonly imageError = signal('');
  readonly submitting = signal(false);

  /** Валидность формы как сигнал: form.valid сам по себе НЕ реактивен,
   *  computed без этого не пересчитывался бы (баг: disabled валидной формы). */
  private readonly formStatus = toSignal(this.form.statusChanges, {
    initialValue: this.form.status,
  });

  /** Картинка обязательна только при создании (FR-CREATE-3, FR-UPDATE-4). */
  readonly canSave = computed(() => {
    const fileOk = this.isEdit || this.selectedFile() !== null;
    return this.formStatus() === 'VALID' && fileOk && !this.submitting();
  });

  constructor() {
    const id = this.route.snapshot.paramMap.get('id');
    this.recordId = id !== null ? Number(id) : null;
    this.isEdit = this.recordId !== null;
  }

  ngOnInit(): void {
    if (this.isEdit) {
      // Предзаполнение формы данными записи (FR-UPDATE-1)
      this.recordsService.get(this.recordId!).subscribe({
        next: (r) => {
          this.form.patchValue({
            numValue: r.numValue,
            textValue: r.textValue,
            dateValue: new Date(r.dateValue + 'T00:00:00'),
          });
          this.currentImageName.set(r.imageName);
          this.hasCurrentImage.set(r.hasImage);
        },
        error: () => {
          this.notify.error('Запись не найдена');
          void this.router.navigate(['/records']);
        },
      });
    }
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.selectedFile.set(file);
    this.fileName.set(file?.name ?? '');
    this.imageError.set('');
    this.previewUrl.set(null);
    if (file) {
      const reader = new FileReader();
      reader.onload = () => this.previewUrl.set(reader.result as string);
      reader.readAsDataURL(file);
    }
  }

  submit(): void {
    if (!this.canSave()) {
      return;
    }
    this.submitting.set(true);
    this.imageError.set('');

    const { numValue, textValue, dateValue } = this.form.getRawValue();
    const fd = new FormData();
    fd.append('numValue', String(numValue));
    fd.append('textValue', textValue);
    fd.append('dateValue', toIsoDate(dateValue!));
    const file = this.selectedFile();
    if (file) {
      fd.append('image', file, file.name);
    }

    const request = this.isEdit
      ? this.recordsService.update(this.recordId!, fd)
      : this.recordsService.create(fd);

    request.subscribe({
      next: () => {
        this.notify.success(this.isEdit ? 'Запись обновлена' : 'Запись успешно добавлена');
        void this.router.navigate(['/records']);
      },
      error: (err: { status?: number; error?: ApiError }) => {
        this.submitting.set(false);
        if (err.status === 400 && err.error?.fieldErrors) {
          this.applyServerErrors(err.error.fieldErrors);
        } else if (err.status === 404) {
          this.notify.error('Запись не найдена');
          void this.router.navigate(['/records']);
        } else {
          this.notify.error('Ошибка сохранения записи');
        }
      },
    });
  }

  /** Серверные ошибки 400 — подсветка полей, введённые значения сохраняются (FR-CREATE-6). */
  private applyServerErrors(fieldErrors: Record<string, string>): void {
    for (const field of ['numValue', 'textValue', 'dateValue'] as const) {
      if (fieldErrors[field]) {
        this.form.controls[field].setErrors({ server: fieldErrors[field] });
        this.form.controls[field].markAsTouched();
      }
    }
    if (fieldErrors['image']) {
      this.imageError.set(fieldErrors['image']);
    }
  }

  cancel(): void {
    void this.router.navigate(['/records']);
  }

  errorText(field: 'numValue' | 'textValue' | 'dateValue'): string {
    const control = this.form.controls[field];
    if (control.hasError('server')) {
      return control.getError('server') as string;
    }
    if (control.hasError('required')) {
      return { numValue: 'Число обязательно', textValue: 'Текст обязателен', dateValue: 'Дата обязательна' }[field];
    }
    if (control.hasError('maxlength')) {
      return 'Текст не должен превышать 500 символов';
    }
    return '';
  }
}

/** Date (локальная дата из datepicker) -> ISO "yyyy-MM-dd" без сдвига часового пояса. */
function toIsoDate(d: Date): string {
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mm}-${dd}`;
}
