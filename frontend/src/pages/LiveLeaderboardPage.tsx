import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { tournamentsApi } from '../api/tournaments';
import { liveApi, LiveLeaderboardDto } from '../api/live';

export default function LiveLeaderboardPage() {
  const [tournamentId, setTournamentId] = useState<number | null>(null);
  const [leaderboard, setLeaderboard] = useState<LiveLeaderboardDto | null>(null);

  const { data: tournaments = [] } = useQuery({
    queryKey: ['tournaments'],
    queryFn: () => tournamentsApi.list().then((r) => r.data),
  });

  useEffect(() => {
    if (!tournamentId) return;
    liveApi.getScores(tournamentId).then(setLeaderboard);
    const es = liveApi.subscribeStream(tournamentId, (data) => setLeaderboard(data));
    return () => es.close();
  }, [tournamentId]);

  return (
    <div style={{ padding: '1.5rem', maxWidth: 800 }}>
      <h2>Live Leaderboard</h2>
      <div style={{ marginBottom: '1rem' }}>
        <label>Tournament: </label>
        <select
          value={tournamentId ?? ''}
          onChange={(e) => setTournamentId(e.target.value ? Number(e.target.value) : null)}
        >
          <option value="">Select a tournament</option>
          {tournaments.map((t) => (
            <option key={t.id} value={t.id}>
              {t.name}
            </option>
          ))}
        </select>
      </div>

      {tournamentId && leaderboard && (
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ borderBottom: '2px solid #333' }}>
              <th style={{ textAlign: 'left', padding: 8 }}>Rank</th>
              <th style={{ textAlign: 'left', padding: 8 }}>Player</th>
              <th style={{ textAlign: 'right', padding: 8 }}>Score</th>
            </tr>
          </thead>
          <tbody>
            {leaderboard.rows.map((row) => (
              <tr key={row.displayName} style={{ borderBottom: '1px solid #ddd' }}>
                <td style={{ padding: 8 }}>{row.rank ?? '-'}</td>
                <td style={{ padding: 8 }}>{row.displayName}</td>
                <td style={{ textAlign: 'right', padding: 8 }}>{row.totalScore}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      {tournamentId && !leaderboard?.rows?.length && (
        <p>No scores yet. The leaderboard updates every 5 seconds when the tournament is live.</p>
      )}
    </div>
  );
}
