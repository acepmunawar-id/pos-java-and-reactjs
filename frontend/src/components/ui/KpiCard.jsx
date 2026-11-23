/**
 * KpiCard — kartu metrik utama.
 *
 * variant="solid"   : background warna penuh, teks putih (baris atas dashboard).
 * variant="default" : background putih, ikon berwarna (baris kedua dashboard).
 *
 * Props:
 *   label    {string}        — judul metrik
 *   value    {string|number} — nilai utama
 *   icon     {string}        — emoji/teks ikon
 *   color    {string}        — "orange" | "teal" | "emerald" | "blue" | "violet" | "amber"
 *   sub      {string}        — teks keterangan kecil
 *   pct      {number|null}   — % perubahan; null = tidak ditampilkan
 *   pctLabel {string}        — label perbandingan, default "vs kemarin"
 *   variant  {string}        — "solid" | "default"
 */

const SOLID_BG = {
  orange:  'bg-orange-500',
  teal:    'bg-teal-800',
  emerald: 'bg-emerald-600',
  blue:    'bg-blue-600',
  violet:  'bg-violet-600',
  amber:   'bg-amber-500',
}

const DEFAULT_ICON_BG = {
  orange:  'bg-orange-50 text-orange-500',
  teal:    'bg-teal-50 text-teal-700',
  emerald: 'bg-emerald-50 text-emerald-600',
  blue:    'bg-blue-50 text-blue-600',
  violet:  'bg-violet-50 text-violet-600',
  amber:   'bg-amber-50 text-amber-500',
}

export default function KpiCard({
  label,
  value,
  icon,
  color = 'orange',
  sub,
  pct = null,
  pctLabel = 'vs kemarin',
  variant = 'default',
}) {
  const pctNum = pct !== null ? parseFloat(pct) : null

  /* ── Solid: background warna penuh ── */
  if (variant === 'solid') {
    return (
      <div className={`rounded-xl p-5 text-white shadow-sm ${SOLID_BG[color] ?? 'bg-orange-500'}`}>
        <div className="flex items-start justify-between">
          <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-white/20">
            <span className="text-2xl">{icon}</span>
          </div>
          {pctNum !== null && (
            <span className="rounded-full bg-white/20 px-2.5 py-0.5 text-xs font-semibold">
              {pctNum >= 0 ? '▲' : '▼'} {Math.abs(pctNum)}%
            </span>
          )}
        </div>
        <div className="mt-4">
          <p className="text-sm font-medium text-white/80">{label}</p>
          <p className="mt-1 truncate text-2xl font-bold">{value}</p>
          {sub && <p className="mt-0.5 text-xs text-white/60">{sub}</p>}
          {pctNum !== null && pctLabel && (
            <p className="mt-0.5 text-xs text-white/60">{pctLabel}</p>
          )}
        </div>
      </div>
    )
  }

  /* ── Default: background putih ── */
  const iconCls = DEFAULT_ICON_BG[color] ?? DEFAULT_ICON_BG.orange
  return (
    <div className="flex items-start gap-4 rounded-xl bg-white p-5 shadow-sm">
      <div className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-xl ${iconCls}`}>
        <span className="text-2xl">{icon}</span>
      </div>
      <div className="min-w-0 flex-1">
        <p className="text-xs font-medium uppercase tracking-wide text-slate-500">{label}</p>
        <p className="mt-0.5 truncate text-2xl font-bold text-slate-800">{value}</p>
        {sub && <p className="mt-0.5 text-xs text-slate-400">{sub}</p>}
        {pctNum !== null && (
          <p className={`mt-1 text-xs font-medium ${pctNum >= 0 ? 'text-emerald-600' : 'text-red-500'}`}>
            {pctNum >= 0 ? '▲' : '▼'} {Math.abs(pctNum)}% {pctLabel}
          </p>
        )}
      </div>
    </div>
  )
}
