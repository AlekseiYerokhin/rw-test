import { Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { AuthService } from '../../services/auth.service';
import { RecordsService } from '../../services/records.service';
import { NotificationService } from '../../services/notification.service';
import { RecordDto } from '../../models/record.dto';
import { DeleteConfirmDialogComponent } from './delete-confirm-dialog.component';
import { ImageViewDialogComponent } from './image-view-dialog.component';

/** Страница списка записей (ТЗ 7.2, FR-LIST-1..8). Серверная пагинация и сортировка. */
@Component({
  selector: 'app-records',
  imports: [
    DatePipe, MatToolbarModule, MatButtonModule, MatTableModule,
    MatPaginatorModule, MatSortModule,
  ],
  templateUrl: './records.component.html',
  styleUrl: './records.component.css',
})
export class RecordsComponent implements OnInit {
  readonly auth = inject(AuthService);
  readonly recordsService = inject(RecordsService);
  private readonly notify = inject(NotificationService);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);

  readonly records = signal<RecordDto[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  readonly sortField = signal('id');
  readonly sortDir = signal<'asc' | 'desc'>('asc');
  readonly loading = signal(false);

  readonly displayedColumns = ['id', 'numValue', 'textValue', 'dateValue', 'image', 'actions'];

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.recordsService
      .getPage(this.pageIndex(), this.pageSize(), this.sortField(), this.sortDir())
      .subscribe({
        next: (page) => {
          this.records.set(page.content);
          this.totalElements.set(page.totalElements);
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.notify.error('Не удалось загрузить список записей');
        },
      });
  }

  onPage(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.load();
  }

  onSort(sort: Sort): void {
    this.sortField.set(sort.active || 'id');
    this.sortDir.set(sort.direction || 'asc');
    this.pageIndex.set(0);   // смена сортировки — на первую страницу (FR-LIST-6)
    this.load();
  }

  addRecord(): void {
    void this.router.navigate(['/records/new']);
  }

  /** Полноразмерный просмотр картинки по клику на миниатюру (FR-LIST-4). */
  viewImage(record: RecordDto): void {
    this.dialog.open(ImageViewDialogComponent, { data: record });
  }

  editRecord(record: RecordDto): void {
    void this.router.navigate(['/records', record.id, 'edit']);
  }

  deleteRecord(record: RecordDto): void {
    this.dialog
      .open(DeleteConfirmDialogComponent, { data: record })
      .afterClosed()
      .subscribe((confirmed: boolean | undefined) => {
        if (!confirmed) {
          return;
        }
        this.recordsService.delete(record.id).subscribe({
          next: () => {
            this.notify.success('Запись удалена');
            // Если удалили последнюю запись на странице — шаг назад (FR-DELETE-4)
            if (this.records().length === 1 && this.pageIndex() > 0) {
              this.pageIndex.update((i) => i - 1);
            }
            this.load();
          },
          error: () => this.notify.error('Не удалось удалить запись'),
        });
      });
  }

  logout(): void {
    this.auth.logout().subscribe(() => {
      void this.router.navigate(['/login']);
    });
  }
}
