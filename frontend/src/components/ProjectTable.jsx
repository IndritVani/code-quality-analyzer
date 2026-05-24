import { Link } from 'react-router-dom';
import TierBadge from './TierBadge.jsx';
import { fmt, scoreClass } from '../lib/metrics.js';

export default function ProjectTable({ rows, busyId, onAnalyze, onDelete }) {
  if (rows.length === 0) {
    return <p className="muted">No projects registered yet. Add one above to get started.</p>;
  }
  return (
    <table>
      <thead>
        <tr>
          <th>Name</th>
          <th>Tier</th>
          <th>Maintainability</th>
          <th>Status</th>
          <th>Last analyzed</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        {rows.map((row) => (
          <tr key={row.id}>
            <td><Link to={`/projects/${row.id}`}>{row.name}</Link></td>
            <td><TierBadge tier={row.tier} /></td>
            <td className={scoreClass(row.mi)} style={{ fontWeight: 700 }}>
              {row.mi == null ? '—' : fmt(row.mi, 1)}
            </td>
            <td>
              {row.status
                ? <span className={`status ${row.status}`}>{row.status}</span>
                : <span className="muted">—</span>}
            </td>
            <td className="muted">
              {row.analyzedAt ? new Date(row.analyzedAt).toLocaleString() : 'never'}
            </td>
            <td>
              <div className="row-actions">
                <button
                  className="btn"
                  disabled={busyId === row.id}
                  onClick={() => onAnalyze(row.id)}
                >
                  {busyId === row.id ? 'Analyzing…' : 'Analyze'}
                </button>
                <button className="btn danger" onClick={() => onDelete(row.id)}>Delete</button>
              </div>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
