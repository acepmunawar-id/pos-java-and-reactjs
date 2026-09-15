import { Link } from 'react-router-dom'

export default function ForbiddenPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-3 bg-slate-100 text-center">
      <div className="text-5xl">🚫</div>
      <h1 className="text-xl font-semibold text-slate-800">Akses Ditolak</h1>
      <p className="text-sm text-slate-500">Anda tidak memiliki hak akses ke halaman ini.</p>
      <Link to="/dashboard" className="mt-2 text-sm font-medium text-emerald-600 hover:underline">
        Kembali ke Dashboard
      </Link>
    </div>
  )
}
