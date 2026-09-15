import { useQuery } from '@tanstack/react-query'
import { useMemo } from 'react'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts'
import { useAuth } from '../../auth/AuthContext'
import { listSales } from '../../api/sales'
import { productsApi } from '../../api/resources'

function money(value) {
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(
    value || 0,
  )
}

function isoDate(date) {
  return date.toISOString().slice(0, 10)
}

// Full class names spelled out (not interpolated) so Tailwind's build-time
// scanner can actually find and generate them.
const TONE_CLASSES = {
  emerald: 'text-emerald-600',
  amber: 'text-amber-600',
}

function StatCard({ label, value, tone = 'emerald' }) {
  return (
    <div className="rounded-xl bg-white p-5 shadow-sm">
      <div className="text-sm text-slate-500">{label}</div>
      <div className={`mt-1 text-2xl font-semibold ${TONE_CLASSES[tone]}`}>{value}</div>
    </div>
  )
}

export default function DashboardPage() {
  const { user, hasRole } = useAuth()
  const isManager = hasRole('SUPER_ADMIN', 'ADMIN')

  const today = isoDate(new Date())
  const weekAgo = isoDate(new Date(Date.now() - 6 * 24 * 60 * 60 * 1000))

  const { data: todaySales } = useQuery({
    queryKey: ['dashboard', 'today-sales', today],
    queryFn: () => listSales({ from: today, to: today, size: 100 }),
  })

  const { data: weekSales } = useQuery({
    queryKey: ['dashboard', 'week-sales', weekAgo, today],
    queryFn: () => listSales({ from: weekAgo, to: today, size: 200 }),
  })

  const { data: lowStock } = useQuery({
    queryKey: ['dashboard', 'low-stock'],
    queryFn: () => productsApi.lowStock(),
    enabled: isManager,
  })

  const todayTotal = useMemo(
    () => (todaySales?.content || []).reduce((sum, s) => sum + Number(s.total), 0),
    [todaySales],
  )
  const todayCount = todaySales?.content?.length || 0

  const chartData = useMemo(() => {
    const byDay = {}
    for (let i = 0; i < 7; i++) {
      const d = isoDate(new Date(Date.now() - i * 24 * 60 * 60 * 1000))
      byDay[d] = 0
    }
    ;(weekSales?.content || []).forEach((sale) => {
      const day = sale.saleDate?.slice(0, 10)
      if (day in byDay) byDay[day] += Number(sale.total)
    })
    return Object.entries(byDay)
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([date, total]) => ({ date: date.slice(5), total }))
  }, [weekSales])

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-slate-800">Selamat datang, {user.fullName}</h1>
        <p className="text-sm text-slate-500">Ringkasan aktivitas toko hari ini</p>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <StatCard label="Transaksi Hari Ini" value={todayCount} />
        <StatCard label="Total Penjualan Hari Ini" value={money(todayTotal)} />
        {isManager && (
          <StatCard label="Produk Stok Menipis" value={lowStock?.length || 0} tone="amber" />
        )}
      </div>

      {isManager && (
        <div className="rounded-xl bg-white p-5 shadow-sm">
          <h2 className="mb-4 text-sm font-semibold text-slate-700">Penjualan 7 Hari Terakhir</h2>
          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="date" fontSize={12} />
                <YAxis fontSize={12} tickFormatter={(v) => `${Math.round(v / 1000)}rb`} />
                <Tooltip formatter={(v) => money(v)} />
                <Bar dataKey="total" fill="#059669" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}

      {isManager && lowStock?.length > 0 && (
        <div className="rounded-xl bg-white p-5 shadow-sm">
          <h2 className="mb-3 text-sm font-semibold text-slate-700">Peringatan Stok Menipis</h2>
          <ul className="divide-y divide-slate-100 text-sm">
            {lowStock.map((p) => (
              <li key={p.id} className="flex justify-between py-2">
                <span>{p.name}</span>
                <span className="font-medium text-red-600">
                  Sisa {p.stock} {p.unit?.symbol || ''}
                </span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  )
}
