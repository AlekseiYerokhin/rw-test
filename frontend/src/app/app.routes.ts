import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { LoginComponent } from './pages/login/login.component';
import { RecordsComponent } from './pages/records/records.component';
import { RecordFormComponent } from './pages/record-form/record-form.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'records', component: RecordsComponent, canActivate: [authGuard] },
  { path: 'records/new', component: RecordFormComponent, canActivate: [authGuard] },
  { path: 'records/:id/edit', component: RecordFormComponent, canActivate: [authGuard] },
  { path: '', pathMatch: 'full', redirectTo: 'records' },
  { path: '**', redirectTo: 'records' },
];
