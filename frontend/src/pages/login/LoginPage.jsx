import { useState } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth, landingPathFor } from '../../auth/AuthContext'
import { errorMessage } from '../../api/client'

export default function LoginPage() {
  const { login, isAuthenticated, user } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  if (isAuthenticated) {
    return <Navigate to={location.state?.from || landingPathFor(user.role)} replace />
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const loggedInUser = await login(username, password)
      // Role-based redirect: cashier goes straight to the POS screen,
      // admin/super admin land on the dashboard.
      navigate(landingPathFor(loggedInUser.role), { replace: true })
    } catch (err) {
      setError(errorMessage(err, 'Username atau password salah'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 p-4">
      <div className="w-full max-w-sm rounded-2xl bg-white p-8 shadow-lg">
        <div className="mb-6 text-center">
          <div className="mb-2 text-4xl">🛒</div>
          <h1 className="text-xl font-semibold text-slate-800">POS App</h1>
          <p className="text-sm text-slate-500">Masuk untuk melanjutkan</p>
        </div>
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && <div className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</div>}
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-600">Username</span>
            <input
              autoFocus
              required
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none"
            />
          </label>
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-600">Password</span>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none"
            />
          </label>
          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-lg bg-emerald-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-emerald-700 disabled:opacity-60"
          >
            {loading ? 'Memproses...' : 'Masuk'}
          </button>
        </form>
        <p className="mt-6 text-center text-xs text-slate-400">
          Akun default: superadmin / admin / kasir &mdash; password: Password123!
        </p>
      </div>
    </div>
  )
}
