import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { RecordDto } from '../../models/record.dto';

/** Диалог подтверждения удаления (ТЗ 7.4, FR-DELETE-2). */
@Component({
  selector: 'app-delete-confirm-dialog',
  imports: [MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>Подтверждение удаления</h2>
    <mat-dialog-content>
      Удалить запись №{{ data.id }}? Действие необратимо.
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Отмена</button>
      <button mat-flat-button color="warn" [mat-dialog-close]="true">Удалить</button>
    </mat-dialog-actions>
  `,
})
export class DeleteConfirmDialogComponent {
  readonly data = inject<RecordDto>(MAT_DIALOG_DATA);
}
