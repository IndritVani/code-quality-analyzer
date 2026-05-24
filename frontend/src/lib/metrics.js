// Mirrors the backend ResultAggregator weights/caps so the Score Breakdown view can
// decompose the Maintainability Index into its contributing terms.
export const CAPS = { cyclomatic: 25, coupling: 20, comment: 0.3 };

export function miBreakdown(m) {
  const complexity = (0.4 * Math.min(m.avgCyclomatic, CAPS.cyclomatic)) / CAPS.cyclomatic * 100;
  const duplication = 0.25 * Math.min(m.duplicationRatio, 1) * 100;
  const coupling = (0.2 * Math.min(m.avgCoupling, CAPS.coupling)) / CAPS.coupling * 100;
  const documentation = (0.15 * Math.min(m.commentDensity, CAPS.comment)) / CAPS.comment * 100;
  return [
    { name: 'Base', value: 100, kind: 'base' },
    { name: 'Complexity', value: -complexity, kind: 'penalty' },
    { name: 'Duplication', value: -duplication, kind: 'penalty' },
    { name: 'Coupling', value: -coupling, kind: 'penalty' },
    { name: 'Docs', value: documentation, kind: 'bonus' },
  ];
}

export function scoreClass(mi) {
  if (mi == null) return 'muted';
  if (mi >= 75) return 'score-good';
  if (mi >= 50) return 'score-mid';
  return 'score-bad';
}

export const fmt = (n, d = 1) => (n == null ? '—' : Number(n).toFixed(d));
export const pct = (n, d = 1) => (n == null ? '—' : `${(Number(n) * 100).toFixed(d)}%`);
