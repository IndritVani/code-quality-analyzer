import { NavLink, Route, Routes } from 'react-router-dom';
import Overview from './pages/Overview.jsx';
import ProjectDetail from './pages/ProjectDetail.jsx';
import Compare from './pages/Compare.jsx';

export default function App() {
  return (
    <div className="app">
      <header className="app-header">
        <h1>Code Quality Analyzer</h1>
        <nav>
          <NavLink to="/" end>Overview</NavLink>
          <NavLink to="/compare">Compare</NavLink>
        </nav>
      </header>
      <main className="app-main">
        <Routes>
          <Route path="/" element={<Overview />} />
          <Route path="/projects/:id" element={<ProjectDetail />} />
          <Route path="/compare" element={<Compare />} />
        </Routes>
      </main>
    </div>
  );
}
