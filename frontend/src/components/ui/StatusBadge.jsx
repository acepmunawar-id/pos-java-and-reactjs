/**
 * StatusBadge — badge status untuk transaksi / entitas lain.
 *
 * Props:
 *   status  {string} — kode status dari backend, mis. "VOIDED", "COMPLETED"
 *   map     {object} — opsional: override pemetaan status ke label & warna
 *                      format: { STATUS: { label: 'Teks', color: 'emerald' } }
 *
 * Warna yang didukung: emerald | red | amber | blue | violet | slate
 */

const DEFAULT_MAP = {
  COMPLETED: { label: 'Lunas',    color: 'emerald' },
  VOIDED:    { label: 'Void',     color: 'red'     },
  PENDING:   { label: 'Pending',  color: 'amber'   },
  DRAFT:     { label: 'Draft',    color: 'amber'   },
  ACTIVE:    { label: 'Aktif',    color: 'emerald' },
  INACTIVE:  { label: 'Nonaktif', color: 'slate'   },
}

const COLOR_CLASSES = {
  emerald: 'bg-emerald-100 text-emerald-700',
  red:     'bg-red-100 text-red-600',
  amber:   'bg-amber-100 text-amber-700',
  blue:    'bg-blue-100 text-blue-700',
  violet:  'bg-violet-100 text-violet-700',
  slate:   'bg-slate-100 text-slate-600',
}

export default function StatusBadge({ status, map }) {
  const merged = map ? { ...DEFAULT_MAP, ...map } : DEFAULT_MAP
  const cfg    = merged[status] ?? { label: status, color: 'slate' }
  const cls    = COLOR_CLASSES[cfg.color] ?? COLOR_CLASSES.slate

  return (
    <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${cls}`}>
      {cfg.label}
    </span>
  )
}
