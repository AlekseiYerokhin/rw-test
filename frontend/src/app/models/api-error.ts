/** Единый формат ошибки backend'а (ТЗ п. 6.3). */
export interface ApiError {
  status: number;
  message: string;
  fieldErrors: Record<string, string> | null;
  timestamp: string;
}
