import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { LoginComponent } from './pages/login/login.component';
import { RecordsComponent } from './pages/records/records.component';
import { PlaceholderComponent } from './pages/placeholder/placeholder.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'records', component: RecordsComponent, canActivate: [authGuard] },
  // Заглушки форм — этап 5 (#20, #21)
  { path: 'records/new', component: PlaceholderComponent, canActivate: [authGuard] },
  { path: 'records/:id/edit', component: PlaceholderComponent, canActivate: [authGuard] },
  { path: '', pathMatch: 'full', redirectTo: 'records' },
  { path: '**', redirectTo: 'records' },
];
