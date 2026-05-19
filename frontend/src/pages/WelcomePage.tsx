import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Shield, Copy, Check } from 'lucide-react';

function WelcomePage() {
  const [apiKey, setApiKey] = useState<string | null>(null);
  const [owner, setOwner] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);
  const navigate = useNavigate();

  const handleGenerateKey = async () => {
    if (!owner.trim()) {
      setError('Por favor, informe o nome do proprietário (owner) antes de gerar a chave.');
      return;
    }

    setIsLoading(true);
    setError(null);
    setSuccessMsg(null);
    setCopied(false);

    try {
      const response = await fetch(`http://localhost:8080/api-keys?owner=${encodeURIComponent(owner)}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error('Falha ao gerar a chave de acesso. Verifique se o backend está rodando.');
      }

      const data = await response.json();
      const generatedKey = data.key || data.apiKey || data.token || JSON.stringify(data);
      
      localStorage.setItem('clash_api_key', generatedKey);
      setApiKey(generatedKey);
      setSuccessMsg('Sua chave foi gerada com sucesso! Copie-a para acessar o sistema.');
      
    } catch (err: any) {
      setError(err.message || 'Ocorreu um erro desconhecido.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleCopyAndEnter = () => {
    if (apiKey) {
      navigator.clipboard.writeText(apiKey);
      setCopied(true);
      setTimeout(() => {
        navigate('/dashboard/players');
      }, 500);
    }
  };

  return (
    <div className="min-h-screen bg-black flex flex-col items-center justify-center p-6 font-sans selection:bg-purple-600 selection:text-white">
      
      <div className="max-w-md w-full bg-zinc-950 p-10 rounded-xl border border-zinc-800 text-center shadow-2xl">
        <div className="mb-10">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-purple-600 mb-6 shadow-[0_0_20px_rgba(147,51,234,0.3)]">
            <Shield className="h-8 w-8 text-white" />
          </div>
          <h1 className="text-3xl font-bold text-white tracking-tight">
            Clash API Manager
          </h1>
          <p className="text-zinc-400 mt-3 text-sm">
            Gere sua chave de acesso para conectar-se ao sistema.
          </p>
        </div>

        {!apiKey ? (
          <>
            <div className="mb-6 text-left">
              <label className="block text-xs font-semibold text-zinc-400 uppercase tracking-wider mb-2">
                Proprietário (Owner)
              </label>
              <input
                type="text"
                value={owner}
                onChange={(e) => setOwner(e.target.value)}
                placeholder="Digite seu nome"
                className="w-full bg-black border border-zinc-800 rounded-lg px-4 py-3 text-white placeholder-zinc-600 focus:outline-none focus:border-purple-600 focus:ring-1 focus:ring-purple-600 transition-colors"
              />
            </div>

            <button
              onClick={handleGenerateKey}
              disabled={isLoading}
              className="w-full bg-purple-600 hover:bg-purple-700 text-white font-semibold py-3 px-4 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed shadow-[0_0_15px_rgba(147,51,234,0.2)] hover:shadow-[0_0_25px_rgba(147,51,234,0.4)]"
            >
              {isLoading ? 'Gerando...' : 'Gerar Chave de Acesso'}
            </button>
          </>
        ) : (
          <div className="animate-fade-in flex flex-col gap-4">
            <div className="p-4 rounded-lg bg-purple-950/30 border border-purple-500/30 text-left">
              <span className="font-semibold text-white text-sm">{successMsg}</span>
              <div className="mt-3 bg-black p-3 rounded border border-purple-500/20 font-mono text-xs break-all text-purple-300 relative group select-all">
                {apiKey}
              </div>
            </div>

            <button
              onClick={handleCopyAndEnter}
              className={`w-full font-semibold py-3 px-4 rounded-lg transition-colors flex items-center justify-center gap-2 ${
                copied 
                  ? 'bg-emerald-600 text-white hover:bg-emerald-700' 
                  : 'bg-purple-600 text-white hover:bg-purple-700 shadow-[0_0_15px_rgba(147,51,234,0.2)] hover:shadow-[0_0_25px_rgba(147,51,234,0.4)]'
              }`}
            >
              {copied ? (
                <>
                  <Check size={20} />
                  Copiado! Entrando...
                </>
              ) : (
                <>
                  <Copy size={20} />
                  Copiar Chave e Acessar Painel
                </>
              )}
            </button>
          </div>
        )}

        {error && (
          <div className="mt-6 p-4 rounded-lg bg-red-950/50 border border-red-900/50 text-red-400 text-sm text-left animate-fade-in">
            {error}
          </div>
        )}
      </div>

    </div>
  );
}

export default WelcomePage;
