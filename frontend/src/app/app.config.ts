import { ApplicationConfig, LOCALE_ID, provideBrowserGlobalErrorListeners } from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localeRu from '@angular/common/locales/ru';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatPaginatorIntl } from '@angular/material/paginator';
import { RuPaginatorIntl } from './paginator-intl';
import { routes } from './app.routes';
import { authInterceptor } from './interceptors/auth.interceptor';
import { loadingInterceptor } from './interceptors/loading.interceptor';

registerLocaleData(localeRu);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    // Русская локаль для pipe'ов и компонентов (NFR-LOC-3)
    { provide: LOCALE_ID, useValue: 'ru' },
    provideRouter(routes),
    provideHttpClient(withInterceptors([loadingInterceptor, authInterceptor])),
    provideAnimationsAsync(),
    provideNativeDateAdapter(),
    // Datepicker с русской локалью: отображение ДД.ММ.ГГГГ (NFR-LOC-3)
    { provide: MAT_DATE_LOCALE, useValue: 'ru' },
    { provide: MatPaginatorIntl, useClass: RuPaginatorIntl },
  ],
};
