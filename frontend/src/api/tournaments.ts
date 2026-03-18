import { api } from './client';

export interface Tournament {
  id: number;
  name: string;
  espnUrl: string;
  espnEventId: string | null;
  rosterCount: number;
  startTime: string | null;
  endTime: string | null;
  picksCutoff: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface TournamentCreateRequest {
  name: string;
  espnUrl: string;
  espnEventId?: string;
  startTime?: string;
  endTime?: string;
  picksCutoff?: string;
}

export const tournamentsApi = {
  list: () => api.get<Tournament[]>('/tournaments'),
  get: (id: number) => api.get<Tournament>(`/tournaments/${id}`),
  getRoster: (id: number) => api.get<string[]>(`/tournaments/${id}/roster`),
  create: (data: TournamentCreateRequest) => api.post<Tournament>('/tournaments', data),
  update: (id: number, data: Partial<TournamentCreateRequest>) =>
    api.put<Tournament>(`/tournaments/${id}`, data),
  ingestRoster: (id: number) => api.post<string[]>(`/tournaments/${id}/ingest-roster`),
  delete: (id: number) => api.delete(`/tournaments/${id}`),
};
