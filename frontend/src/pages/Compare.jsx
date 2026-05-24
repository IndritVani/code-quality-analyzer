import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { compareProjects, listProjects } from '../api/client.js';
import MetricBarChart from '../components/MetricBarChart.jsx';
import TierBadge from '../components/TierBadge.jsx';
import { fmt, pct } from '../lib/metrics.js';

const describe = (e) => e?.response?.data?.message || e?.message || 'Request failed';

const ROWS = [
  { label: 'Maintainability Index', get: (m) => fmt(m.maintainabilityIndex, 1) },
  { label: 'Total LOC', get: (m) => m.totalLoc.toLocaleString() },
  { label: 'Avg Cyclomatic', get: (m) => fmt(m.avgCyclomatic, 2) },
  { label: 'Max Cyclomatic', get: (m) => fmt(m.maxCyclomatic, 0) },
  { label: 'Duplication', get: (m) => pct(m.duplicationRatio) },
  { label: 'Avg Coupling', get: (m) => fmt(m.avgCoupling, 2) },
  { label: 'Comment Density', get: (m) => pct(m.commentDensity) },
  { label: 'Outdated Deps', get: (m) => m.outdatedDependencies },
];

export default function Compare() {
  const [projects, setProjects] = useState([]);
  const [selected, setSelected] = useState([]);
  const [entries, setEntries] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    listProjects()
      .then(setProjects)
      .catch((e) => setError(describe(e)));
  }, []);

  useEffect(() => {
    if (selected.length === 0) {
      setEntries([]);
      return;
    }
    compareProjects(selected)
      .then((r) => setEntries(r.projects))
      .catch((e) => setError(describe(e)));
  }, [selected]);

  function toggle(id) {
    setSelected((s) => (s.includes(id) ? s.filter((x) => x !== id) : [...s, id]));
  }

  const withMetrics = entries.filter((e) => e.metrics);
  const chartData = useMemo(
    () =>
      withMetrics.map((e) => ({
        name: e.name,
        mi: Number(e.metrics.maintainabilityIndex.toFixed(1)),
        duplication: Number((e.metrics.duplicationRatio * 100).toFixed(1)),
        avgCyclomatic: Number(e.metrics.avgCyclomatic.toFixed(2)),
        commentDensity: Number((e.metrics.commentDensity * 100).toFixed(1)),
      })),
    [withMetrics],
  );

  return (
    <div>
      <section className="panel">
        <h2>Select projects to compare</h2>
        {error && <p className="error">{error}</p>}
        {projects.length === 0 ? (
          <p className="muted">No projects yet. <Link to="/">Register one</Link>.</p>
        ) : (
          projects.map((p) => (
            <label key={p.id} className="checkbox-row">
              <input type="checkbox" checked={selected.includes(p.id)} onChange={() => toggle(p.id)} />
              {p.name} <TierBadge tier={p.tier} />
            </label>
          ))
        )}
      </section>

      {entries.length > 0 && (
        <section className="panel">
          <h2>Side-by-side</h2>
          <table>
            <thead>
              <tr>
                <th>Metric</th>
                {entries.map((e) => (
                  <th key={e.projectId}>{e.name}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {ROWS.map((row) => (
                <tr key={row.label}>
                  <td className="muted">{row.label}</td>
                  {entries.map((e) => (
                    <td key={e.projectId}>{e.metrics ? row.get(e.metrics) : <span className="muted">not analyzed</span>}</td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      )}

      {chartData.length > 0 && (
        <>
          <MetricBarChart data={chartData} dataKey="mi" label="Maintainability Index" color="#22c55e" />
          <MetricBarChart data={chartData} dataKey="duplication" label="Duplication (%)" color="#ef4444" />
          <MetricBarChart data={chartData} dataKey="avgCyclomatic" label="Avg Cyclomatic Complexity" color="#f59e0b" />
          <MetricBarChart data={chartData} dataKey="commentDensity" label="Comment Density (%)" color="#38bdf8" />
        </>
      )}
    </div>
  );
}
