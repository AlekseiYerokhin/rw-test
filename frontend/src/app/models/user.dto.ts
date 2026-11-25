/** Текущий пользователь (GET /api/auth/me, POST /api/auth/login). */
export interface UserDto {
  login: string;
  displayName: string;
}

/** Входные данные логина (POST /api/auth/login). */
export interface LoginRequest {
  login: string;
  password: string;
}
