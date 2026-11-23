/**
 * SectionCard — wrapper kartu putih dengan header opsional.
 *
 * Props:
 *   title     {string}    — judul di header; kalau kosong, header tidak ditampilkan
 *   badge     {ReactNode} — elemen di kanan header (mis. badge jumlah, tombol)
 *   className {string}    — kelas tambahan untuk wrapper luar
 *   bodyClass {string}    — kelas tambahan untuk area konten (di bawah header)
 *   children  {ReactNode} — konten kartu
 *
 * Contoh pemakaian:
 *   <SectionCard title="Transaksi Terakhir" badge={<span>8 terbaru</span>}>
 *     <table>…</table>
 *   </SectionCard>
 */
export default function SectionCard({ title, badge, className = '', bodyClass = '', children }) {
  return (
    <div className={`rounded-xl bg-white shadow-sm ${className}`}>
      {title && (
        <div className="flex items-center justify-between border-b border-slate-100 px-5 py-4">
          <h2 className="text-sm font-semibold text-slate-700">{title}</h2>
          {badge && <div className="text-xs text-slate-400">{badge}</div>}
        </div>
      )}
      <div className={bodyClass}>{children}</div>
    </div>
  )
}
