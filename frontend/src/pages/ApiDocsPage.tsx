import { Terminal, Key, Users, Castle, Sword, AlertTriangle, Flag } from 'lucide-react';

export default function ApiDocsPage() {
  const sections = [
    {
      id: 'auth',
      title: 'Autenticação & Acesso',
      icon: <Key className="text-purple-500" size={24} />,
      endpoint: '/api-keys',
      method: 'POST',
      description: 'Gera um token de acesso. O token deve ser enviado no cabeçalho X-API-Key em todas as requisições subsequentes.',
      body: '{ "owner": "string" }'
    },
    {
      id: 'clans',
      title: 'Gestão de Clãs',
      icon: <Flag className="text-purple-500" size={24} />,
      endpoint: '/clans',
      method: 'GET | POST | PUT | DELETE',
      description: 'Criação e gerenciamento da aliança principal. Ao listar clãs (GET), envie o header X-API-Version: 2 para que a API retorne a contagem de totalMembers agrupados por clã.',
      body: '{ "name": "string", "description": "string", "requiredTrophies": number }',
      alerts: [
        'Idempotência no POST: Envie o UUID no cabeçalho X-Idempotency-Key para evitar duplicidade de clãs em erros de timeout.'
      ]
    },
    {
      id: 'players',
      title: 'Gerenciamento de Jogadores',
      icon: <Users className="text-purple-500" size={24} />,
      endpoint: '/players',
      method: 'GET | POST | PUT | DELETE',
      description: 'Administração dos usuários do ecossistema. Permite definir nível, nickname e o cargo no clã (MEMBRO, ANCIAO, CO_LIDER, LIDER).',
      body: '{ "nickname": "string", "level": number, "role": "string" }'
    },
    {
      id: 'villages',
      title: 'Centro de Vilas',
      icon: <Castle className="text-purple-500" size={24} />,
      endpoint: '/villages',
      method: 'GET | POST | PUT | DELETE',
      description: 'Controle das bases. Cada vila tem um relacionamento 1:1 com um jogador.',
      body: '{ "name": "string", "townHallLevel": number, "player": { "id": number } }',
      alerts: [
        'Regra de Negócio: Um jogador só pode ter UMA vila (Erro 400 se violada).',
        'Idempotência: Envie um UUID no header X-Idempotency-Key no POST para evitar duplicação em falhas de rede (Erro 409 se repetida).'
      ]
    },
    {
      id: 'arsenal',
      title: 'Catálogo do Arsenal (Tropas e Feitiços)',
      icon: <Sword className="text-purple-500" size={24} />,
      endpoint: '/troops | /spells',
      method: 'GET | POST | PUT | DELETE',
      description: 'Catálogo independente para consultar e cadastrar tropas e feitiços.',
      body: 'Tropas: { "name": "string", "damage": number }\nFeitiços: { "name": "string", "effectType": "string" }'
    }
  ];

  return (
    <div className="animate-fade-in">
      <div className="mb-10 flex items-center gap-4">
        <div className="bg-purple-600 p-3 rounded-xl shadow-[0_0_20px_rgba(147,51,234,0.4)]">
          <Terminal size={28} className="text-white" />
        </div>
        <div>
          <h1 className="text-3xl font-bold text-white tracking-tight">Documentação da API</h1>
          <p className="text-zinc-400 mt-1">Referência rápida dos endpoints, contratos e regras de negócio do backend.</p>
        </div>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-8">
        {sections.map((section) => (
          <div key={section.id} className="bg-zinc-950 border border-zinc-900 rounded-2xl overflow-hidden hover:border-purple-500/50 transition-colors duration-300">
            <div className="p-6 border-b border-zinc-900 flex items-center justify-between bg-black">
              <div className="flex items-center gap-3">
                <div className="bg-zinc-900 p-2.5 rounded-lg border border-zinc-800">
                  {section.icon}
                </div>
                <h2 className="text-xl font-bold text-white tracking-tight">{section.title}</h2>
              </div>
            </div>
            
            <div className="p-6 space-y-6">
              <div>
                <h3 className="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-2">Endpoint & Métodos</h3>
                <div className="flex flex-wrap items-center gap-3">
                  <span className="bg-purple-600 text-white font-mono text-xs font-bold px-3 py-1.5 rounded-md shadow-[0_0_10px_rgba(147,51,234,0.3)]">
                    {section.method}
                  </span>
                  <span className="font-mono text-zinc-300 bg-black px-3 py-1.5 rounded-md border border-zinc-800">
                    {section.endpoint}
                  </span>
                </div>
              </div>

              <div>
                <h3 className="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-2">Descrição</h3>
                <p className="text-zinc-300 leading-relaxed text-sm">
                  {section.description}
                </p>
              </div>

              <div>
                <h3 className="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-2">Payload (JSON)</h3>
                <pre className="bg-black border border-zinc-800 rounded-lg p-4 text-xs font-mono text-purple-300 overflow-x-auto">
                  {section.body}
                </pre>
              </div>

              {section.alerts && (
                <div className="bg-black border border-purple-500/30 rounded-lg p-4 space-y-3">
                  <div className="flex items-center gap-2 text-purple-400 mb-1">
                    <AlertTriangle size={16} />
                    <span className="font-bold text-sm uppercase tracking-wider">Regras Críticas</span>
                  </div>
                  <ul className="space-y-2">
                    {section.alerts.map((alert, idx) => (
                      <li key={idx} className="flex gap-2 text-sm text-zinc-300">
                        <span className="text-purple-500 mt-0.5">•</span>
                        {alert}
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
