import { useState, useEffect } from 'react';
import { apiFetch } from '../services/api';
import { Plus, Shield, X, Eye, Flag, Users, Pencil, Trash2, AlertTriangle, Lock, Unlock } from 'lucide-react';
import { v4 as uuidv4 } from 'uuid';

interface Clan {
  id: number;
  name: string;
  description: string;
  requiredTrophies: number;
  isPublic?: boolean;
  totalMembers?: number; // Only from API v2
}

export default function ClansPage() {
  const [clans, setClans] = useState<Clan[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  
  // Modals state
  const [showModal, setShowModal] = useState(false);
  const [selectedClan, setSelectedClan] = useState<Clan | null>(null);
  const [clanToDelete, setClanToDelete] = useState<Clan | null>(null);
  const [editMode, setEditMode] = useState<Clan | null>(null);

  // Members state for selected clan
  const [clanMembers, setClanMembers] = useState<{id: number, nickname: string, role: string}[]>([]);
  const [isLoadingMembers, setIsLoadingMembers] = useState(false);

  // Form states
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [requiredTrophies, setRequiredTrophies] = useState('0');
  const [isPublic, setIsPublic] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const fetchClans = async () => {
    try {
      setIsLoading(true);
      const res = await apiFetch('/clans?size=1000', {
        headers: { 'X-API-Version': '2' }
      });
      if (res.ok) {
        const data = await res.json();
        setClans(Array.isArray(data) ? data : data.content || []);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchClans();
  }, []);

  // Fetch players when a clan is selected
  useEffect(() => {
    if (selectedClan) {
      const fetchMembers = async () => {
        setIsLoadingMembers(true);
        try {
          const res = await apiFetch('/players?size=1000');
          if (res.ok) {
            const data = await res.json();
            let allPlayers = [];
            if (Array.isArray(data)) {
              allPlayers = data;
            } else if (data.content && Array.isArray(data.content)) {
              allPlayers = data.content;
            } else if (data._embedded && data._embedded.players) {
              allPlayers = data._embedded.players;
            } else if (data._embedded && data._embedded.playerList) {
              allPlayers = data._embedded.playerList;
            }
            const members = allPlayers.filter((p: any) => p.clan?.id === selectedClan.id);
            setClanMembers(members);
          }
        } catch (err) {
          console.error(err);
        } finally {
          setIsLoadingMembers(false);
        }
      };
      fetchMembers();
    } else {
      setClanMembers([]);
    }
  }, [selectedClan]);

  const openCreateModal = () => {
    setEditMode(null);
    setName('');
    setDescription('');
    setRequiredTrophies('0');
    setIsPublic(true);
    setError(null);
    setShowModal(true);
  };

  const openEditModal = (clan: Clan) => {
    setEditMode(clan);
    setName(clan.name);
    setDescription(clan.description);
    setRequiredTrophies(clan.requiredTrophies.toString());
    setIsPublic(clan.isPublic ?? true);
    setError(null);
    setShowModal(true);
  };

  const handleSaveClan = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name) return;
    
    setIsSubmitting(true);
    setError(null);
    
    const idempotencyKey = uuidv4();
    const payload = {
      name,
      description,
      requiredTrophies: parseInt(requiredTrophies) || 0,
      isPublic
    };

    try {
      let res;
      if (editMode) {
        res = await apiFetch(`/clans/${editMode.id}`, {
          method: 'PUT',
          body: JSON.stringify(payload)
        });
      } else {
        res = await apiFetch('/clans', {
          method: 'POST',
          headers: { 'X-Idempotency-Key': idempotencyKey },
          body: JSON.stringify(payload)
        });
      }

      if (!res.ok) {
        if (res.status === 409) {
          throw new Error('Conflito: Já existe um clã com esse nome ou a operação foi duplicada.');
        } else {
          const errData = await res.json().catch(() => ({}));
          throw new Error(errData.message || 'Falha ao salvar clã.');
        }
      }

      setShowModal(false);
      fetchClans();
    } catch (err: any) {
      setError(err.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteClan = async () => {
    if (!clanToDelete) return;
    setIsSubmitting(true);
    setDeleteError(null);

    try {
      const res = await apiFetch(`/clans/${clanToDelete.id}`, {
        method: 'DELETE'
      });

      if (!res.ok) {
        const errData = await res.json().catch(() => ({}));
        throw new Error(errData.message || 'Falha ao excluir clã.');
      }

      setClanToDelete(null);
      fetchClans();
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
          <h1 className="text-3xl font-bold text-white mb-2 tracking-tight">Clãs</h1>
          <p className="text-zinc-400">Gerencie as alianças e grupos do jogo.</p>
        </div>
        <button
          onClick={openCreateModal}
          className="bg-purple-600 hover:bg-purple-700 text-white px-5 py-2.5 rounded-lg font-semibold flex items-center gap-2 transition-colors shadow-[0_0_15px_rgba(147,51,234,0.2)] hover:shadow-[0_0_20px_rgba(147,51,234,0.4)]"
        >
          <Plus size={20} />
          <span>Novo Clã</span>
        </button>
      </div>

      <div className="bg-zinc-950 border border-zinc-900 rounded-xl overflow-hidden shadow-2xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-zinc-900/50 border-b border-zinc-800 text-zinc-400 text-xs uppercase tracking-wider font-semibold">
                <th className="px-6 py-4">Nome do Clã</th>
                <th className="px-6 py-4">Tipo</th>
                <th className="px-6 py-4">Troféus Exigidos</th>
                <th className="px-6 py-4">Membros</th>
                <th className="px-6 py-4 text-right">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-900">
              {isLoading ? (
                <tr>
                  <td colSpan={5} className="px-6 py-12 text-center text-zinc-500">Carregando clãs...</td>
                </tr>
              ) : clans.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-6 py-12 text-center text-zinc-500">Nenhum clã encontrado.</td>
                </tr>
              ) : (
                clans.map(clan => (
                  <tr key={clan.id} className="hover:bg-zinc-900/50 transition-colors">
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <div className="bg-zinc-900 p-2 rounded-full border border-zinc-800">
                          <Flag size={16} className="text-zinc-400" />
                        </div>
                        <span className="font-semibold text-white">{clan.name}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      {clan.isPublic ?? true ? (
                        <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                          <Unlock size={12} />
                          Público
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider bg-red-500/10 text-red-400 border border-red-500/20">
                          <Lock size={12} />
                          Privado
                        </span>
                      )}
                    </td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center gap-2 font-mono text-zinc-300">
                        <Shield size={16} className="text-purple-500" />
                        {clan.requiredTrophies}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2 text-zinc-400">
                        <Users size={16} />
                        <span className="font-medium text-white">{clan.totalMembers ?? '-'} / 50</span>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          onClick={() => setSelectedClan(clan)}
                          className="p-2 bg-zinc-900 hover:bg-zinc-800 border border-zinc-800 hover:border-purple-500/50 text-zinc-400 hover:text-purple-400 rounded-lg transition-all"
                          title="Ver Detalhes"
                        >
                          <Eye size={16} />
                        </button>
                        <button
                          onClick={() => openEditModal(clan)}
                          className="p-2 bg-zinc-900 hover:bg-zinc-800 border border-zinc-800 hover:border-blue-500/50 text-zinc-400 hover:text-blue-400 rounded-lg transition-all"
                          title="Editar Clã"
                        >
                          <Pencil size={16} />
                        </button>
                        <button
                          onClick={() => setClanToDelete(clan)}
                          className="p-2 bg-zinc-900 hover:bg-zinc-800 border border-zinc-800 hover:border-red-500/50 text-zinc-400 hover:text-red-400 rounded-lg transition-all"
                          title="Excluir Clã"
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

      {/* Modal Salvar/Editar Clã */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="bg-zinc-950 border border-zinc-800 rounded-xl w-full max-w-lg shadow-2xl overflow-hidden animate-fade-in">
            <div className="flex justify-between items-center p-6 border-b border-zinc-900">
              <h2 className="text-xl font-bold text-white tracking-tight">
                {editMode ? 'Editar Clã' : 'Criar Novo Clã'}
              </h2>
              <button onClick={() => setShowModal(false)} className="text-zinc-500 hover:text-white transition-colors">
                <X size={24} />
              </button>
            </div>
            
            <form onSubmit={handleSaveClan} className="p-6 space-y-5">
              <div>
                <label className="block text-xs font-semibold text-zinc-400 uppercase tracking-wider mb-2">Nome do Clã</label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Ex: Guerreiros da Noite"
                  className="w-full bg-black border border-zinc-800 rounded-lg px-4 py-3 text-white placeholder-zinc-600 focus:outline-none focus:border-purple-600 focus:ring-1 focus:ring-purple-600 transition-colors"
                />
              </div>
              
              <div>
                <label className="block text-xs font-semibold text-zinc-400 uppercase tracking-wider mb-2">Descrição</label>
                <textarea
                  required
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Regras e apresentação do seu clã..."
                  rows={3}
                  className="w-full bg-black border border-zinc-800 rounded-lg px-4 py-3 text-white placeholder-zinc-600 focus:outline-none focus:border-purple-600 focus:ring-1 focus:ring-purple-600 transition-colors resize-none"
                />
              </div>
              
              <div className="flex gap-5">
                <div className="flex-1">
                  <label className="block text-xs font-semibold text-zinc-400 uppercase tracking-wider mb-2">Troféus Necessários</label>
                  <input
                    type="number"
                    min="0"
                    required
                    value={requiredTrophies}
                    onChange={(e) => setRequiredTrophies(e.target.value)}
                    placeholder="0"
                    className="w-full bg-black border border-zinc-800 rounded-lg px-4 py-3 text-white placeholder-zinc-600 focus:outline-none focus:border-purple-600 focus:ring-1 focus:ring-purple-600 transition-colors"
                  />
                </div>
                <div className="flex-1">
                  <label className="block text-xs font-semibold text-zinc-400 uppercase tracking-wider mb-2">Privacidade</label>
                  <select
                    value={isPublic ? 'true' : 'false'}
                    onChange={(e) => setIsPublic(e.target.value === 'true')}
                    className="w-full bg-black border border-zinc-800 rounded-lg px-4 py-3 text-white focus:outline-none focus:border-purple-600 focus:ring-1 focus:ring-purple-600 transition-colors appearance-none"
                  >
                    <option value="true">Público (Aberto)</option>
                    <option value="false">Privado (Fechado)</option>
                  </select>
                </div>
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
                  {isSubmitting ? 'Salvando...' : 'Salvar Clã'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Ver Detalhes do Clã */}
      {selectedClan && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="bg-zinc-950 border border-purple-500/30 rounded-xl w-full max-w-md shadow-[0_0_40px_rgba(147,51,234,0.15)] overflow-hidden animate-fade-in flex flex-col max-h-[90vh]">
            <div className="bg-black border-b border-zinc-900 p-6 flex justify-between items-start shrink-0">
              <div className="flex gap-4 items-center">
                <div className="bg-purple-600/20 p-3 rounded-xl border border-purple-500/30">
                  <Flag className="text-purple-500" size={24} />
                </div>
                <div>
                  <h2 className="text-2xl font-bold text-white tracking-tight">{selectedClan.name}</h2>
                  <p className="text-zinc-500 text-xs font-mono mt-1">ID: #{selectedClan.id}</p>
                </div>
              </div>
              <button onClick={() => setSelectedClan(null)} className="text-zinc-500 hover:text-white transition-colors bg-zinc-900 p-1.5 rounded-lg border border-zinc-800 hover:border-zinc-600">
                <X size={20} />
              </button>
            </div>
            
            <div className="p-6 space-y-6 overflow-y-auto">
              <div>
                <div className="flex items-center justify-between mb-2">
                  <h3 className="text-xs font-semibold text-zinc-500 uppercase tracking-wider">Descrição</h3>
                  {selectedClan.isPublic ?? true ? (
                    <span className="flex items-center gap-1 text-[10px] uppercase font-bold text-emerald-500 bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/20">
                      <Unlock size={10} /> Público
                    </span>
                  ) : (
                    <span className="flex items-center gap-1 text-[10px] uppercase font-bold text-red-500 bg-red-500/10 px-2 py-0.5 rounded border border-red-500/20">
                      <Lock size={10} /> Privado
                    </span>
                  )}
                </div>
                <p className="text-zinc-300 text-sm leading-relaxed bg-black p-4 rounded-lg border border-zinc-800">
                  {selectedClan.description || 'Nenhuma descrição fornecida.'}
                </p>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="bg-black p-4 rounded-lg border border-zinc-800">
                  <div className="flex items-center gap-2 mb-2 text-zinc-400">
                    <Shield size={16} />
                    <span className="text-xs font-semibold uppercase tracking-wider">Troféus</span>
                  </div>
                  <span className="text-xl font-bold text-white">{selectedClan.requiredTrophies}</span>
                </div>
                
                <div className="bg-black p-4 rounded-lg border border-zinc-800">
                  <div className="flex items-center gap-2 mb-2 text-zinc-400">
                    <Users size={16} />
                    <span className="text-xs font-semibold uppercase tracking-wider">Membros</span>
                  </div>
                  <span className="text-xl font-bold text-white">{selectedClan.totalMembers ?? 0} / 50</span>
                </div>
              </div>

              <div>
                <h3 className="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-2">Lista de Membros</h3>
                <div className="bg-black rounded-lg border border-zinc-800 max-h-48 overflow-y-auto">
                  {isLoadingMembers ? (
                    <div className="p-4 text-center text-zinc-500 text-sm">Carregando membros...</div>
                  ) : clanMembers.length === 0 ? (
                    <div className="p-4 text-center text-zinc-500 text-sm">Este clã não possui membros.</div>
                  ) : (
                    <ul className="divide-y divide-zinc-800">
                      {clanMembers.map(member => (
                        <li key={member.id} className="p-3 flex justify-between items-center hover:bg-zinc-900/50 transition-colors">
                          <span className="text-white font-medium text-sm">{member.nickname}</span>
                          <span className="text-[10px] font-bold uppercase tracking-wider text-purple-400 bg-purple-500/10 px-2.5 py-1 rounded border border-purple-500/20">
                            {member.role.replace('_', ' ')}
                          </span>
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal Confirmação de Exclusão */}
      {clanToDelete && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="bg-zinc-950 border border-red-500/30 rounded-xl w-full max-w-md shadow-[0_0_40px_rgba(239,68,68,0.15)] overflow-hidden animate-fade-in">
            <div className="p-6 text-center">
              <div className="w-16 h-16 bg-red-500/10 rounded-full flex items-center justify-center mx-auto mb-4 border border-red-500/20">
                <AlertTriangle className="text-red-500" size={32} />
              </div>
              <h2 className="text-xl font-bold text-white mb-2">Excluir Clã</h2>
              <p className="text-zinc-400 text-sm mb-6">
                Tem certeza que deseja excluir o clã <span className="font-bold text-white">{clanToDelete.name}</span>? Esta ação removerá a afiliação de todos os jogadores membros.
              </p>

              {deleteError && (
                <div className="mb-6 p-3 rounded-lg bg-red-950/50 border border-red-900/50 text-red-400 text-sm text-left">
                  <span className="font-bold">Atenção:</span> {deleteError}
                </div>
              )}

              <div className="flex gap-3">
                <button
                  onClick={() => setClanToDelete(null)}
                  disabled={isSubmitting}
                  className="flex-1 px-4 py-2.5 rounded-lg text-zinc-300 bg-zinc-900 hover:bg-zinc-800 transition-colors font-semibold border border-zinc-800 hover:border-zinc-700"
                >
                  Cancelar
                </button>
                <button
                  onClick={handleDeleteClan}
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
