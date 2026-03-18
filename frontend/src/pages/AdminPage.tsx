import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../contexts/AuthContext';
import { tournamentsApi, Tournament } from '../api/tournaments';
import { rulesApi, GameRules } from '../api/rules';
import { picksApi, Pick } from '../api/picks';
import { usersApi } from '../api/users';

const sectionStyle = { marginBottom: 24, padding: 16, border: '1px solid #ddd', borderRadius: 8 };

export default function AdminPage() {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState<'tournaments' | 'rules' | 'picks'>('tournaments');
  const queryClient = useQueryClient();

  if (user?.role !== 'ADMIN') {
    return <p>Admin access required.</p>;
  }

  const { data: tournaments = [] } = useQuery({
    queryKey: ['tournaments'],
    queryFn: () => tournamentsApi.list().then((r) => r.data),
  });

  const { data: rulesList = [] } = useQuery({
    queryKey: ['rules'],
    queryFn: () => rulesApi.list().then((r) => r.data),
  });

  const ingestMutation = useMutation({
    mutationFn: (id: number) => tournamentsApi.ingestRoster(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['tournaments'] }),
  });

  return (
    <div style={{ padding: '1.5rem', maxWidth: 900 }}>
      <h2>Admin</h2>
      <div style={{ marginBottom: '1rem', display: 'flex', gap: 8 }}>
        <button onClick={() => setActiveTab('tournaments')}>Tournaments</button>
        <button onClick={() => setActiveTab('rules')}>Rules</button>
        <button onClick={() => setActiveTab('picks')}>Manual Picks</button>
      </div>

      {activeTab === 'tournaments' && (
        <TournamentsSection tournaments={tournaments} ingestMutation={ingestMutation} />
      )}
      {activeTab === 'rules' && <RulesSection tournaments={tournaments} rulesList={rulesList} />}
      {activeTab === 'picks' && <PicksAdminSection tournaments={tournaments} />}
    </div>
  );
}

