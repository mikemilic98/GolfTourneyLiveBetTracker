import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { tournamentsApi, Tournament } from '../api/tournaments';
import { picksApi, Pick } from '../api/picks';
import { rulesApi, GameRules } from '../api/rules';

export default function PicksPage() {
  const [selectedTournamentId, setSelectedTournamentId] = useState<number | null>(null);
  const [selectedPlayers, setSelectedPlayers] = useState<string[]>([]);
  const queryClient = useQueryClient();

  const { data: tournaments = [] } = useQuery({
    queryKey: ['tournaments'],
    queryFn: () => tournamentsApi.list().then((r) => r.data),
  });

  const { data: roster = [], isLoading: rosterLoading } = useQuery({
    queryKey: ['roster', selectedTournamentId],
    queryFn: () => tournamentsApi.getRoster(selectedTournamentId!).then((r) => r.data),
    enabled: !!selectedTournamentId,
  });

  const { data: rules } = useQuery({
    queryKey: ['rules', selectedTournamentId],
    queryFn: () => rulesApi.getByTournament(selectedTournamentId!).then((r) => r.data),
    enabled: !!selectedTournamentId,
  });

  const { data: myPicks = [] } = useQuery({
    queryKey: ['picks', 'me', selectedTournamentId],
    queryFn: () =>
      picksApi.listMyPicks(selectedTournamentId ?? undefined).then((r) => r.data),
    enabled: !!selectedTournamentId,
  });

  const numPicks = rules?.numPicks ?? 5;
  const existingPick = myPicks.find((p) => p.tournamentId === selectedTournamentId);

  const createMutation = useMutation({
    mutationFn: (data: { tournamentId: number; playerNames: string[] }) =>
      picksApi.createMyPick(data),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['picks'] }),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: { playerNames: string[] } }) =>
      picksApi.updateMyPick(id, data),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['picks'] }),
  });

  useEffect(() => {
    if (existingPick) setSelectedPlayers(existingPick.playerNames);
    else setSelectedPlayers([]);
  }, [existingPick, selectedTournamentId]);

  const togglePlayer = (name: string) => {
    setSelectedPlayers((prev) =>
      prev.includes(name) ? prev.filter((p) => p !== name) : [...prev, name]
    );
  };

  const handleSubmit = () => {
    if (!selectedTournamentId || selectedPlayers.length !== numPicks) return;
    if (existingPick) {
      updateMutation.mutate({ id: existingPick.id, data: { playerNames: selectedPlayers } });
    } else {
      createMutation.mutate({ tournamentId: selectedTournamentId, playerNames: selectedPlayers });
    }
  };

  const error =
    createMutation.error?.response?.data?.error || updateMutation.error?.response?.data?.error;

  return (
    <div style={{ padding: '1.5rem', maxWidth: 600 }}>
      <h2>Manage My Picks</h2>
      <div style={{ marginBottom: '1rem' }}>
        <label>Tournament: </label>
        <select
          value={selectedTournamentId ?? ''}
          onChange={(e) => setSelectedTournamentId(e.target.value ? Number(e.target.value) : null)}
        >
          <option value="">Select a tournament</option>
          {tournaments.map((t) => (
            <option key={t.id} value={t.id}>
              {t.name} ({t.rosterCount} players)
            </option>
          ))}
        </select>
      </div>

      {selectedTournamentId && (
        <>
          <p>
            Select {numPicks} players. {roster.length > 0 ? `${roster.length} in roster.` : 'Roster not loaded.'}
          </p>
          {error && <p style={{ color: 'red' }}>{error}</p>}
          {rosterLoading ? (
            <p>Loading roster...</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', marginBottom: '1rem' }}>
              {roster.map((name) => (
                <label key={name} style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
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
          )}
          <button
            onClick={handleSubmit}
            disabled={selectedPlayers.length !== numPicks || createMutation.isPending || updateMutation.isPending}
          >
            {existingPick ? 'Update Picks' : 'Save Picks'}
          </button>
        </>
      )}
    </div>
  );
}
