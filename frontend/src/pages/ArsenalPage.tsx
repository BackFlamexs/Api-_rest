import { useState, useEffect } from 'react';
import { apiFetch } from '../services/api';
import { Sword, Zap, Eye, X, Plus, Pencil, Trash2, AlertTriangle, Flame } from 'lucide-react';

interface Troop {
  id: number;
  name: string;
  damage: number;
  damageType?: string;
}

interface Spell {
  id: number;
  name: string;
  type: string;
}

export default function ArsenalPage() {
  const [troops, setTroops] = useState<Troop[]>([]);
  const [spells, setSpells] = useState<Spell[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'troops' | 'spells'>('troops');

  // Modals for details
  const [selectedTroop, setSelectedTroop] = useState<Troop | null>(null);
  const [selectedSpell, setSelectedSpell] = useState<Spell | null>(null);

  // Form modals state
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editTroopMode, setEditTroopMode] = useState<Troop | null>(null);
  const [editSpellMode, setEditSpellMode] = useState<Spell | null>(null);
  
  // Delete modals state
  const [troopToDelete, setTroopToDelete] = useState<Troop | null>(null);
  const [spellToDelete, setSpellToDelete] = useState<Spell | null>(null);

  // Shared form inputs
  const [createName, setCreateName] = useState('');
  const [createDamage, setCreateDamage] = useState('10');
  const [createDamageType, setCreateDamageType] = useState('FOGO');
  const [createEffectType, setCreateEffectType] = useState('CURA');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const fetchArsenal = async () => {
    setIsLoading(true);
    try {
      const [troopsRes, spellsRes] = await Promise.all([
        apiFetch('/troops?size=1000'),
        apiFetch('/spells?size=1000')
      ]);

      if (troopsRes.ok) {
        const tData = await troopsRes.json();
        setTroops(tData.content || []);
      }
      
      if (spellsRes.ok) {
        const sData = await spellsRes.json();
        setSpells(sData.content || []);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchArsenal();
  }, []);

  const openCreateModal = () => {
    setEditTroopMode(null);
    setEditSpellMode(null);
    setCreateName('');
    setCreateDamage('10');
    setCreateDamageType('FOGO');
    setCreateEffectType('CURA');
    setError(null);
    setShowCreateModal(true);
  };

  const openEditTroop = (troop: Troop) => {
    setEditTroopMode(troop);
    setEditSpellMode(null);
    setCreateName(troop.name);
    setCreateDamage(troop.damage.toString());
    setCreateDamageType(troop.damageType || 'FOGO');
    setError(null);
    setShowCreateModal(true);
  };

  const openEditSpell = (spell: Spell) => {
    setEditSpellMode(spell);
    setEditTroopMode(null);
    setCreateName(spell.name);
    setCreateEffectType(spell.type);
    setError(null);
    setShowCreateModal(true);
  };

  const handleSaveItem = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!createName) return;

    setIsSubmitting(true);
    setError(null);

    const isTroop = activeTab === 'troops';
    const endpoint = isTroop ? '/troops' : '/spells';
    const payload = isTroop 
      ? { name: createName, damage: parseInt(createDamage) || 10, damageType: createDamageType }
      : { name: createName, type: createEffectType };

    try {
      let res;
      if (isTroop && editTroopMode) {
        res = await apiFetch(`/troops/${editTroopMode.id}`, {
          method: 'PUT',
          body: JSON.stringify(payload)
        });
      } else if (!isTroop && editSpellMode) {
        res = await apiFetch(`/spells/${editSpellMode.id}`, {
          method: 'PUT',
          body: JSON.stringify(payload)
        });
      } else {
        res = await apiFetch(endpoint, {
          method: 'POST',
          body: JSON.stringify(payload)
        });
      }

      if (!res.ok) {
        const errData = await res.json().catch(() => ({}));
        throw new Error(errData.message || 'Falha ao salvar o item.');
      }

      setShowCreateModal(false);
      fetchArsenal();
    } catch (err: any) {
      setError(err.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteTroop = async () => {
    if (!troopToDelete) return;
    setIsSubmitting(true);
    setDeleteError(null);
    try {
      const res = await apiFetch(`/troops/${troopToDelete.id}`, { method: 'DELETE' });
      if (!res.ok) {
        const errData = await res.json().catch(() => ({}));
        throw new Error(errData.message || 'Falha ao excluir tropa.');
      }
      setTroopToDelete(null);
      fetchArsenal();
    } catch (err: any) {
      setDeleteError(err.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteSpell = async () => {
    if (!spellToDelete) return;
    setIsSubmitting(true);
    setDeleteError(null);
    try {
      const res = await apiFetch(`/spells/${spellToDelete.id}`, { method: 'DELETE' });
      if (!res.ok) {
        const errData = await res.json().catch(() => ({}));
        throw new Error(errData.message || 'Falha ao excluir feitiço.');
      }
      setSpellToDelete(null);
      fetchArsenal();
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
          <h1 className="text-3xl font-bold text-white mb-2 tracking-tight">Arsenal</h1>
          <p className="text-zinc-400">Catálogo completo de tropas e feitiços disponíveis.</p>
        </div>
        <button
          onClick={openCreateModal}
          className="bg-purple-600 hover:bg-purple-700 text-white px-5 py-2.5 rounded-lg font-semibold flex items-center gap-2 transition-colors shadow-[0_0_15px_rgba(147,51,234,0.2)] hover:shadow-[0_0_20px_rgba(147,51,234,0.4)]"
        >
          <Plus size={20} />
          <span>{activeTab === 'troops' ? 'Nova Tropa' : 'Novo Feitiço'}</span>
        </button>
      </div>

      {/* Tabs */}
      <div className="flex gap-3 mb-8">
        <button
          onClick={() => setActiveTab('troops')}
          className={`flex items-center gap-2 px-6 py-3 rounded-lg font-semibold transition-colors duration-200 ${
            activeTab === 'troops'
              ? 'bg-purple-600 text-white shadow-[0_0_15px_rgba(147,51,234,0.3)]'
              : 'bg-zinc-950 text-zinc-400 hover:text-white hover:bg-zinc-900 border border-zinc-800'
          }`}
        >
          <Sword size={20} />
          <span>Tropas de Combate</span>
        </button>
        <button
          onClick={() => setActiveTab('spells')}
          className={`flex items-center gap-2 px-6 py-3 rounded-lg font-semibold transition-colors duration-200 ${
            activeTab === 'spells'
              ? 'bg-purple-600 text-white shadow-[0_0_15px_rgba(147,51,234,0.3)]'
              : 'bg-zinc-950 text-zinc-400 hover:text-white hover:bg-zinc-900 border border-zinc-800'
          }`}
        >
          <Zap size={20} />
          <span>Feitiços Mágicos</span>
        </button>
      </div>

      {/* Content Area */}
      <div className="bg-zinc-950 border border-zinc-900 rounded-xl overflow-hidden shadow-2xl p-8 min-h-[400px]">
        
        {isLoading ? (
          <div className="py-20 text-center text-zinc-500 font-medium">
            Carregando catálogo do arsenal...
          </div>
        ) : activeTab === 'troops' ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {troops.length === 0 ? (
              <div className="col-span-full py-16 text-center text-zinc-500 font-medium">
                Nenhuma tropa encontrada no catálogo.
              </div>
            ) : (
              troops.map(troop => (
                <div key={troop.id} className="bg-black border border-zinc-800 hover:border-purple-500 rounded-xl p-6 transition-colors group flex flex-col justify-between">
                  <div>
                    <div className="flex items-center justify-between mb-6">
                      <h3 className="font-bold text-xl text-white group-hover:text-purple-400 transition-colors">{troop.name}</h3>
                      <div className="bg-zinc-900 p-2.5 rounded-lg border border-zinc-800 flex items-center justify-center">
                        <Sword size={22} className="text-zinc-400 group-hover:text-purple-400 transition-colors" />
                      </div>
                    </div>
                    
                    <div className="flex items-center justify-between mb-4">
                      <span className="text-sm font-semibold text-zinc-500 uppercase tracking-wider">Tipo de Dano</span>
                      <span className="inline-flex max-w-fit px-3 py-1.5 rounded-md text-xs font-bold bg-zinc-900 text-white border border-zinc-800">
                        {troop.damageType || 'N/A'}
                      </span>
                    </div>

                    <div className="flex items-center justify-between mb-6">
                      <span className="text-sm font-semibold text-zinc-500 uppercase tracking-wider">Poder</span>
                      <span className="font-mono font-bold text-lg text-white">{troop.damage}</span>
                    </div>
                  </div>
                  <div className="flex items-center gap-2 mt-4">
                    <button 
                      onClick={() => setSelectedTroop(troop)}
                      className="flex-1 flex items-center justify-center gap-2 py-2 bg-zinc-900 hover:bg-purple-600 text-zinc-400 hover:text-white rounded-lg transition-colors border border-zinc-800 hover:border-purple-500"
                    >
                      <Eye size={18} />
                      <span className="font-semibold text-sm">Ver</span>
                    </button>
                    <button 
                      onClick={() => openEditTroop(troop)}
                      className="px-3 py-2 bg-zinc-900 hover:bg-blue-600/20 text-zinc-400 hover:text-blue-400 rounded-lg transition-colors border border-zinc-800 hover:border-blue-500/50"
                      title="Editar"
                    >
                      <Pencil size={18} />
                    </button>
                    <button 
                      onClick={() => setTroopToDelete(troop)}
                      className="px-3 py-2 bg-zinc-900 hover:bg-red-600/20 text-zinc-400 hover:text-red-400 rounded-lg transition-colors border border-zinc-800 hover:border-red-500/50"
                      title="Excluir"
                    >
                      <Trash2 size={18} />
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 animate-fade-in">
            {spells.length === 0 ? (
              <div className="col-span-full py-16 text-center text-zinc-500 font-medium">
                Nenhum feitiço encontrado no catálogo.
              </div>
            ) : (
              spells.map(spell => (
                <div key={spell.id} className="bg-black border border-zinc-800 hover:border-purple-500 rounded-xl p-6 transition-colors group flex flex-col justify-between">
                  <div>
                    <div className="flex items-center justify-between mb-6">
                      <h3 className="font-bold text-xl text-white group-hover:text-purple-400 transition-colors">{spell.name}</h3>
                      <div className="bg-zinc-900 p-2.5 rounded-lg border border-zinc-800">
                        <Zap size={22} className="text-zinc-400 group-hover:text-purple-400 transition-colors" />
                      </div>
                    </div>
                    <div className="flex flex-col gap-2 mb-6">
                      <span className="text-sm font-semibold text-zinc-500 uppercase tracking-wider">Tipo de Efeito</span>
                      <span className="inline-flex max-w-fit px-3 py-1.5 rounded-md text-xs font-bold bg-zinc-900 text-white border border-zinc-800">
                        {spell.type}
                      </span>
                    </div>
                  </div>
                  <div className="flex items-center gap-2 mt-4">
                    <button 
                      onClick={() => setSelectedSpell(spell)}
                      className="flex-1 flex items-center justify-center gap-2 py-2 bg-zinc-900 hover:bg-purple-600 text-zinc-400 hover:text-white rounded-lg transition-colors border border-zinc-800 hover:border-purple-500"
                    >
                      <Eye size={18} />
                      <span className="font-semibold text-sm">Ver</span>
                    </button>
                    <button 
                      onClick={() => openEditSpell(spell)}
                      className="px-3 py-2 bg-zinc-900 hover:bg-blue-600/20 text-zinc-400 hover:text-blue-400 rounded-lg transition-colors border border-zinc-800 hover:border-blue-500/50"
                      title="Editar"
                    >
                      <Pencil size={18} />
                    </button>
                    <button 
                      onClick={() => setSpellToDelete(spell)}
                      className="px-3 py-2 bg-zinc-900 hover:bg-red-600/20 text-zinc-400 hover:text-red-400 rounded-lg transition-colors border border-zinc-800 hover:border-red-500/50"
                      title="Excluir"
                    >
                      <Trash2 size={18} />
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        )}
      </div>

      {/* Modal Salvar/Editar Item */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="bg-zinc-950 border border-zinc-800 rounded-xl w-full max-w-lg shadow-2xl overflow-hidden animate-fade-in">
            <div className="flex justify-between items-center p-6 border-b border-zinc-900">
              <h2 className="text-xl font-bold text-white tracking-tight">
                {activeTab === 'troops' 
                  ? (editTroopMode ? 'Editar Tropa' : 'Criar Nova Tropa') 
                  : (editSpellMode ? 'Editar Feitiço' : 'Criar Novo Feitiço')}
              </h2>
              <button onClick={() => setShowCreateModal(false)} className="text-zinc-500 hover:text-white transition-colors">
                <X size={24} />
              </button>
            </div>
            
            <form onSubmit={handleSaveItem} className="p-6 space-y-5">
              <div>
                <label className="block text-xs font-semibold text-zinc-400 uppercase tracking-wider mb-2">
                  Nome {activeTab === 'troops' ? 'da Tropa' : 'do Feitiço'}
                </label>
                <input
                  type="text"
                  required
                  value={createName}
                  onChange={(e) => setCreateName(e.target.value)}
                  placeholder={activeTab === 'troops' ? 'Ex: P.E.K.K.A' : 'Ex: Feitiço de Fúria'}
                  className="w-full bg-black border border-zinc-800 rounded-lg px-4 py-3 text-white placeholder-zinc-600 focus:outline-none focus:border-purple-600 focus:ring-1 focus:ring-purple-600 transition-colors"
                />
              </div>
              
              {activeTab === 'troops' ? (
                <>
                  <div>
                    <label className="block text-xs font-semibold text-zinc-400 uppercase tracking-wider mb-2">Tipo de Dano</label>
                    <select
                      value={createDamageType}
                      onChange={(e) => setCreateDamageType(e.target.value)}
                      className="w-full bg-black border border-zinc-800 rounded-lg px-4 py-3 text-white focus:outline-none focus:border-purple-600 focus:ring-1 focus:ring-purple-600 transition-colors appearance-none"
                    >
                      <option value="FOGO">Fogo</option>
                      <option value="GELO">Gelo</option>
                      <option value="AR">Ar</option>
                      <option value="TERRA">Terra</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-zinc-400 uppercase tracking-wider mb-2">Poder de Dano</label>
                    <input
                      type="number"
                      min="1"
                      required
                      value={createDamage}
                      onChange={(e) => setCreateDamage(e.target.value)}
                      placeholder="Ex: 500"
                      className="w-full bg-black border border-zinc-800 rounded-lg px-4 py-3 text-white placeholder-zinc-600 focus:outline-none focus:border-purple-600 focus:ring-1 focus:ring-purple-600 transition-colors"
                    />
                  </div>
                </>
              ) : (
                <div>
                  <label className="block text-xs font-semibold text-zinc-400 uppercase tracking-wider mb-2">Tipo de Efeito</label>
                  <select
                    value={createEffectType}
                    onChange={(e) => setCreateEffectType(e.target.value)}
                    className="w-full bg-black border border-zinc-800 rounded-lg px-4 py-3 text-white focus:outline-none focus:border-purple-600 focus:ring-1 focus:ring-purple-600 transition-colors appearance-none"
                  >
                    <option value="CURA">Cura</option>
                    <option value="DANO">Dano</option>
                    <option value="SUPORTE">Suporte</option>
                    <option value="ILUSAO">Ilusão</option>
                  </select>
                </div>
              )}

              {error && (
                <div className="p-3 rounded-lg bg-red-950/50 border border-red-900/50 text-red-400 text-sm flex items-start gap-2">
                  <span className="font-bold">Atenção:</span> {error}
                </div>
              )}

              <div className="flex gap-3 pt-4 mt-2">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="flex-1 px-4 py-3 rounded-lg text-zinc-400 hover:text-white hover:bg-zinc-900 transition-colors font-semibold border border-transparent hover:border-zinc-800"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="flex-1 px-4 py-3 rounded-lg bg-purple-600 hover:bg-purple-700 text-white transition-colors font-semibold disabled:opacity-50 shadow-[0_0_15px_rgba(147,51,234,0.2)] hover:shadow-[0_0_20px_rgba(147,51,234,0.4)]"
                >
                  {isSubmitting ? 'Salvando...' : 'Salvar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Confirmação Exclusão - Tropa */}
      {troopToDelete && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="bg-zinc-950 border border-red-500/30 rounded-xl w-full max-w-md shadow-[0_0_40px_rgba(239,68,68,0.15)] overflow-hidden animate-fade-in">
            <div className="p-6 text-center">
              <div className="w-16 h-16 bg-red-500/10 rounded-full flex items-center justify-center mx-auto mb-4 border border-red-500/20">
                <AlertTriangle className="text-red-500" size={32} />
              </div>
              <h2 className="text-xl font-bold text-white mb-2">Excluir Tropa</h2>
              <p className="text-zinc-400 text-sm mb-6">
                Tem certeza que deseja excluir a tropa <span className="font-bold text-white">{troopToDelete.name}</span> permanentemente do arsenal?
              </p>
              {deleteError && (
                <div className="mb-6 p-3 rounded-lg bg-red-950/50 border border-red-900/50 text-red-400 text-sm text-left">
                  <span className="font-bold">Atenção:</span> {deleteError}
                </div>
              )}
              <div className="flex gap-3">
                <button
                  onClick={() => setTroopToDelete(null)}
                  disabled={isSubmitting}
                  className="flex-1 px-4 py-2.5 rounded-lg text-zinc-300 bg-zinc-900 hover:bg-zinc-800 transition-colors font-semibold border border-zinc-800 hover:border-zinc-700"
                >
                  Cancelar
                </button>
                <button
                  onClick={handleDeleteTroop}
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

      {/* Modal Confirmação Exclusão - Feitiço */}
      {spellToDelete && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="bg-zinc-950 border border-red-500/30 rounded-xl w-full max-w-md shadow-[0_0_40px_rgba(239,68,68,0.15)] overflow-hidden animate-fade-in">
            <div className="p-6 text-center">
              <div className="w-16 h-16 bg-red-500/10 rounded-full flex items-center justify-center mx-auto mb-4 border border-red-500/20">
                <AlertTriangle className="text-red-500" size={32} />
              </div>
              <h2 className="text-xl font-bold text-white mb-2">Excluir Feitiço</h2>
              <p className="text-zinc-400 text-sm mb-6">
                Tem certeza que deseja excluir o feitiço <span className="font-bold text-white">{spellToDelete.name}</span> permanentemente do arsenal?
              </p>
              {deleteError && (
                <div className="mb-6 p-3 rounded-lg bg-red-950/50 border border-red-900/50 text-red-400 text-sm text-left">
                  <span className="font-bold">Atenção:</span> {deleteError}
                </div>
              )}
              <div className="flex gap-3">
                <button
                  onClick={() => setSpellToDelete(null)}
                  disabled={isSubmitting}
                  className="flex-1 px-4 py-2.5 rounded-lg text-zinc-300 bg-zinc-900 hover:bg-zinc-800 transition-colors font-semibold border border-zinc-800 hover:border-zinc-700"
                >
                  Cancelar
                </button>
                <button
                  onClick={handleDeleteSpell}
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

      {/* Modal Ver Detalhes da Tropa */}
      {selectedTroop && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="bg-zinc-950 border border-purple-500/30 rounded-xl w-full max-w-sm shadow-[0_0_40px_rgba(147,51,234,0.15)] overflow-hidden animate-fade-in">
            <div className="bg-black border-b border-zinc-900 p-6 flex justify-between items-start">
              <div className="flex gap-4 items-center">
                <div className="bg-purple-600/20 p-3 rounded-xl border border-purple-500/30">
                  <Sword className="text-purple-500" size={24} />
                </div>
                <div>
                  <h2 className="text-2xl font-bold text-white tracking-tight">{selectedTroop.name}</h2>
                  <p className="text-zinc-500 text-xs font-mono mt-1">ID da Tropa: #{selectedTroop.id}</p>
                </div>
              </div>
              <button onClick={() => setSelectedTroop(null)} className="text-zinc-500 hover:text-white transition-colors bg-zinc-900 p-1.5 rounded-lg border border-zinc-800 hover:border-zinc-600">
                <X size={20} />
              </button>
            </div>
            
            <div className="p-6 space-y-4">
              <div className="bg-black p-5 rounded-xl border border-zinc-800 text-center flex flex-col justify-center items-center">
                <div className="flex items-center justify-center gap-2 mb-2 text-zinc-400">
                  <Flame size={20} className={selectedTroop.damageType === 'FOGO' ? 'text-orange-500' : selectedTroop.damageType === 'GELO' ? 'text-blue-400' : selectedTroop.damageType === 'TERRA' ? 'text-emerald-500' : 'text-zinc-400'} />
                  <span className="text-sm font-semibold uppercase tracking-wider">Tipo de Dano</span>
                </div>
                <span className="text-xl font-bold text-white mb-1">
                  {selectedTroop.damageType || 'N/A'}
                </span>
              </div>
              
              <div className="bg-black p-5 rounded-xl border border-zinc-800 text-center">
                <div className="flex items-center justify-center gap-2 mb-3 text-zinc-400">
                  <Sword size={20} />
                  <span className="text-sm font-semibold uppercase tracking-wider">Poder de Dano Base</span>
                </div>
                <span className="text-5xl font-black text-transparent bg-clip-text bg-gradient-to-r from-purple-400 to-purple-600">
                  {selectedTroop.damage}
                </span>
                <p className="text-zinc-500 text-xs mt-4">Unidade de combate corpo-a-corpo ou à distância configurada para batalhas.</p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal Ver Detalhes do Feitiço */}
      {selectedSpell && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="bg-zinc-950 border border-purple-500/30 rounded-xl w-full max-w-sm shadow-[0_0_40px_rgba(147,51,234,0.15)] overflow-hidden animate-fade-in">
            <div className="bg-black border-b border-zinc-900 p-6 flex justify-between items-start">
              <div className="flex gap-4 items-center">
                <div className="bg-purple-600/20 p-3 rounded-xl border border-purple-500/30">
                  <Zap className="text-purple-500" size={24} />
                </div>
                <div>
                  <h2 className="text-2xl font-bold text-white tracking-tight">{selectedSpell.name}</h2>
                  <p className="text-zinc-500 text-xs font-mono mt-1">ID do Feitiço: #{selectedSpell.id}</p>
                </div>
              </div>
              <button onClick={() => setSelectedSpell(null)} className="text-zinc-500 hover:text-white transition-colors bg-zinc-900 p-1.5 rounded-lg border border-zinc-800 hover:border-zinc-600">
                <X size={20} />
              </button>
            </div>
            
            <div className="p-6">
              <div className="bg-black p-6 rounded-xl border border-zinc-800 text-center">
                <div className="flex items-center justify-center gap-2 mb-4 text-zinc-400">
                  <Zap size={20} />
                  <span className="text-sm font-semibold uppercase tracking-wider">Mecânica do Feitiço</span>
                </div>
                <div className="inline-flex max-w-fit px-5 py-2.5 rounded-lg text-lg font-bold bg-zinc-900 text-purple-400 border border-purple-500/30">
                  {selectedSpell.type}
                </div>
                <p className="text-zinc-500 text-xs mt-5">Poção mágica fabricada para alterar o curso do campo de batalha com efeitos de área únicos.</p>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