function TournamentsSection({
  tournaments,
  ingestMutation,
}: {
  tournaments: Tournament[];
  ingestMutation: { mutate: (id: number) => void; isPending: boolean };
}) {
  const [showForm, setShowForm] = useState(false);
  const [name, setName] = useState('');
  const [espnUrl, setEspnUrl] = useState('https://www.espn.com/golf/leaderboard');
  const queryClient = useQueryClient();

  const createMutation = useMutation({
    mutationFn: (data: { name: string; espnUrl: string }) =>
      tournamentsApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tournaments'] });
      setShowForm(false);
      setName('');
      setEspnUrl('https://www.espn.com/golf/leaderboard');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => tournamentsApi.delete(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['tournaments'] }),
  });

  return (
    <div>
      <h3>Tournaments</h3>
      <button onClick={() => setShowForm(!showForm)} style={{ marginBottom: 12 }}>
        {showForm ? 'Cancel' : '+ Create Tournament'}
      </button>
      {showForm && (
        <div style={sectionStyle}>
          <div style={{ marginBottom: 8 }}>
            <label>Name: </label>
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Valspar Championship"
              style={{ marginLeft: 8, padding: 4, width: 300 }}
            />
          </div>
          <div style={{ marginBottom: 8 }}>
            <label>ESPN URL: </label>
            <input
              value={espnUrl}
              onChange={(e) => setEspnUrl(e.target.value)}
              placeholder="https://www.espn.com/golf/leaderboard?tournamentId=401811938"
              style={{ marginLeft: 8, padding: 4, width: 400 }}
            />
          </div>
          <button
            onClick={() => createMutation.mutate({ name, espnUrl })}
            disabled={!name.trim() || !espnUrl.trim() || createMutation.isPending}
          >
            Create
          </button>
          {createMutation.error && (
            <span style={{ color: 'red', marginLeft: 12 }}>
              {(createMutation.error as { response?: { data?: { error?: string } } })?.response?.data?.error}
            </span>
          )}
        </div>
      )}
      <ul style={{ listStyle: 'none', padding: 0 }}>
        {tournaments.map((t) => (
          <li key={t.id} style={{ ...sectionStyle, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              <strong>{t.name}</strong> — Roster: {t.rosterCount} players
              <br />
              <small style={{ color: '#666' }}>{t.espnUrl}</small>
            </div>
            <div>
              <button
                onClick={() => ingestMutation.mutate(t.id)}
                disabled={ingestMutation.isPending}
                style={{ marginRight: 8 }}
              >
                Ingest Roster
              </button>
              <button
                onClick={() => deleteMutation.mutate(t.id)}
                disabled={deleteMutation.isPending}
                style={{ color: 'crimson' }}
              >
                Delete
              </button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}

function RulesSection({
  tournaments,
  rulesList,
}: {
  tournaments: Tournament[];
  rulesList: GameRules[];
}) {
  const [showForm, setShowForm] = useState(false);
  const [tournamentId, setTournamentId] = useState<number | null>(null);
  const [numPicks, setNumPicks] = useState(5);
  const [numDropped, setNumDropped] = useState(1);
  const queryClient = useQueryClient();

  const createMutation = useMutation({
    mutationFn: (data: { tournamentId: number; numPicks: number; numDropped: number }) =>
      rulesApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['rules'] });
      setShowForm(false);
    },
  });

  const lockMutation = useMutation({
    mutationFn: (id: number) => rulesApi.update(id, { lock: true }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['rules'] }),
  });

  return (
    <div>
      <h3>Game Rules</h3>
      <button onClick={() => setShowForm(!showForm)} style={{ marginBottom: 12 }}>
        {showForm ? 'Cancel' : '+ Create Rules'}
      </button>
      {showForm && (
        <div style={sectionStyle}>
          <div style={{ marginBottom: 8 }}>
            <label>Tournament: </label>
            <select
              value={tournamentId ?? ''}
              onChange={(e) => setTournamentId(e.target.value ? Number(e.target.value) : null)}
              style={{ marginLeft: 8, padding: 4 }}
            >
              <option value="">Select tournament</option>
              {tournaments.filter((t) => !rulesList.some((r) => r.tournamentId === t.id)).map((t) => (
                <option key={t.id} value={t.id}>{t.name}</option>
              ))}
            </select>
          </div>
          <div style={{ marginBottom: 8 }}>
            <label>Number of picks: </label>
            <input
              type="number"
              min={1}
              value={numPicks}
              onChange={(e) => setNumPicks(Number(e.target.value))}
              style={{ marginLeft: 8, padding: 4, width: 60 }}
            />
          </div>
          <div style={{ marginBottom: 8 }}>
            <label>Drop worst: </label>
            <input
              type="number"
              min={0}
              value={numDropped}
              onChange={(e) => setNumDropped(Number(e.target.value))}
              style={{ marginLeft: 8, padding: 4, width: 60 }}
            />
          </div>
          <button
            onClick={() => tournamentId && createMutation.mutate({ tournamentId, numPicks, numDropped })}
            disabled={!tournamentId || createMutation.isPending}
          >
            Create
          </button>
        </div>
      )}
      <ul style={{ listStyle: 'none', padding: 0 }}>
        {rulesList.map((r) => (
          <li key={r.id} style={{ marginBottom: 8, padding: 12, border: '1px solid #eee', borderRadius: 4 }}>
            <strong>{r.tournamentName}</strong>: {r.numPicks} picks, drop {r.numDropped}, WD penalty={r.wdPenaltyPosition}
            {r.lockedAt ? ' [LOCKED]' : (
              <button onClick={() => lockMutation.mutate(r.id)} style={{ marginLeft: 8 }}>Lock</button>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}

function PicksAdminSection({ tournaments }: { tournaments: Tournament[] }) {
  const [tournamentId, setTournamentId] = useState<number | null>(null);
  const [createPick, setCreatePick] = useState(false);
  const [userId, setUserId] = useState<number | null>(null);
  const [selectedPlayers, setSelectedPlayers] = useState<string[]>([]);
  const queryClient = useQueryClient();

  const { data: picks = [] } = useQuery({
    queryKey: ['picks', 'tournament', tournamentId],
    queryFn: () => picksApi.listByTournament(tournamentId!).then((r) => r.data),
    enabled: !!tournamentId,
  });

  const { data: users = [] } = useQuery({
    queryKey: ['users'],
    queryFn: () => usersApi.list().then((r) => r.data),
    enabled: createPick,
  });

  const { data: roster = [] } = useQuery({
    queryKey: ['roster', tournamentId],
    queryFn: () => tournamentsApi.getRoster(tournamentId!).then((r) => r.data),
    enabled: !!tournamentId && createPick,
  });

  const { data: rules } = useQuery({
    queryKey: ['rules', tournamentId],
    queryFn: () => rulesApi.getByTournament(tournamentId!).then((r) => r.data),
    enabled: !!tournamentId && createPick,
  });

  const createMutation = useMutation({
    mutationFn: (data: { userId: number; tournamentId: number; playerNames: string[] }) =>
      picksApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['picks'] });
      setCreatePick(false);
      setUserId(null);
      setSelectedPlayers([]);
    },
  });

  const numPicks = rules?.numPicks ?? 5;

  const togglePlayer = (name: string) => {
    setSelectedPlayers((prev) =>
      prev.includes(name) ? prev.filter((p) => p !== name) : [...prev, name]
    );
  };

  return (
    <div>
      <h3>Manual Pick Entry</h3>
      <div style={{ marginBottom: 12 }}>
        <label>Tournament: </label>
        <select
          value={tournamentId ?? ''}
          onChange={(e) => {
            setTournamentId(e.target.value ? Number(e.target.value) : null);
            setCreatePick(false);
          }}
          style={{ marginLeft: 8, padding: 4, minWidth: 250 }}
        >
          <option value="">Select tournament</option>
          {tournaments.map((t) => (
            <option key={t.id} value={t.id}>{t.name}</option>
          ))}
        </select>
      </div>

      {tournamentId && (
        <>
          <button onClick={() => setCreatePick(!createPick)} style={{ marginBottom: 12 }}>
            {createPick ? 'Cancel' : '+ Create Pick for User'}
          </button>

          {createPick && (
            <div style={sectionStyle}>
              <div style={{ marginBottom: 12 }}>
                <label>User: </label>
                <select
                  value={userId ?? ''}
                  onChange={(e) => setUserId(e.target.value ? Number(e.target.value) : null)}
                  style={{ marginLeft: 8, padding: 4, minWidth: 200 }}
                >
                  <option value="">Select user</option>
                  {users.map((u) => (
                    <option key={u.id} value={u.id}>{u.displayName} ({u.email})</option>
                  ))}
                </select>
              </div>
              <p>Select {numPicks} players from roster:</p>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginBottom: 12, maxHeight: 200, overflowY: 'auto' }}>
                {roster.map((name) => (
                  <label key={name} style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                    <input
                      type="checkbox"
                      checked={selectedPlayers.includes(name)}
                      onChange={() => togglePlayer(name)}
                      disabled={!selectedPlayers.includes(name) && selectedPlayers.length >= numPicks}
                    />
                    {name}
                  </label>
                ))}
              </div>
              <button
                onClick={() =>
                  userId &&
                  tournamentId &&
                  selectedPlayers.length === numPicks &&
                  createMutation.mutate({ userId, tournamentId, playerNames: selectedPlayers })
                }
                disabled={!userId || selectedPlayers.length !== numPicks || createMutation.isPending}
              >
                Create Pick
              </button>
              {createMutation.error && (
                <span style={{ color: 'red', marginLeft: 12 }}>
                  {(createMutation.error as { response?: { data?: { error?: string } } })?.response?.data?.error}
                </span>
              )}
            </div>
          )}

          <h4 style={{ marginTop: 16 }}>Existing Picks</h4>
          <ul style={{ listStyle: 'none', padding: 0 }}>
            {picks.map((p) => (
              <li key={p.id} style={{ marginBottom: 8, padding: 12, border: '1px solid #eee', borderRadius: 4 }}>
                <strong>{p.userDisplayName}</strong>: {p.playerNames.join(', ')}
              </li>
            ))}
          </ul>
        </>
      )}
    </div>
  );
}
