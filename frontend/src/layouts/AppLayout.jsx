import { useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { NAV_ITEMS, ROLE_LABEL } from './navConfig'

export default function AppLayout() {
  const { user, logout } = useAuth()
  const [menuOpen, setMenuOpen]     = useState(false)
  const [collapsed, setCollapsed]   = useState(false)

  const items = NAV_ITEMS.filter((item) => item.roles.includes(user.role))

  return (
    <div className="flex min-h-screen bg-slate-100">

      {/* ── Sidebar ─────────────────────────────────────────── */}
      <aside
        className={`
          fixed inset-y-0 left-0 z-30 flex flex-col bg-slate-900 text-slate-100
          transition-all duration-200 overflow-hidden
          md:static md:translate-x-0
          w-64
          ${collapsed ? 'md:w-16' : 'md:w-64'}
          ${menuOpen  ? 'translate-x-0' : '-translate-x-full'}
        `}
      >
        {/* Logo — struktur identik dengan nav item agar icon sejajar */}
        <div className={`flex h-16 shrink-0 items-center border-b border-slate-800 transition-all duration-200
          ${collapsed ? 'md:justify-center md:px-0 md:gap-0 gap-3 px-3' : 'gap-3 px-3'}`}>
          <span className="shrink-0 text-base leading-none">🛒</span>
          <span className={`whitespace-nowrap text-base font-semibold overflow-hidden transition-all duration-200
            ${collapsed ? 'md:w-0 md:opacity-0' : 'opacity-100'}`}>
            POS App
          </span>
        </div>

        {/* Nav items */}
        <nav className="flex flex-col gap-0.5 overflow-y-auto p-2 flex-1">
          {items.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              title={collapsed ? item.label : ''}
              onClick={() => setMenuOpen(false)}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-lg py-2 text-sm font-medium transition
                 ${collapsed ? 'md:justify-center md:px-0 md:gap-0 px-3' : 'px-3'}
                 ${isActive ? 'bg-emerald-600 text-white' : 'text-slate-300 hover:bg-slate-800'}`
              }
            >
              <span className="shrink-0 text-base leading-none">{item.icon}</span>
              <span className={`whitespace-nowrap overflow-hidden transition-all duration-200 ${collapsed ? 'md:w-0 md:opacity-0' : 'opacity-100'}`}>
                {item.label}
              </span>
            </NavLink>
          ))}
        </nav>

        {/* Tombol expand + hak cipta — hanya desktop */}
        <div className="shrink-0 border-t border-slate-800 p-2 hidden md:block">
          <button
            onClick={() => setCollapsed(v => !v)}
            title={collapsed ? 'Perluas sidebar' : 'Ciutkan sidebar'}
            className={`flex w-full items-center rounded-lg py-2 text-sm font-medium text-slate-400 hover:bg-slate-800 hover:text-white transition
              ${collapsed ? 'justify-center px-0 gap-0' : 'gap-3 px-3'}`}
          >
            <span className="shrink-0 text-base leading-none">{collapsed ? '›' : '‹'}</span>
            <span className={`whitespace-nowrap overflow-hidden transition-all duration-200 ${collapsed ? 'w-0 opacity-0' : 'opacity-100'}`}>
              Ciutkan
            </span>
          </button>
          {!collapsed && (
            <p className="mt-1 text-center text-[10px] text-slate-600">
              &copy; {new Date().getFullYear()} POS App. All rights reserved.
            </p>
          )}
        </div>
      </aside>

      {/* Mobile overlay */}
      {menuOpen && (
        <div
          className="fixed inset-0 z-20 bg-black/40 md:hidden"
          onClick={() => setMenuOpen(false)}
        />
      )}

      {/* ── Main area ───────────────────────────────────────── */}
      <div className="flex min-h-screen flex-1 flex-col min-w-0">
        <header className="sticky top-0 z-10 flex h-16 items-center justify-between border-b border-slate-200 bg-white px-4 shadow-sm">
          {/* Hamburger — mobile only */}
          <button
            className="rounded-md p-2 text-slate-600 hover:bg-slate-100 md:hidden"
            onClick={() => setMenuOpen(v => !v)}
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
