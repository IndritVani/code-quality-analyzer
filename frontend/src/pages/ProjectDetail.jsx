import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { analyzeProject, getMiWeights, getProject, getRun, getRunFiles, getRuns } from '../api/client.js';
import MetricCard from '../components/MetricCard.jsx';
import ScoreBreakdown from '../components/ScoreBreakdown.jsx';
import TierBadge from '../components/TierBadge.jsx';
import { fmt, pct, scoreClass } from '../lib/metrics.js';

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
const describe = (e) => e?.response?.data?.message || e?.message || 'Request failed';

export default function ProjectDetail() {
  const { id } = useParams();
  const [project, setProject] = useState(null);
  const [run, setRun] = useState(null);
  const [files, setFiles] = useState([]);
  const [weights, setWeights] = useState(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  async function load() {
    setLoading(true);
    try {
      const [proj, runs, miWeights] = await Promise.all([getProject(id), getRuns(id), getMiWeights()]);
      setProject(proj);
      setWeights(miWeights);
      const latest = runs[0] || null;
      setRun(latest);
      if (latest && latest.status === 'complete') {
        setFiles(await getRunFiles(latest.id));
      } else {
        setFiles([]);
      }
      setError(null);
    } catch (e) {
      setError(describe(e));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  async function handleAnalyze() {
    setBusy(true);
    setError(null);
    try {
      const started = await analyzeProject(id);
      for (let i = 0; i < 120; i++) {
        const r = await getRun(started.id);
        if (r.status === 'complete' || r.status === 'failed') break;
        await sleep(1500);
      }
      await load();
    } catch (e) {
      setError(describe(e));
    } finally {
      setBusy(false);
    }
  }

  if (loading) return <p className="muted">Loading…</p>;
  if (error && !project) return <p className="error">{error}</p>;
  if (!project) return <p className="muted">Project not found. <Link to="/">Back to overview</Link></p>;

  const m = run && run.status === 'complete' ? run.metrics : null;
  const sortedFiles = [...files].sort((a, b) => b.loc - a.loc);

  return (
    <div>
      <p><Link to="/">← All projects</Link></p>

      <section className="panel">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '1rem' }}>
          <div>
            <h2 style={{ marginBottom: '0.35rem' }}>{project.name} <TierBadge tier={project.tier} /></h2>
            {project.description && <p className="muted" style={{ margin: '0.25rem 0' }}>{project.description}</p>}
            <p className="muted" style={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>{project.path}</p>
          </div>
          <button className="btn" onClick={handleAnalyze} disabled={busy}>
            {busy ? 'Analyzing…' : 'Run analysis'}
          </button>
        </div>
        {error && <p className="error">{error}</p>}
        {run && (
          <p style={{ marginBottom: 0 }}>
            Latest run: <span className={`status ${run.status}`}>{run.status}</span>
            {run.completedAt && <span className="muted"> · {new Date(run.completedAt).toLocaleString()}</span>}
            {run.status === 'failed' && run.errorMessage && <span className="error"> · {run.errorMessage}</span>}
          </p>
        )}
      </section>

      {!m ? (
        <section className="panel">
          <p className="muted">No completed analysis yet. Click “Run analysis” to compute metrics.</p>
        </section>
      ) : (
        <>
          <section className="panel" style={{ display: 'flex', gap: '2rem', alignItems: 'center' }}>
            <div style={{ textAlign: 'center' }}>
              <div className="label muted">Maintainability Index</div>
              <div className={`score-badge ${scoreClass(m.maintainabilityIndex)}`}>
                {fmt(m.maintainabilityIndex, 0)}
              </div>
              <div className="muted" style={{ fontSize: '0.75rem' }}>out of 100</div>
            </div>
            <div style={{ flex: 1 }}>
              <h3 style={{ marginTop: 0, fontSize: '0.95rem' }}>Score breakdown</h3>
              <ScoreBreakdown metrics={m} weights={weights} />
            </div>
          </section>

          <section className="panel">
            <h3 style={{ marginTop: 0 }}>Metrics</h3>
            <div className="metric-grid">
              <MetricCard label="Total LOC" value={m.totalLoc.toLocaleString()} />
              <MetricCard label="Avg Cyclomatic" value={fmt(m.avgCyclomatic, 2)} hint="per method" />
              <MetricCard label="Max Cyclomatic" value={fmt(m.maxCyclomatic, 0)} />
              <MetricCard label="Duplication" value={pct(m.duplicationRatio)} />
              <MetricCard label="Avg Coupling" value={fmt(m.avgCoupling, 2)} hint="external refs / file" />
              <MetricCard label="Comment Density" value={pct(m.commentDensity)} />
              <MetricCard label="Avg Volume" value={fmt(m.avgVolume, 0)} hint="Halstead, per file" />
              <MetricCard label="Outdated Deps" value={m.outdatedDependencies} />
            </div>
          </section>

          <section className="panel">
            <h3 style={{ marginTop: 0 }}>Files ({sortedFiles.length})</h3>
            <table className="file-table">
              <thead>
                <tr>
                  <th>File</th>
                  <th>LOC</th>
                  <th>Avg CC</th>
                  <th>Max CC</th>
                  <th>Coupling</th>
                  <th>Comments</th>
                </tr>
              </thead>
              <tbody>
                {sortedFiles.map((f) => (
                  <tr key={f.filePath}>
                    <td>{f.filePath}</td>
                    <td>{f.loc}</td>
                    <td>{fmt(f.avgCyclomatic, 2)}</td>
                    <td>{fmt(f.maxCyclomatic, 0)}</td>
                    <td>{f.coupling}</td>
                    <td>{pct(f.commentDensity)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </section>
        </>
      )}
    </div>
  );
}
