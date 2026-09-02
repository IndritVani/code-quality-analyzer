// Mirrors the backend ResultAggregator so the Score Breakdown view can decompose the
// Maintainability Index into its contributing terms. The score is layered: a classic
// Coleman–Oman / SEI base (Volume + complexity + size), then duplication/coupling penalties
// and a documentation bonus for the signals the classic formula ignores.
//
// All factor weights are configurable on the backend under analyzer.mi.* — fetch them via
// api/client.js#getMiWeights and pass them in so the breakdown reflects the live config.

// Fallback used when the active weights haven't been fetched yet; matches the backend defaults
// in com.analyzer.config.MaintainabilityProperties so the chart renders sanely before load.
export const DEFAULT_WEIGHTS = {
  baseWeight: 1.0,
  base: { constant: 171.0, volumeCoefficient: 5.2, complexityCoefficient: 0.23, locCoefficient: 16.2 },
  scale: { offset: 4.0, slope: 1.8 },
  duplicationWeight: 15.0,
  couplingWeight: 10.0,
  documentationWeight: 10.0,
  couplingCap: 20.0,
  commentCap: 0.30,
};

const ln = (x) => Math.log(Math.max(x, 1));
const clamp = (x) => Math.max(0, Math.min(100, x));

// Classic SEI MI, normalized by the intercept, then linearly remapped onto the intuitive 0–100
// display range — mirrors ResultAggregator's seiBase → displayBase step so the chart's base bar
// matches the backend score.
export function classicBase(m, w = DEFAULT_WEIGHTS) {
  const b = w.base;
  const norm = (b.constant
      - b.volumeCoefficient * ln(m.avgVolume)
      - b.complexityCoefficient * m.avgCyclomatic
      - b.locCoefficient * ln(m.avgLoc)) * 100 / b.constant;
  const seiBase = clamp(w.baseWeight * norm);
  return clamp(w.scale.offset + w.scale.slope * seiBase);
}

export function miBreakdown(m, w = DEFAULT_WEIGHTS) {
  const base = classicBase(m, w);
  const duplication = w.duplicationWeight * Math.min(m.duplicationRatio, 1);
  const coupling = (w.couplingWeight * Math.min(m.avgCoupling, w.couplingCap)) / w.couplingCap;
  const documentation = (w.documentationWeight * Math.min(m.commentDensity, w.commentCap)) / w.commentCap;
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
  if (mi >= 75) return 'score-good';
  if (mi >= 50) return 'score-mid';
  return 'score-bad';
}

export const fmt = (n, d = 1) => (n == null ? '—' : Number(n).toFixed(d));
export const pct = (n, d = 1) => (n == null ? '—' : `${(Number(n) * 100).toFixed(d)}%`);
