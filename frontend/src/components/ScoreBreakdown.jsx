import { Bar, BarChart, Cell, ReferenceLine, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { miBreakdown } from '../lib/metrics.js';

const colorFor = (kind) => (kind === 'bonus' ? '#22c55e' : kind === 'base' ? '#38bdf8' : '#ef4444');

export default function ScoreBreakdown({ metrics }) {
  const data = miBreakdown(metrics).map((d) => ({ ...d, value: Number(d.value.toFixed(2)) }));
  return (
    <div style={{ width: '100%', height: 280 }}>
      <ResponsiveContainer>
        <BarChart data={data} margin={{ top: 10, right: 16, bottom: 4, left: -8 }}>
          <XAxis dataKey="name" stroke="#94a3b8" fontSize={12} />
          <YAxis stroke="#94a3b8" fontSize={12} />
          <ReferenceLine y={0} stroke="#64748b" />
          <Tooltip
            cursor={{ fill: 'rgba(148,163,184,0.08)' }}
            contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: 8 }}
            formatter={(v) => [`${v > 0 ? '+' : ''}${v} pts`, 'Contribution']}
          />
          <Bar dataKey="value" radius={[4, 4, 0, 0]}>
            {data.map((d, i) => (
              <Cell key={i} fill={colorFor(d.kind)} />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
