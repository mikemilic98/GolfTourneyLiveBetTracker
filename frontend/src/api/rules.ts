import { api } from './client';

export interface GameRules {
  id: number;
  tournamentId: number;
  tournamentName: string;
  numPicks: number;
  numDropped: number;
  tieRule: string;
  wdPenaltyPosition: number;
  lockedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface GameRulesCreateRequest {
  tournamentId: number;
  numPicks?: number;
  numDropped?: number;
  tieRule?: string;
  wdPenaltyPosition?: number;
}

export const rulesApi = {
  list: () => api.get<GameRules[]>('/rules'),
  get: (id: number) => api.get<GameRules>(`/rules/${id}`),
  getByTournament: (tournamentId: number) =>
    api.get<GameRules>(`/rules/tournament/${tournamentId}`),
  create: (data: GameRulesCreateRequest) => api.post<GameRules>('/rules', data),
  update: (id: number, data: Partial<GameRulesCreateRequest> & { lock?: boolean }) =>
    api.put<GameRules>(`/rules/${id}`, data),
  delete: (id: number) => api.delete(`/rules/${id}`),
};
