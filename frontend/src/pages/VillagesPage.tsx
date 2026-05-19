import { useState, useEffect } from 'react';
import { apiFetch } from '../services/api';
import { Plus, Castle, User, X, Eye, Pencil, Trash2, AlertTriangle } from 'lucide-react';
import { v4 as uuidv4 } from 'uuid';

interface Village {
  id: number;
  name: string;
  townHallLevel: number;
  player?: { id: number; nickname: string };
}

interface PlayerOption {
  id: number;
  nickname: string;
}

export default function VillagesPage() {
  const [villages, setVillages] = useState<Village[]>([]);
  const [players, setPlayers] = useState<PlayerOption[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Modals state
  const [showModal, setShowModal] = useState(false);
  const [selectedVillage, setSelectedVillage] = useState<Village | null>(null);
  const [villageToDelete, setVillageToDelete] = useState<Village | null>(null);
  const [editMode, setEditMode] = useState<Village | null>(null);

  // Form states
  const [name, setName] = useState('');
  const [townHallLevel, setTownHallLevel] = useState('1');
  const [playerId, setPlayerId] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const fetchVillages = async () => {
    try {
      setIsLoading(true);
      const res = await apiFetch('/villages?size=1000');
      if (res.ok) {
        const data = await res.json();
        // Handle array directly, or data.content (Spring PageImpl), or data._embedded.villages (Spring HATEOAS)
        let parsedVillages = [];
        if (Array.isArray(data)) {
          parsedVillages = data;
        } else if (data.content && Array.isArray(data.content)) {
          parsedVillages = data.content;
        } else if (data._embedded && data._embedded.villages) {
          parsedVillages = data._embedded.villages;
        } else if (data._embedded && data._embedded.villageList) {
          parsedVillages = data._embedded.villageList;
        }
        setVillages(parsedVillages);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const fetchPlayersForSelect = async () => {
    try {
      const res = await apiFetch('/players?size=1000');
      if (res.ok) {
        const data = await res.json();
        setPlayers(data.content || []);
      }
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    fetchVillages();
    fetchPlayersForSelect();
  }, []);

  const openCreateModal = () => {
    setEditMode(null);
    setName('');
    setTownHallLevel('1');
    setPlayerId('');
    setError(null);
    setShowModal(true);
  };

  const getAvailablePlayers = () => {
    if (editMode && editMode.player) {
      // If editing, allow the current owner to be selected (it will be preselected and shouldn't change)
      // but actually the backend doesn't allow changing owner in PUT /villages/{id}.
      // So we can just show the current player.
      return players.filter(p => p.id === editMode.player?.id);
    }
    // If creating, only show players who DO NOT have a village in the villages list
    return players.filter(p => !villages.some(v => v.player?.id === p.id));
  };

  const openEditModal = (village: Village) => {
    setEditMode(village);
    setName(village.name);
    setTownHallLevel(village.townHallLevel.toString());
    setPlayerId(village.player ? village.player.id.toString() : '');
    setError(null);
    setShowModal(true);
  };

  const handleSaveVillage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name || !playerId) {
      setError("Nome e Jogador são obrigatórios.");
      return;
    }
    
    setIsSubmitting(true);
    setError(null);
    
    const idempotencyKey = uuidv4();
    const payload = {
      name,
      townHallLevel: parseInt(townHallLevel) || 1,
      player: { id: parseInt(playerId) }
    };

    try {
      let res;
      if (editMode) {
        res = await apiFetch(`/villages/${editMode.id}`, {
          method: 'PUT',
          body: JSON.stringify(payload)
        });
      } else {
        res = await apiFetch('/villages', {
          method: 'POST',
          headers: { 'X-Idempotency-Key': idempotencyKey },
          body: JSON.stringify(payload)
        });
      }

      if (!res.ok) {
        const errData = await res.json().catch(() => ({}));
        
        if (res.status === 400) {
          throw new Error(errData.message || 'Ação negada: Verifique os dados enviados ou se o jogador já possui uma vila.');
        } else if (res.status === 409) {
          throw new Error(errData.message || 'Requisição duplicada: Esta operação já foi processada.');
        } else {
          throw new Error(errData.message || 'Falha ao salvar vila.');
        }
      }

      setShowModal(false);
      fetchVillages();
    } catch (err: any) {
      setError(err.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteVillage = async () => {
    if (!villageToDelete) return;
    setIsSubmitting(true);
    setDeleteError(null);

    try {
      const res = await apiFetch(`/villages/${villageToDelete.id}`, {
        method: 'DELETE'
      });

      if (!res.ok) {
        const errData = await res.json().catch(() => ({}));
        throw new Error(errData.message || 'Falha ao excluir vila.');
      }

      setVillageToDelete(null);
      fetchVillages();
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
          <h1 className="text-3xl font-bold text-white mb-2 tracking-tight">Centro de Vilas</h1>
          <p className="text-zinc-400">Administre as vilas e bases dos jogadores.</p>
        </div>
        <button
          onClick={openCreateModal}
          className="bg-purple-600 hover:bg-purple-700 text-white px-5 py-2.5 rounded-lg font-semibold flex items-center gap-2 transition-colors shadow-[0_0_15px_rgba(147,51,234,0.2)] hover:shadow-[0_0_20px_rgba(147,51,234,0.4)]"
        >
          <Plus size={20} />
          <span>Nova Vila</span>
        </button>
      </div>

      <div className="bg-zinc-950 border border-zinc-900 rounded-xl overflow-hidden shadow-2xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-zinc-900/50 border-b border-zinc-800 text-zinc-400 text-xs uppercase tracking-wider font-semibold">
                <th className="px-6 py-4">Vila</th>
                <th className="px-6 py-4">Nível do CV</th>
                <th className="px-6 py-4">Proprietário (Jogador)</th>
                <th className="px-6 py-4 text-right">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-900">
              {isLoading ? (
                <tr>
                  <td colSpan={4} className="px-6 py-12 text-center text-zinc-500">Carregando vilas...</td>
                </tr>
              ) : villages.length === 0 ? (
                <tr>
                  <td colSpan={4} className="px-6 py-12 text-center text-zinc-500">Nenhuma vila encontrada.</td>
                </tr>
              ) : (
                villages.map(village => (
                  <tr key={village.id} className="hover:bg-zinc-900/50 transition-colors">
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <div className="bg-zinc-900 p-2 rounded-full border border-zinc-800">
                          <Castle size={16} className="text-zinc-400" />
                        </div>
                        <span className="font-semibold text-white">{village.name}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center justify-center min-w-[2.5rem] px-3 py-1 rounded-full text-xs font-bold bg-purple-600/10 text-purple-400 border border-purple-500/20">
                        CV {village.townHallLevel}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2 text-zinc-300">
                        <User size={16} className="text-zinc-500" />
                        {village.player ? village.player.nickname : <span className="text-zinc-600 italic">Desconhecido</span>}
                      </div>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          onClick={() => setSelectedVillage(village)}
                          className="p-2 bg-zinc-900 hover:bg-zinc-800 border border-zinc-800 hover:border-purple-500/50 text-zinc-400 hover:text-purple-400 rounded-lg transition-all"
                          title="Ver Detalhes"
                        >
                          <Eye size={16} />
                        </button>
                        <button
                          onClick={() => openEditModal(village)}
                          className="p-2 bg-zinc-900 hover:bg-zinc-800 border border-zinc-800 hover:border-blue-500/50 text-zinc-400 hover:text-blue-400 rounded-lg transition-all"
                          title="Editar Vila"
                        >
                          <Pencil size={16} />
                        </button>
                        <button
                          onClick={() => setVillageToDelete(village)}
                          className="p-2 bg-zinc-900 hover:bg-zinc-800 border border-zinc-800 hover:border-red-500/50 text-zinc-400 hover:text-red-400 rounded-lg transition-all"
                          title="Excluir Vila"
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

      {/* Modal Salvar/Editar Vila */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="bg-zinc-950 border border-zinc-800 rounded-xl w-full max-w-lg shadow-2xl overflow-hidden animate-fade-in">
            <div className="flex justify-between items-center p-6 border-b border-zinc-900">
              <h2 className="text-xl font-bold text-white tracking-tight">
                {editMode ? 'Editar Vila' : 'Criar Nova Vila'}
              </h2>
              <button onClick={() => setShowModal(false)} className="text-zinc-500 hover:text-white transition-colors">
                <X size={24} />
              </button>
            </div>
            
            <form onSubmit={handleSaveVillage} className="p-6 space-y-5">
              <div>
                <label className="block text-xs font-semibold text-zinc-400 uppercase tracking-wider mb-2">Nome da Vila</label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Ex: Fortaleza de Ferro"
                  className="w-full bg-black border border-zinc-800 rounded-lg px-4 py-3 text-white placeholder-zinc-600 focus:outline-none focus:border-purple-600 focus:ring-1 focus:ring-purple-600 transition-colors"
                />
              </div>
              
              <div>
                <label className="block text-xs font-semibold text-zinc-400 uppercase tracking-wider mb-2">Nível do Centro de Vila</label>
                <input
                  type="number"
                  min="1"
                  max="16"
                  required
                  value={townHallLevel}
                  onChange={(e) => setTownHallLevel(e.target.value)}
                  placeholder="1"
                  className="w-full bg-black border border-zinc-800 rounded-lg px-4 py-3 text-white placeholder-zinc-600 focus:outline-none focus:border-purple-600 focus:ring-1 focus:ring-purple-600 transition-colors"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-zinc-400 uppercase tracking-wider mb-2">Proprietário (Jogador)</label>
                <select
                  required
                  value={playerId}
                  onChange={(e) => setPlayerId(e.target.value)}
                  className="w-full bg-black border border-zinc-800 rounded-lg px-4 py-3 text-white focus:outline-none focus:border-purple-600 focus:ring-1 focus:ring-purple-600 transition-colors appearance-none"
                >
                  <option value="" disabled>Selecione um jogador</option>
                  {getAvailablePlayers().map(p => (
                    <option key={p.id} value={p.id}>{p.nickname}</option>
                  ))}
                </select>
                <p className="text-xs text-zinc-500 mt-2">Lembrete: Um jogador só pode ter uma única vila no sistema.</p>
              </div>

              {error && (
                <div className="bg-red-950/50 border border-red-900/50 text-red-400 p-4 rounded-lg text-sm flex gap-2 items-start mt-2">
                  <span className="font-bold">Atenção:</span> {error}
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
                  {isSubmitting ? 'Salvando...' : 'Salvar Vila'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Ver Detalhes da Vila */}
      {selectedVillage && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="bg-zinc-950 border border-purple-500/30 rounded-xl w-full max-w-md shadow-[0_0_40px_rgba(147,51,234,0.15)] overflow-hidden animate-fade-in">
            <div className="bg-black border-b border-zinc-900 p-6 flex justify-between items-start">
              <div className="flex gap-4 items-center">
                <div className="bg-purple-600/20 p-3 rounded-xl border border-purple-500/30">
                  <Castle className="text-purple-500" size={24} />
                </div>
                <div>
                  <h2 className="text-2xl font-bold text-white tracking-tight">{selectedVillage.name}</h2>
                  <p className="text-zinc-500 text-xs font-mono mt-1">ID: #{selectedVillage.id}</p>
                </div>
              </div>
              <button onClick={() => setSelectedVillage(null)} className="text-zinc-500 hover:text-white transition-colors bg-zinc-900 p-1.5 rounded-lg border border-zinc-800 hover:border-zinc-600">
                <X size={20} />
              </button>
            </div>
            
            <div className="p-6 space-y-6">
              <div className="bg-black p-4 rounded-lg border border-zinc-800 text-center">
                <div className="flex items-center justify-center gap-2 mb-2 text-zinc-400">
                  <Castle size={16} />
                  <span className="text-xs font-semibold uppercase tracking-wider">Nível do Centro de Vila</span>
                </div>
                <span className="text-4xl font-black text-transparent bg-clip-text bg-gradient-to-r from-purple-400 to-purple-600">
                  CV {selectedVillage.townHallLevel}
                </span>
              </div>

              <div>
                <h3 className="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-2">Proprietário</h3>
                <div className="bg-black border border-zinc-800 rounded-lg p-4 flex items-center gap-3">
                  <div className="bg-zinc-900 p-2 rounded-lg">
                    <User size={20} className={selectedVillage.player ? "text-purple-500" : "text-zinc-600"} />
                  </div>
                  <div>
                    {selectedVillage.player ? (
                      <>
                        <p className="text-white font-bold">{selectedVillage.player.nickname}</p>
                        <p className="text-zinc-500 text-xs font-mono">ID do Jogador: #{selectedVillage.player.id}</p>
                      </>
                    ) : (
                      <p className="text-zinc-500 font-medium">Sem Proprietário (Órfã)</p>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal Confirmação de Exclusão */}
      {villageToDelete && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="bg-zinc-950 border border-red-500/30 rounded-xl w-full max-w-md shadow-[0_0_40px_rgba(239,68,68,0.15)] overflow-hidden animate-fade-in">
            <div className="p-6 text-center">
              <div className="w-16 h-16 bg-red-500/10 rounded-full flex items-center justify-center mx-auto mb-4 border border-red-500/20">
                <AlertTriangle className="text-red-500" size={32} />
              </div>
              <h2 className="text-xl font-bold text-white mb-2">Excluir Vila</h2>
              <p className="text-zinc-400 text-sm mb-6">
                Tem certeza que deseja excluir a vila <span className="font-bold text-white">{villageToDelete.name}</span>? Esta ação não pode ser desfeita. O jogador proprietário continuará existindo, mas perderá o vínculo com a base.
              </p>

              {deleteError && (
                <div className="mb-6 p-3 rounded-lg bg-red-950/50 border border-red-900/50 text-red-400 text-sm text-left">
                  <span className="font-bold">Atenção:</span> {deleteError}
                </div>
              )}

              <div className="flex gap-3">
                <button
                  onClick={() => setVillageToDelete(null)}
                  disabled={isSubmitting}
                  className="flex-1 px-4 py-2.5 rounded-lg text-zinc-300 bg-zinc-900 hover:bg-zinc-800 transition-colors font-semibold border border-zinc-800 hover:border-zinc-700"
                >
                  Cancelar
                </button>
                <button
                  onClick={handleDeleteVillage}
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
