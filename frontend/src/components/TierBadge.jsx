export default function TierBadge({ tier }) {
  if (!tier) return null;
  return <span className={`badge ${tier}`}>{tier.replace(/_/g, ' ')}</span>;
}
