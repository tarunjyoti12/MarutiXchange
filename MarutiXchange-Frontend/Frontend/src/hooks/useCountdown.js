import { useEffect, useState } from 'react';

// Returns formatted "Hh Mm Ss" string (or "Ended") for a future timestamp.
export function useCountdown(endsAt) {
  const [ms, setMs] = useState(() => Math.max(0, endsAt - Date.now()));

  useEffect(() => {
    if (ms <= 0) return undefined;
    const t = setInterval(() => {
      const next = Math.max(0, endsAt - Date.now());
      setMs(next);
      if (next === 0) clearInterval(t);
    }, 1000);
    return () => clearInterval(t);
  }, [endsAt, ms]);

  return formatMs(ms);
}

function formatMs(ms) {
  if (ms <= 0) return 'Ended';
  const totalSec = Math.floor(ms / 1000);
  const h = Math.floor(totalSec / 3600);
  const m = Math.floor((totalSec % 3600) / 60);
  const s = totalSec % 60;
  if (h > 0) return `${h}h ${m}m ${s}s`;
  if (m > 0) return `${m}m ${s}s`;
  return `${s}s`;
}
