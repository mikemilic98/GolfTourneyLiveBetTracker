const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export interface LeaderboardRow {
  displayName: string;
  totalScore: number;
  rank: number | null;
}

export interface LiveLeaderboardDto {
  tournamentId: number;
  rows: LeaderboardRow[];
  computedAt: string;
}

export const liveApi = {
  getScores: async (tournamentId: number): Promise<LiveLeaderboardDto> => {
    const token = localStorage.getItem('token');
    const res = await fetch(`${API_BASE}/live/${tournamentId}/scores`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    if (!res.ok) throw new Error('Failed to fetch leaderboard');
    return res.json();
  },

  /** Subscribe to live updates via SSE. Returns an EventSource. */
  subscribeStream: (tournamentId: number, onMessage: (data: LiveLeaderboardDto) => void): EventSource => {
    const token = localStorage.getItem('token');
    const url = `${API_BASE}/live/${tournamentId}/stream`;
    const es = new EventSource(url + (token ? `?token=${token}` : ''));
    es.onmessage = (e) => {
      try {
        const data = JSON.parse(e.data) as LiveLeaderboardDto;
        onMessage(data);
      } catch (_) {}
    };
    return es;
  },
};
