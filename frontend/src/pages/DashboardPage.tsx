import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { picksApi } from '../api/picks';

export function DashboardPage() {
  const { user } = useAuth();

  const { data: myPicks = [] } = useQuery({
    queryKey: ['picks', 'me'],
    queryFn: () => picksApi.listMyPicks().then((r) => r.data),
  });

  return (
    <div style={{ padding: '1.5rem', maxWidth: 800 }}>
      <h1>Welcome, {user?.displayName ?? 'Guest'}</h1>
      <p>
        <Link to="/picks">Manage your picks</Link> · <Link to="/live">Live leaderboard</Link>
      </p>
      <h2>My Picks</h2>
      {myPicks.length === 0 ? (
        <p>You haven't made any picks yet. <Link to="/picks">Select a tournament and make your picks</Link>.</p>
      ) : (
        <ul style={{ listStyle: 'none', padding: 0 }}>
          {myPicks.map((p) => (
            <li key={p.id} style={{ marginBottom: 12, padding: 12, border: '1px solid #ddd', borderRadius: 4 }}>
              <strong>{p.tournamentName}</strong>
              <p style={{ margin: '4px 0 0', fontSize: 14 }}>Picks: {p.playerNames.join(', ')}</p>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
