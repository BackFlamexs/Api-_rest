import { useState, useEffect } from 'react';
import { apiFetch } from '../services/api';
import { Plus, User, Shield, Trophy, X, Eye, Flag, Pencil, Trash2, AlertTriangle, Castle } from 'lucide-react';

interface ClanOption {
  id: number;
  name: string;
}

interface Player {
  id: number;
  nickname: string;
  level: number;
  role: string;
  clan?: { id: number; name: string };
  village?: { id: number; name: string; townHallLevel: number };
}

export default function PlayersPage() {
  const [players, setPlayers] = useState<Player[]>([]);
  const [clans, setClans] = useState<ClanOption[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Modals state
  const [showModal, setShowModal] = useState(false);
  const [selectedPlayer, setSelectedPlayer] = useState<Player | null>(null);
  const [playerToDelete, setPlayerToDelete] = useState<Player | null>(null);
  const [editMode, setEditMode] = useState<Player | null>(null);

  // Form states
  const [nickname, setNickname] = useState('');
  const [level, setLevel] = useState('');
  const [role, setRole] = useState('MEMBRO');
  const [clanId, setClanId] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const fetchPlayers = async () => {
    try {
      setIsLoading(true);
      const res = await apiFetch('/players?size=1000');
      if (res.ok) {
        const data = await res.json();
        let parsedPlayers = [];
        if (Array.isArray(data)) {
          parsedPlayers = data;
        } else if (data.content && Array.isArray(data.content)) {
          parsedPlayers = data.content;
        } else if (data._embedded && data._embedded.players) {
          parsedPlayers = data._embedded.players;
        } else if (data._embedded && data._embedded.playerList) {
          parsedPlayers = data._embedded.playerList;
        }
        setPlayers(parsedPlayers);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const fetchClans = async () => {
    try {
      const res = await apiFetch('/clans?size=1000', {
        headers: { 'X-API-Version': '2' }
      });
      if (res.ok) {
        const data = await res.json();
        setClans(Array.isArray(data) ? data : data.content || []);
      }
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    fetchPlayers();
    fetchClans();
  }, []);

  const openCreateModal = () => {
    setEditMode(null);
    setNickname('');
    setLevel('');
    setRole('MEMBRO');
    setClanId('');
    setError(null);
    setShowModal(true);
  };

  const openEditModal = (player: Player) => {
    setEditMode(player);
    setNickname(player.nickname);
    setLevel(player.level.toString());
    setRole(player.role);
    setClanId(player.clan ? player.clan.id.toString() : '');
    setError(null);
    setShowModal(true);
  };

  const handleSavePlayer = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!nickname) return;
    
    setIsSubmitting(true);
    setError(null);
    
    const payload: any = {
      nickname,
      level: parseInt(level) || 1,
      role
    };

    if (clanId) {
      payload.clan = { id: parseInt(clanId) };
    } else if (editMode) {
      payload.clan = null; // Backend suporta clan=null para desvincular? No controller ele checa se getClan() != null e getId() != null. Se passarmos sem "clan" no payload, getClan() será null e ele desvincula.
    }

    try {
      let res;
      if (editMode) {
        res = await apiFetch(`/players/${editMode.id}`, {
          method: 'PUT',
          body: JSON.stringify(payload)
        });
      } else {
        res = await apiFetch('/players', {
          method: 'POST',
          body: JSON.stringify(payload)
        });
      }

      if (!res.ok) {
        const errData = await res.json().catch(() => ({}));
        throw new Error(errData.message || 'Falha ao salvar jogador.');
      }

      setShowModal(false);
      fetchPlayers(); // Reload list
    } catch (err: any) {
      setError(err.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeletePlayer = async () => {
    if (!playerToDelete) return;
    setIsSubmitting(true);
    setDeleteError(null);

    try {
      const res = await apiFetch(`/players/${playerToDelete.id}`, {
        method: 'DELETE'
      });

      if (!res.ok) {
        const errData = await res.json().catch(() => ({}));
        throw new Error(errData.message || 'Falha ao excluir jogador.');
      }

      setPlayerToDelete(null);
      fetchPlayers();
    } catch (err: any) {
      setDeleteError(err.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="animate-fade-in">
      <div className="flex justify-between items-center mb-10">
        <div>
          <h1 className="text-3xl font-bold text-white mb-2 tracking-tight">Jogadores</h1>
          <p className="text-zinc-400">Gerencie todos os membros do ecossistema.</p>
        </div>
        <button
          onClick={openCreateModal}
          className="bg-purple-600 hover:bg-purple-700 text-white px-5 py-2.5 rounded-lg font-semibold flex items-center gap-2 transition-colors shadow-[0_0_15px_rgba(147,51,234,0.2)] hover:shadow-[0_0_20px_rgba(147,51,234,0.4)]"
        >
          <Plus size={20} />
          <span>Novo Jogador</span>
        </button>
      </div>

      <div className="bg-zinc-950 border border-zinc-900 rounded-xl overflow-hidden shadow-2xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-zinc-900/50 border-b border-zinc-800 text-zinc-400 text-xs uppercase tracking-wider font-semibold">
                <th className="px-6 py-4">Nickname</th>
                <th className="px-6 py-4">Level</th>
                <th className="px-6 py-4">Cargo</th>
                <th className="px-6 py-4">Clã</th>
                <th className="px-6 py-4 text-right">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-900">
              {isLoading ? (
                <tr>
                  <td colSpan={5} className="px-6 py-12 text-center text-zinc-500">Carregando jogadores...</td>
                </tr>
              ) : players.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-6 py-12 text-center text-zinc-500">Nenhum jogador encontrado.</td>
                </tr>
              ) : (
                players.map(player => (
                  <tr key={player.id} className="hover:bg-zinc-900/50 transition-colors">
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <div className="bg-zinc-900 p-2 rounded-full border border-zinc-800">
                          <User size={16} className="text-zinc-400" />
                        </div>
                        <span className="font-semibold text-white">{player.nickname}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2 text-zinc-300">
                        <Trophy size={16} className="text-purple-500" />
                        <span className="font-medium">{player.level}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[10px] font-bold tracking-wider uppercase bg-purple-600/10 text-purple-400 border border-purple-500/20">
                        <Shield size={12} />
                        {player.role.replace('_', ' ')}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-zinc-400">
                      {player.clan ? player.clan.name : <span className="text-zinc-600 italic">Sem clã</span>}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          onClick={() => setSelectedPlayer(player)}
                          className="p-2 bg-zinc-900 hover:bg-zinc-800 border border-zinc-800 hover:border-purple-500/50 text-zinc-400 hover:text-purple-400 rounded-lg transition-all"
                          title="Ver Detalhes"
                        >
                          <Eye size={16} />
                        </button>
                        <button
                          onClick={() => openEditModal(player)}
                          className="p-2 bg-zinc-900 hover:bg-zinc-800 border border-zinc-800 hover:border-blue-500/50 text-zinc-400 hover:text-blue-400 rounded-lg transition-all"
                          title="Editar Jogador"
                        >
                          <Pencil size={16} />
                        </button>
                        <button
                          onClick={() => setPlayerToDelete(player)}
                          className="p-2 bg-zinc-900 hover:bg-zinc-800 border border-zinc-800 hover:border-red-500/50 text-zinc-400 hover:text-red-400 rounded-lg transition-all"
                          title="Excluir Jogador"
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal Salvar/Editar Jogador */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="bg-zinc-950 border border-zinc-800 rounded-xl w-full max-w-lg shadow-2xl overflow-hidden animate-fade-in">
            <div className="flex justify-between items-center p-6 border-b border-zinc-900">
              <h2 className="text-xl font-bold text-white tracking-tight">
                {editMode ? 'Editar Jogador' : 'Cadastrar Novo Jogador'}
              </h2>
              <button onClick={() => setShowModal(false)} className="text-zinc-500 hover:text-white transition-colors">
                <X size={24} />
              </button>
            </div>
            
            <form onSubmit={handleSavePlayer} className="p-6 space-y-5">
              <div>
                <label className="block text-xs font-semibold text-zinc-400 uppercase tracking-wider mb-2">Nickname</label>
                <input
                  type="text"
                  required
                  value={nickname}
                  onChange={(e) => setNickname(e.target.value)}
                  placeholder="Ex: TheLegend27"
                  className="w-full bg-black border border-zinc-800 rounded-lg px-4 py-3 text-white placeholder-zinc-600 focus:outline-none focus:border-purple-600 focus:ring-1 focus:ring-purple-600 transition-colors"
                />
              </div>
              
              <div className="flex gap-5">
                <div className="flex-1">
                  <label className="block text-xs font-semibold text-zinc-400 uppercase tracking-wider mb-2">Level</label>
                  <input
                    type="number"
                    min="1"
                    required
                    value={level}
                    onChange={(e) => setLevel(e.target.value)}
                    placeholder="1"
                    className="w-full bg-black border border-zinc-800 rounded-lg px-4 py-3 text-white placeholder-zinc-600 focus:outline-none focus:border-purple-600 focus:ring-1 focus:ring-purple-600 transition-colors"
                  />
                </div>
                <div className="flex-1">
                  <label className="block text-xs font-semibold text-zinc-400 uppercase tracking-wider mb-2">Cargo</label>
                  <select
                    value={role}
                    onChange={(e) => setRole(e.target.value)}
                    className="w-full bg-black border border-zinc-800 rounded-lg px-4 py-3 text-white focus:outline-none focus:border-purple-600 focus:ring-1 focus:ring-purple-600 transition-colors appearance-none"
                  >
                    <option value="MEMBRO">Membro</option>
                    <option value="ANCIAO">Ancião</option>
                    <option value="CO_LIDER">Co-Líder</option>
                    <option value="LIDER">Líder</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-zinc-400 uppercase tracking-wider mb-2">Vincular a um Clã (Opcional)</label>
                <select
                  value={clanId}
                  onChange={(e) => setClanId(e.target.value)}
                  className="w-full bg-black border border-zinc-800 rounded-lg px-4 py-3 text-white focus:outline-none focus:border-purple-600 focus:ring-1 focus:ring-purple-600 transition-colors appearance-none"
                >
                  <option value="">Sem clã</option>
                  {clans.map(c => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>

              {error && (
                <div className="p-3 rounded-lg bg-red-950/50 border border-red-900/50 text-red-400 text-sm">
                  {error}
                </div>
              )}

              <div className="flex gap-3 pt-4 mt-2">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="flex-1 px-4 py-3 rounded-lg text-zinc-400 hover:text-white hover:bg-zinc-900 transition-colors font-semibold border border-transparent hover:border-zinc-800"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="flex-1 px-4 py-3 rounded-lg bg-purple-600 hover:bg-purple-700 text-white transition-colors font-semibold disabled:opacity-50 shadow-[0_0_15px_rgba(147,51,234,0.2)] hover:shadow-[0_0_20px_rgba(147,51,234,0.4)]"
                >
                  {isSubmitting ? 'Salvando...' : 'Salvar Jogador'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Ver Detalhes do Jogador */}
      {selectedPlayer && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="bg-zinc-950 border border-purple-500/30 rounded-xl w-full max-w-md shadow-[0_0_40px_rgba(147,51,234,0.15)] overflow-hidden animate-fade-in">
            <div className="bg-black border-b border-zinc-900 p-6 flex justify-between items-start">
              <div className="flex gap-4 items-center">
                <div className="bg-purple-600/20 p-3 rounded-xl border border-purple-500/30">
                  <User className="text-purple-500" size={24} />
                </div>
                <div>
                  <h2 className="text-2xl font-bold text-white tracking-tight">{selectedPlayer.nickname}</h2>
                  <p className="text-zinc-500 text-xs font-mono mt-1">ID: #{selectedPlayer.id}</p>
                </div>
              </div>
              <button onClick={() => setSelectedPlayer(null)} className="text-zinc-500 hover:text-white transition-colors bg-zinc-900 p-1.5 rounded-lg border border-zinc-800 hover:border-zinc-600">
                <X size={20} />
              </button>
            </div>
            
            <div className="p-6 space-y-6">
              <div className="grid grid-cols-2 gap-4">
                <div className="bg-black p-4 rounded-lg border border-zinc-800">
                  <div className="flex items-center gap-2 mb-2 text-zinc-400">
                    <Trophy size={16} />
                    <span className="text-xs font-semibold uppercase tracking-wider">Level</span>
                  </div>
                  <span className="text-xl font-bold text-white">{selectedPlayer.level}</span>
                </div>
                
                <div className="bg-black p-4 rounded-lg border border-zinc-800">
                  <div className="flex items-center gap-2 mb-2 text-zinc-400">
                    <Shield size={16} />
                    <span className="text-xs font-semibold uppercase tracking-wider">Cargo</span>
                  </div>
                  <span className="text-lg font-bold text-white">{selectedPlayer.role.replace('_', ' ')}</span>
                </div>
              </div>

              <div>
                <h3 className="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-2">Afiliação</h3>
                <div className="bg-black border border-zinc-800 rounded-lg p-4 flex items-center gap-3">
                  <div className="bg-zinc-900 p-2 rounded-lg">
                    <Flag size={20} className={selectedPlayer.clan ? "text-purple-500" : "text-zinc-600"} />
                  </div>
                  <div>
                    {selectedPlayer.clan ? (
                      <>
                        <p className="text-white font-bold">{selectedPlayer.clan.name}</p>
                        <p className="text-zinc-500 text-xs font-mono">ID do Clã: #{selectedPlayer.clan.id}</p>
                      </>
                    ) : (
                      <p className="text-zinc-500 font-medium">Jogador Sem Clã</p>
                    )}
                  </div>
                </div>
              </div>

              {selectedPlayer.village && (
                <div>
                  <h3 className="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-2">Base Principal</h3>
                  <div className="bg-black border border-zinc-800 rounded-lg p-4 flex items-center gap-3">
                    <div className="bg-zinc-900 p-2 rounded-lg">
                      <Castle size={20} className="text-purple-500" />
                    </div>
                    <div>
                      <p className="text-white font-bold">{selectedPlayer.village.name}</p>
                      <p className="text-zinc-500 text-xs font-mono">CV {selectedPlayer.village.townHallLevel} • ID da Vila: #{selectedPlayer.village.id}</p>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Modal Confirmação de Exclusão */}
      {playerToDelete && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="bg-zinc-950 border border-red-500/30 rounded-xl w-full max-w-md shadow-[0_0_40px_rgba(239,68,68,0.15)] overflow-hidden animate-fade-in">
            <div className="p-6 text-center">
              <div className="w-16 h-16 bg-red-500/10 rounded-full flex items-center justify-center mx-auto mb-4 border border-red-500/20">
                <AlertTriangle className="text-red-500" size={32} />
              </div>
              <h2 className="text-xl font-bold text-white mb-2">Excluir Jogador</h2>
              <p className="text-zinc-400 text-sm mb-6">
                Tem certeza que deseja excluir o jogador <span className="font-bold text-white">{playerToDelete.nickname}</span>? Esta ação não pode ser desfeita. Se ele possuir uma vila, ela será apagada junto.
              </p>

              {deleteError && (
                <div className="mb-6 p-3 rounded-lg bg-red-950/50 border border-red-900/50 text-red-400 text-sm text-left">
                  <span className="font-bold">Atenção:</span> {deleteError}
                </div>
              )}

              <div className="flex gap-3">
                <button
                  onClick={() => setPlayerToDelete(null)}
                  disabled={isSubmitting}
                  className="flex-1 px-4 py-2.5 rounded-lg text-zinc-300 bg-zinc-900 hover:bg-zinc-800 transition-colors font-semibold border border-zinc-800 hover:border-zinc-700"
                >
                  Cancelar
                </button>
                <button
                  onClick={handleDeletePlayer}
                  disabled={isSubmitting}
                  className="flex-1 px-4 py-2.5 rounded-lg bg-red-600 hover:bg-red-700 text-white transition-colors font-semibold disabled:opacity-50 shadow-[0_0_15px_rgba(239,68,68,0.2)]"
                >
                  {isSubmitting ? 'Excluindo...' : 'Sim, Excluir'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
