import { Routes, Route } from 'react-router-dom';
import WelcomePage from './pages/WelcomePage';
import DashboardLayout from './layouts/DashboardLayout';
import PlayersPage from './pages/PlayersPage';
import VillagesPage from './pages/VillagesPage';
import ArsenalPage from './pages/ArsenalPage';
import ApiDocsPage from './pages/ApiDocsPage';
import ClansPage from './pages/ClansPage';

function App() {
  return (
    <Routes>
      <Route path="/" element={<WelcomePage />} />
      <Route path="/dashboard" element={<DashboardLayout />}>
        <Route path="clans" element={<ClansPage />} />
        <Route path="players" element={<PlayersPage />} />
        <Route path="villages" element={<VillagesPage />} />
        <Route path="arsenal" element={<ArsenalPage />} />
        <Route path="docs" element={<ApiDocsPage />} />
      </Route>
    </Routes>
  );
}

export default App;
