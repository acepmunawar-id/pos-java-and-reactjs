/**
 * StatCard — kartu statistik sekunder (baris kedua dashboard).
 *
 * Props:
 *   label     {string}        — nama metrik
 *   value     {string|number} — nilai utama (tampil besar)
 *   icon      {string}        — emoji di sudut kanan atas
 *   sub       {string}        — teks keterangan (muncul bila pct tidak ada)
 *   pct       {number}        — % perubahan; undefined = tidak ditampilkan
 *   pctLabel  {string}        — label perbandingan, default "vs bulan lalu"
 *   link      {string}        — teks tombol kanan bawah, mis. "Lihat Semua"
 *   onLink    {function}      — callback klik tombol link
 */
export default function StatCard({ label, value, icon, sub, pct, pctLabel = 'vs bulan lalu', link, onLink }) {
  const pctNum = pct !== null && pct !== undefined ? parseFloat(pct) : null

  return (
    <div className="rounded-xl bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="truncate text-2xl font-bold text-slate-800">{value}</p>
          <p className="mt-0.5 text-sm text-slate-500">{label}</p>
        </div>
        {icon && (
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-teal-50 text-xl">
            {icon}
          </div>
        )}
      </div>

      <div className="mt-3 flex items-center justify-between gap-2">
        {pctNum !== null ? (
          <p className={`text-xs font-medium ${pctNum >= 0 ? 'text-emerald-600' : 'text-red-500'}`}>
            {pctNum >= 0 ? '▲' : '▼'} {Math.abs(pctNum)}% {pctLabel}
          </p>
        ) : (
          <p className="text-xs text-slate-400">{sub}</p>
        )}
        {link && (
          <button type="button" onClick={onLink} className="shrink-0 text-xs font-medium text-blue-600 hover:underline">
            {link}
          </button>
        )}
      </div>
    </div>
  )
}
