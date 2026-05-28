// Mirrors the backend ResultAggregator so the Score Breakdown view can decompose the
// Maintainability Index into its contributing terms. The score is layered: a classic
// Coleman–Oman / SEI base (Volume + complexity + size), then duplication/coupling penalties
// and a documentation bonus for the signals the classic formula ignores.
export const CAPS = { coupling: 20, comment: 0.3 };
export const WEIGHTS = { duplication: 15, coupling: 10, documentation: 10 };

const ln = (x) => Math.log(Math.max(x, 1));

// Classic SEI MI, normalized to 0–100 the way Visual Studio does. Mirrors
// ResultAggregator#maintainabilityIndex's base term.
export function classicBase(m) {
  const raw = (171 - 5.2 * ln(m.avgVolume) - 0.23 * m.avgCyclomatic - 16.2 * ln(m.avgLoc)) * 100 / 171;
  return Math.max(0, Math.min(100, raw));
}

export function miBreakdown(m) {
  const base = classicBase(m);
  const duplication = WEIGHTS.duplication * Math.min(m.duplicationRatio, 1);
  const coupling = (WEIGHTS.coupling * Math.min(m.avgCoupling, CAPS.coupling)) / CAPS.coupling;
  const documentation = (WEIGHTS.documentation * Math.min(m.commentDensity, CAPS.comment)) / CAPS.comment;
  return [
    { name: 'Base (SEI MI)', value: base, kind: 'base' },
    { name: 'Duplication', value: -duplication, kind: 'penalty' },
    { name: 'Coupling', value: -coupling, kind: 'penalty' },
    { name: 'Docs', value: documentation, kind: 'bonus' },
  ];
}

// Breakpoints mirror TierClassifier (WELL_MAINTAINED_MIN / AVERAGE_MIN) so the score color
// always agrees with the project's tier badge.
export function scoreClass(mi) {
  if (mi == null) return 'muted';
  if (mi >= 45) return 'score-good';
  if (mi >= 25) return 'score-mid';
  return 'score-bad';
}

export const fmt = (n, d = 1) => (n == null ? '—' : Number(n).toFixed(d));
export const pct = (n, d = 1) => (n == null ? '—' : `${(Number(n) * 100).toFixed(d)}%`);
