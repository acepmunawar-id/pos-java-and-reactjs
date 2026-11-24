/**
 * AlertBanner — banner peringatan yang bisa ditutup.
 *
 * Props:
 *   type      {string}   — "warning" | "error" | "info" | "success"  (default "warning")
 *   onDismiss {function} — dipanggil saat tombol ✕ diklik; kalau tidak ada, tombol tidak muncul
 *   children  {ReactNode}— isi pesan
 *
 * Contoh:
 *   <AlertBanner type="warning" onDismiss={() => setDismissed(true)}>
 *     <strong>5 produk</strong> stok menipis.
 *   </AlertBanner>
 */

const TYPE_MAP = {
  warning: {
    wrapper: 'border-orange-200 bg-orange-50',
    text:    'text-orange-800',
    dismiss: 'text-orange-400 hover:text-orange-700',
    icon:    '⚠️',
  },
  error: {
    wrapper: 'border-red-200 bg-red-50',
    text:    'text-red-800',
    dismiss: 'text-red-400 hover:text-red-700',
    icon:    '🚨',
  },
  info: {
    wrapper: 'border-blue-200 bg-blue-50',
    text:    'text-blue-800',
    dismiss: 'text-blue-400 hover:text-blue-700',
    icon:    'ℹ️',
  },
  success: {
    wrapper: 'border-emerald-200 bg-emerald-50',
    text:    'text-emerald-800',
    dismiss: 'text-emerald-400 hover:text-emerald-700',
    icon:    '✅',
  },
}

export default function AlertBanner({ type = 'warning', onDismiss, children }) {
  const t = TYPE_MAP[type] ?? TYPE_MAP.warning
  return (
    <div className={`flex items-center justify-between gap-3 rounded-xl border px-4 py-3 ${t.wrapper}`}>
      <div className={`flex items-center gap-2 text-sm ${t.text}`}>
        <span className="text-lg">{t.icon}</span>
        <span>{children}</span>
      </div>
      {onDismiss && (
        <button
          type="button"
          onClick={onDismiss}
          className={`shrink-0 ${t.dismiss}`}
          aria-label="Tutup"
        >
          ✕
        </button>
      )}
    </div>
  )
}
