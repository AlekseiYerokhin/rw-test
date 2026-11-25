import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { RecordDto } from '../../models/record.dto';
import { RecordsService } from '../../services/records.service';

/** Полноразмерный просмотр картинки записи (ТЗ 7.4, FR-LIST-4). */
@Component({
  selector: 'app-image-view-dialog',
  imports: [MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>{{ data.imageName ?? 'Картинка записи №' + data.id }}</h2>
    <mat-dialog-content class="image-content">
      <img [src]="recordsService.imageUrl(data.id)" [alt]="data.imageName ?? 'Картинка'" />
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Закрыть</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .image-content img {
      max-width: 80vw;
      max-height: 75vh;
      display: block;
    }
  `],
})
export class ImageViewDialogComponent {
  readonly data = inject<RecordDto>(MAT_DIALOG_DATA);
  readonly recordsService = inject(RecordsService);
}
