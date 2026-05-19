import { Outlet, NavLink } from 'react-router-dom';
import { Users, Castle, Sword, Shield, LogOut, BookOpen, Flag, Settings2, Save } from 'lucide-react';
import { useState, useEffect } from 'react';

function DashboardLayout() {
  const [apiUrl, setApiUrl] = useState('');
  const [apiKey, setApiKey] = useState('');
  const [isSaved, setIsSaved] = useState(false);

  useEffect(() => {
    setApiUrl(localStorage.getItem('clash_api_url') || 'http://localhost:8080');
    setApiKey(localStorage.getItem('clash_api_key') || '');
  }, []);

  const menuItems = [
    { path: '/dashboard/clans', label: 'Alianças (Clãs)', icon: <Flag size={20} /> },
    { path: '/dashboard/players', label: 'Jogadores', icon: <Users size={20} /> },
    { path: '/dashboard/villages', label: 'Centro de Vilas', icon: <Castle size={20} /> },
    { path: '/dashboard/arsenal', label: 'Arsenal', icon: <Sword size={20} /> },
    { path: '/dashboard/docs', label: 'Resumo da API', icon: <BookOpen size={20} /> },
  ];

  const handleLogout = () => {
    localStorage.removeItem('clash_api_key');
    localStorage.removeItem('clash_api_url');
    window.location.href = '/';
  };

  const handleSaveSettings = () => {
    localStorage.setItem('clash_api_url', apiUrl);
    localStorage.setItem('clash_api_key', apiKey);
    setIsSaved(true);
    setTimeout(() => setIsSaved(false), 3000);
    // Reload the page to ensure all components use the new apiFetch state properly
    window.location.reload();
  };

  return (
    <div className="flex h-screen bg-black text-white font-sans overflow-hidden selection:bg-purple-600 selection:text-white">
      
      {/* Sidebar */}
      <aside className="w-64 bg-zinc-950 border-r border-zinc-900 flex flex-col shrink-0">
        <div className="p-6 border-b border-zinc-900">
          <div className="flex items-center gap-3">
            <div className="bg-purple-600 p-2 rounded-lg shadow-[0_0_15px_rgba(147,51,234,0.3)]">
              <Shield className="text-white" size={24} />
            </div>
            <h2 className="font-bold text-xl tracking-tight text-white">
              Clash Manager
            </h2>
          </div>
        </div>

        <nav className="flex-1 py-6 px-4 space-y-1">
          {menuItems.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `flex items-center gap-3 px-4 py-3 rounded-lg transition-colors duration-200 ${
                  isActive
                    ? 'bg-purple-600 text-white'
                    : 'text-zinc-400 hover:bg-zinc-900 hover:text-white'
                }`
              }
            >
              {item.icon}
              <span className="font-medium">{item.label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="p-4 border-t border-zinc-900">
          <button 
            onClick={handleLogout}
            className="flex items-center gap-3 w-full px-4 py-3 text-zinc-400 hover:text-red-400 hover:bg-zinc-900 rounded-lg transition-colors duration-200"
          >
            <LogOut size={20} />
            <span className="font-medium">Sair da Sessão</span>
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="flex-1 flex flex-col overflow-hidden bg-black">
        
        {/* Settings Topbar */}
        <header className="bg-zinc-950 border-b border-zinc-900 px-8 py-4 flex items-center justify-between shrink-0">
          <div className="flex items-center gap-3 text-zinc-400">
            <Settings2 size={20} />
            <span className="font-semibold text-sm uppercase tracking-wider">Configurações de Conexão</span>
          </div>
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2">
              <label className="text-xs font-semibold text-zinc-500 uppercase tracking-wider">URL da API</label>
              <input
                type="text"
                value={apiUrl}
                onChange={(e) => setApiUrl(e.target.value)}
                placeholder="http://localhost:8080"
                className="bg-black border border-zinc-800 rounded-lg px-3 py-1.5 text-sm text-white w-56 focus:outline-none focus:border-purple-600 focus:ring-1 focus:ring-purple-600 transition-colors"
              />
            </div>
            <div className="flex items-center gap-2">
              <label className="text-xs font-semibold text-zinc-500 uppercase tracking-wider">Chave de Acesso</label>
              <input
                type="password"
                value={apiKey}
                onChange={(e) => setApiKey(e.target.value)}
                placeholder="Token de Autenticação"
                className="bg-black border border-zinc-800 rounded-lg px-3 py-1.5 text-sm text-white w-64 focus:outline-none focus:border-purple-600 focus:ring-1 focus:ring-purple-600 transition-colors"
              />
            </div>
            <button
              onClick={handleSaveSettings}
              className={`flex items-center gap-2 px-4 py-1.5 rounded-lg text-sm font-semibold transition-colors ${
                isSaved 
                  ? 'bg-emerald-600 text-white' 
                  : 'bg-purple-600 hover:bg-purple-700 text-white shadow-[0_0_10px_rgba(147,51,234,0.2)]'
              }`}
            >
              <Save size={16} />
              {isSaved ? 'Salvo!' : 'Aplicar'}
            </button>
          </div>
        </header>

        <div className="flex-1 overflow-y-auto p-8">
          <div className="max-w-6xl mx-auto">
            <Outlet />
          </div>
        </div>
      </main>

    </div>
  );
}

export default DashboardLayout;
