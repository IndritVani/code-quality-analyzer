import { useEffect, useState } from 'react';
import {
  analyzeProject,
  compareProjects,
  createProject,
  deleteProject,
  getRun,
  listProjects,
} from '../api/client.js';
import ProjectTable from '../components/ProjectTable.jsx';

const BLANK = { name: '', description: '', path: '', tier: 'average' };
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
const describe = (e) => e?.response?.data?.message || e?.message || 'Request failed';

export default function Overview() {
  const [projects, setProjects] = useState([]);
  const [statusById, setStatusById] = useState({});
  const [busyId, setBusyId] = useState(null);
  const [form, setForm] = useState(BLANK);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  async function load() {
    setLoading(true);
    try {
      const list = await listProjects();
      const miById = {};
      if (list.length) {
        const cmp = await compareProjects(list.map((p) => p.id));
        cmp.projects.forEach((e) => {
          if (e.metrics) miById[e.projectId] = e.metrics.maintainabilityIndex;
        });
      }
      setProjects(list.map((p) => ({ ...p, mi: miById[p.id] ?? null })));
      setError(null);
    } catch (e) {
      setError(describe(e));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function pollRun(runId) {
    for (let i = 0; i < 120; i++) {
      const run = await getRun(runId);
      if (run.status === 'complete' || run.status === 'failed') return run;
      await sleep(1500);
    }
    throw new Error('Analysis timed out');
  }

  async function handleAnalyze(id) {
    setBusyId(id);
    setError(null);
    setStatusById((s) => ({ ...s, [id]: 'running' }));
    try {
      const run = await analyzeProject(id);
      const final = await pollRun(run.id);
      setStatusById((s) => ({ ...s, [id]: final.status }));
      if (final.status === 'failed') setError(`Analysis failed: ${final.errorMessage || 'unknown error'}`);
      await load();
    } catch (e) {
      setStatusById((s) => ({ ...s, [id]: 'failed' }));
      setError(describe(e));
    } finally {
      setBusyId(null);
    }
  }

  async function handleDelete(id) {
    if (!window.confirm('Delete this project and its analysis history?')) return;
    try {
      await deleteProject(id);
      await load();
    } catch (e) {
      setError(describe(e));
    }
  }

  async function handleCreate(e) {
    e.preventDefault();
    setError(null);
    try {
      await createProject(form);
      setForm(BLANK);
      await load();
    } catch (e) {
      setError(describe(e));
    }
  }

  const rows = projects.map((p) => ({ ...p, status: statusById[p.id] ?? null }));

  return (
    <div>
      <section className="panel">
        <h2>Register a project</h2>
        <form className="add-project" onSubmit={handleCreate}>
          <div>
            <label>Name</label>
            <input
              required
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              placeholder="spring-petclinic"
            />
          </div>
          <div>
            <label>Tier</label>
            <select value={form.tier} onChange={(e) => setForm({ ...form, tier: e.target.value })}>
              <option value="well_maintained">Well maintained</option>
              <option value="average">Average</option>
              <option value="neglected">Neglected</option>
            </select>
          </div>
          <div className="full">
            <label>Local repository path (absolute)</label>
            <input
              required
              value={form.path}
              onChange={(e) => setForm({ ...form, path: e.target.value })}
              placeholder="C:/Users/you/repos/spring-petclinic"
            />
          </div>
          <div className="full">
            <label>Description</label>
            <input
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
              placeholder="Optional"
            />
          </div>
          <div className="full">
            <button className="btn" type="submit">Add project</button>
          </div>
        </form>
      </section>

      <section className="panel">
        <h2>Projects</h2>
        {error && <p className="error">{error}</p>}
        {loading ? (
          <p className="muted">Loading…</p>
        ) : (
          <ProjectTable rows={rows} busyId={busyId} onAnalyze={handleAnalyze} onDelete={handleDelete} />
        )}
      </section>
    </div>
  );
}
