import { useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { NAV_ITEMS, ROLE_LABEL } from './navConfig'

export default function AppLayout() {
  const { user, logout } = useAuth()
  const [menuOpen, setMenuOpen] = useState(false)

  const items = NAV_ITEMS.filter((item) => item.roles.includes(user.role))

  return (
    <div className="flex min-h-screen bg-slate-100">
      {/* Sidebar - fixed on desktop, slide-over on mobile for a responsive layout */}
      <aside
        className={`fixed inset-y-0 left-0 z-30 w-64 transform bg-slate-900 text-slate-100 transition-transform duration-200 md:static md:translate-x-0 ${
          menuOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="flex h-16 items-center gap-2 border-b border-slate-800 px-4">
          <span className="text-xl">🛒</span>
          <span className="text-lg font-semibold">POS App</span>
        </div>
        <nav className="flex flex-col gap-1 p-3">
          {items.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              onClick={() => setMenuOpen(false)}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition ${
                  isActive ? 'bg-emerald-600 text-white' : 'text-slate-300 hover:bg-slate-800'
                }`
              }
            >
              <span>{item.icon}</span>
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      {menuOpen && (
        <div
          className="fixed inset-0 z-20 bg-black/40 md:hidden"
          onClick={() => setMenuOpen(false)}
        />
      )}

      <div className="flex min-h-screen flex-1 flex-col md:ml-0">
        <header className="sticky top-0 z-10 flex h-16 items-center justify-between border-b border-slate-200 bg-white px-4 shadow-sm">
          <button
            className="rounded-md p-2 text-slate-600 hover:bg-slate-100 md:hidden"
            onClick={() => setMenuOpen((v) => !v)}
            aria-label="Buka menu"
          >
            ☰
          </button>
          <div className="hidden text-sm text-slate-500 md:block">
            Point of Sale &mdash; {ROLE_LABEL[user.role]}
          </div>
          <div className="flex items-center gap-3">
            <div className="text-right">
              <div className="text-sm font-semibold text-slate-800">{user.fullName}</div>
              <div className="text-xs text-slate-500">{ROLE_LABEL[user.role]}</div>
            </div>
            <button
              onClick={logout}
              className="rounded-md bg-slate-100 px-3 py-1.5 text-sm font-medium text-slate-700 hover:bg-slate-200"
            >
              Keluar
            </button>
          </div>
        </header>

        <main className="flex-1 p-4 md:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
