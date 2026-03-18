import { api } from './client';

export interface Pick {
  id: number;
  userId: number;
  userDisplayName: string;
  tournamentId: number;
  tournamentName: string;
  playerNames: string[];
  createdAt: string;
  updatedAt: string;
}

export interface PickCreateForUserRequest {
  tournamentId: number;
  playerNames: string[];
}

export interface PickUpdateRequest {
  playerNames: string[];
}

export const picksApi = {
  listMyPicks: (tournamentId?: number) => {
    const params = tournamentId ? { tournamentId } : {};
    return api.get<Pick[]>('/picks/me', { params });
  },
  listByTournament: (tournamentId: number) =>
    api.get<Pick[]>(`/picks/tournament/${tournamentId}`),
  get: (id: number) => api.get<Pick>(`/picks/${id}`),
  createMyPick: (data: PickCreateForUserRequest) =>
    api.post<Pick>('/picks/me', data),
  updateMyPick: (id: number, data: PickUpdateRequest) =>
    api.put<Pick>(`/picks/me/${id}`, data),
  create: (data: { userId: number; tournamentId: number; playerNames: string[] }) =>
    api.post<Pick>('/picks', data),
  update: (id: number, data: PickUpdateRequest) =>
    api.put<Pick>(`/picks/${id}`, data),
  delete: (id: number) => api.delete(`/picks/${id}`),
};
