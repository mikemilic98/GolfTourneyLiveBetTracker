import { api } from './client';

export interface User {
  id: number;
  email: string;
  displayName: string;
  role: string;
}

export const usersApi = {
  list: () => api.get<User[]>('/users'),
};
