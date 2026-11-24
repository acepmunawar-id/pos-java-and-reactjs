import { useQuery } from '@tanstack/react-query'
import { useMemo, useState } from 'react'
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid,
  PieChart, Pie, Cell,
} from 'recharts'
import { useAuth } from '../../auth/AuthContext'
import { listSales } from '../../api/sales'
import { productsApi, categoriesApi, customersApi } from '../../api/resources'
import { AlertBanner, KpiCard, SectionCard, StatCard, StatusBadge } from '../../components/ui'

/* ───────── helpers ───────── */
function money(v) {
  return new Intl.NumberFormat('id-ID', {
    style: 'currency', currency: 'IDR', maximumFractionDigits: 0,
  }).format(v || 0)
}
function compact(v) {
  if (v >= 1_000_000) return `${(v / 1_000_000).toFixed(1)}jt`
  if (v >= 1_000)     return `${(v / 1_000).toFixed(0)}rb`
  return String(v)
}
function isoDate(d) { return d.toISOString().slice(0, 10) }
function formatDate(str) {
  if (!str) return '-'
  return new Date(str).toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric' })
}
function pctChange(now, before) {
  if (!before || before === 0) return null
  return ((now - before) / before * 100).toFixed(1)
}
function labelDate(str) {
  if (!str) return ''
  return new Date(str).toLocaleDateString('id-ID', { day: 'numeric', month: 'short', year: 'numeric' })
}

/* ── Tanggal dihitung sekali saat modul di-load ── */
const _now   = new Date()
const _nowMs = _now.getTime()
const TODAY  = isoDate(_now)

/* Default filter: awal bulan ini sampai hari ini */
const DEFAULT_YEAR  = _now.getFullYear()
const DEFAULT_MONTH = _now.getMonth()           // 0-based
const DEFAULT_FROM  = isoDate(new Date(DEFAULT_YEAR, DEFAULT_MONTH, 1))
const DEFAULT_TO    = TODAY

const MONTH_NAMES = ['Jan','Feb','Mar','Apr','Mei','Jun','Jul','Ags','Sep','Okt','Nov','Des']

function monthRange(year, month) {
  const from = isoDate(new Date(year, month, 1))
  const last  = new Date(year, month + 1, 0)   // hari terakhir bulan
  const to    = isoDate(last > _now ? _now : last)
  return { from, to }
}

/* ── Chart: bangun data harian/mingguan/bulanan sesuai rentang ── */
function buildChartData(sales, fromStr, toStr) {
  const active = sales.filter(s => s.status !== 'VOIDED')
  const msFrom = new Date(fromStr).getTime()
  const msTo   = new Date(toStr).getTime()
  const days   = Math.round((msTo - msFrom) / 86400000) + 1

  if (days > 90) {
    // Kelompokkan per bulan
    const months = {}
    active.forEach(s => {
      const key = s.saleDate?.slice(0, 7)
      if (key) months[key] = (months[key] || 0) + Number(s.total)
    })
    return Object.entries(months)
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([date, total]) => ({ date: date.slice(5), total }))
  }

  if (days > 31) {
    // Kelompokkan per minggu
    const weeks = {}
    active.forEach(s => {
      const d = s.saleDate?.slice(0, 10)
      if (!d) return
      const dt  = new Date(d)
      const mon = new Date(dt - dt.getDay() * 86400000)
      const key = isoDate(mon)
      weeks[key] = (weeks[key] || 0) + Number(s.total)
    })
    return Object.entries(weeks)
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([date, total]) => ({ date: date.slice(5).replace('-', '/'), total }))
  }

  // Harian
  const dayMap = {}
  for (let i = 0; i < days; i++) {
    const d = isoDate(new Date(msFrom + i * 86400000))
    dayMap[d] = 0
  }
  active.forEach(s => {
    const day = s.saleDate?.slice(0, 10)
    if (day in dayMap) dayMap[day] += Number(s.total)
  })
  return Object.entries(dayMap).map(([date, total], idx) => ({
    date: (days >= 14 && idx % 5 !== 0) ? '' : date.slice(5).replace('-', '/'),
    total,
  }))
}

/* ── Custom tooltip chart ── */
function ChartTooltip({ active, payload, label }) {
  if (!active || !payload?.length) return null
  return (
    <div className="rounded-lg border border-slate-100 bg-white px-3 py-2 shadow-lg text-sm">
      <p className="font-medium text-slate-600">{label}</p>
      <p className="font-semibold text-orange-600">{money(payload[0].value)}</p>
    </div>
  )
}

/* ══════════════════════════════════════════════
   MAIN DASHBOARD
══════════════════════════════════════════════ */
export default function DashboardPage() {
  const { user, hasRole } = useAuth()
  const isManager = hasRole('SUPER_ADMIN', 'ADMIN')

  const [alertDismissed, setAlertDismissed] = useState(false)

  /* ── Filter state ── */
  const [selYear,  setSelYear]  = useState(DEFAULT_YEAR)
  const [selMonth, setSelMonth] = useState(DEFAULT_MONTH)
  const [inputFrom, setInputFrom] = useState(DEFAULT_FROM)
  const [inputTo,   setInputTo]   = useState(DEFAULT_TO)
  /* from/to aktif — diperbarui saat bulan diklik atau tombol Terapkan */
  const [from, setFrom] = useState(DEFAULT_FROM)
  const [to,   setTo]   = useState(DEFAULT_TO)

  function selectMonth(year, month) {
    const { from: f, to: t } = monthRange(year, month)
    setSelYear(year)
    setSelMonth(month)
    setInputFrom(f)
    setInputTo(t)
    setFrom(f)
    setTo(t)
  }

  function prevYear() { setSelYear(y => y - 1) }
  function nextYear() { setSelYear(y => Math.min(y + 1, DEFAULT_YEAR)) }

  function applyFilter() {
    if (inputFrom && inputTo && inputFrom <= inputTo) {
      setFrom(inputFrom)
      setTo(inputTo)
    }
  }

  /* Hitung rentang periode sebelumnya untuk perbandingan % */
  const { prevFrom, prevTo } = useMemo(() => {
    const msFrom = new Date(from).getTime()
    const msTo   = new Date(to).getTime()
    const span   = msTo - msFrom
    return {
      prevFrom: isoDate(new Date(msFrom - span - 86400000)),
      prevTo:   isoDate(new Date(msFrom - 86400000)),
    }
  }, [from, to])

  /* ── Queries ── */
  const { data: filteredData, isFetching: loadingFiltered } = useQuery({
    queryKey: ['dashboard', 'filtered', from, to],
    queryFn:  () => listSales({ from, to, size: 1000 }),
  })
  const { data: prevData } = useQuery({
    queryKey: ['dashboard', 'prev', prevFrom, prevTo],
    queryFn:  () => listSales({ from: prevFrom, to: prevTo, size: 1000 }),
    enabled:  isManager,
  })
  const { data: recentSales } = useQuery({
    queryKey: ['dashboard', 'recent'],
    queryFn:  () => listSales({ from: isoDate(new Date(_nowMs - 29 * 86400000)), to: TODAY, size: 8 }),
  })
  const { data: lowStock } = useQuery({
    queryKey: ['dashboard', 'low-stock'],
    queryFn:  () => productsApi.lowStock(),
    enabled:  isManager,
  })
  const { data: allProducts } = useQuery({
    queryKey: ['dashboard', 'products-count'],
    queryFn:  () => productsApi.list({ size: 1 }),
    enabled:  isManager,
  })
  const { data: allCategories } = useQuery({
    queryKey: ['dashboard', 'categories-count'],
    queryFn:  () => categoriesApi.list({ size: 1 }),
    enabled:  isManager,
  })
  const { data: allCustomers } = useQuery({
    queryKey: ['dashboard', 'customers-count'],
    queryFn:  () => customersApi.list({ size: 1 }),
    enabled:  isManager,
  })

  /* ── Derived lists ── */
  const filteredList  = useMemo(() => filteredData?.content || [], [filteredData])
  const prevList      = useMemo(() => prevData?.content     || [], [prevData])
  const recentList    = useMemo(() => recentSales?.content  || [], [recentSales])
  const lowStockList  = useMemo(() => lowStock              || [], [lowStock])

  /* ── Metrics dari filtered period ── */
  const activeList     = useMemo(() => filteredList.filter(s => s.status !== 'VOIDED'), [filteredList])
  const voidList       = useMemo(() => filteredList.filter(s => s.status === 'VOIDED'), [filteredList])
  const prevActiveList = useMemo(() => prevList.filter(s => s.status !== 'VOIDED'),     [prevList])

  const totalPenjualan  = useMemo(() => activeList.reduce((a, s) => a + Number(s.total), 0), [activeList])
  const jumlahTransaksi = activeList.length
  const jumlahVoid      = voidList.length
  const jumlahItems     = useMemo(() => activeList.reduce((a, s) => a + (s.items?.length || 0), 0), [activeList])
  const prevTotal       = useMemo(() => prevActiveList.reduce((a, s) => a + Number(s.total), 0), [prevActiveList])
  const prevCount       = prevActiveList.length
  const avgPerTx        = jumlahTransaksi > 0 ? totalPenjualan / jumlahTransaksi : 0

  /* ── Chart ── */
  const chartData  = useMemo(() => buildChartData(filteredList, from, to), [filteredList, from, to])
  const chartTotal = totalPenjualan

  /* ── Top produk ── */
  const topProducts = useMemo(() => {
    const map = {}
    activeList.forEach(s => {
      ;(s.items || []).forEach(item => {
        const key = item.productName || '-'
        if (!map[key]) map[key] = { name: key, qty: 0, total: 0 }
        map[key].qty   += item.quantity || 0
        map[key].total += Number(item.subtotal || 0)
      })
    })
    return Object.values(map).sort((a, b) => b.qty - a.qty).slice(0, 5)
  }, [activeList])

  /* ── Donut: Lunas vs Void ── */
  const totalDonut = jumlahTransaksi + jumlahVoid
  const donutData  = useMemo(() => {
    const d = []
    if (jumlahTransaksi > 0) d.push({ name: 'Lunas', value: jumlahTransaksi, color: '#10b981' })
    if (jumlahVoid > 0)      d.push({ name: 'Void',  value: jumlahVoid,      color: '#ef4444' })
    return d.length > 0 ? d : [{ name: '-', value: 1, color: '#e2e8f0' }]
  }, [jumlahTransaksi, jumlahVoid])

  /* ═══════════════════════════ RENDER ═══════════════════════════ */
  return (
    <div className="space-y-5">

      {/* ── Header ── */}
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h1 className="text-xl font-semibold text-slate-800">
            Selamat datang, {user.fullName}
          </h1>
          <p className="text-sm text-slate-500">
            {_now.toLocaleDateString('id-ID', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}
          </p>
        </div>
        {/* ── Filter periode ── */}
        <div className="w-full flex flex-col gap-2 rounded-xl bg-slate-100 px-3 py-2.5 sm:w-auto">
          {/* Baris 1: navigasi tahun + tombol bulan */}
          <div className="flex items-center gap-1.5 overflow-x-auto pb-0.5">
            <button
              type="button"
              onClick={prevYear}
              className="flex h-6 w-6 shrink-0 items-center justify-center rounded-md text-slate-500 hover:bg-slate-200 hover:text-slate-700"
            >‹</button>
            <span className="min-w-[3.5rem] shrink-0 text-center text-xs font-semibold text-slate-600">{selYear}</span>
            <button
              type="button"
              onClick={nextYear}
              disabled={selYear >= DEFAULT_YEAR}
              className="flex h-6 w-6 shrink-0 items-center justify-center rounded-md text-slate-500 hover:bg-slate-200 hover:text-slate-700 disabled:opacity-30"
            >›</button>
            <span className="mx-1 h-4 w-px shrink-0 bg-slate-300" />
            {MONTH_NAMES.map((name, idx) => {
              const isFuture = selYear === DEFAULT_YEAR && idx > DEFAULT_MONTH
              const isActive = idx === selMonth && from === monthRange(selYear, idx).from
              return (
                <button
                  key={name}
                  type="button"
                  disabled={isFuture}
                  onClick={() => selectMonth(selYear, idx)}
                  className={`shrink-0 rounded-md px-2 py-1 text-xs font-medium transition-all ${
                    isActive
                      ? 'bg-orange-500 text-white shadow-sm'
                      : isFuture
                        ? 'cursor-not-allowed text-slate-300'
                        : 'text-slate-500 hover:bg-slate-200 hover:text-slate-700'
                  }`}
                >
                  {name}
                </button>
              )
            })}
          </div>
          {/* Baris 2: input tanggal manual + terapkan */}
          <div className="flex flex-wrap items-center gap-2">
            <span className="text-xs text-slate-400">📅</span>
            <input
              type="date"
              value={inputFrom}
              max={inputTo}
              onChange={e => setInputFrom(e.target.value)}
              className="flex-1 rounded-lg border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-700 shadow-sm focus:outline-none focus:ring-2 focus:ring-orange-400 sm:flex-none"
            />
            <span className="text-xs text-slate-400">—</span>
            <input
              type="date"
              value={inputTo}
              min={inputFrom}
              max={TODAY}
              onChange={e => setInputTo(e.target.value)}
              className="flex-1 rounded-lg border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-700 shadow-sm focus:outline-none focus:ring-2 focus:ring-orange-400 sm:flex-none"
            />
            <button
              type="button"
              onClick={applyFilter}
              className="flex items-center gap-1.5 rounded-lg bg-orange-500 px-3 py-1.5 text-xs font-medium text-white shadow-sm transition hover:bg-orange-600 active:scale-95"
            >
              {loadingFiltered
                ? <span className="inline-block h-3 w-3 animate-spin rounded-full border-2 border-white/40 border-t-white" />
                : null}
              Terapkan
            </button>
          </div>
        </div>
      </div>

      {/* ── Alert stok menipis ── */}
      {isManager && !alertDismissed && lowStockList.length > 0 && (
        <AlertBanner type="warning" onDismiss={() => setAlertDismissed(true)}>
          Produk <strong>{lowStockList[0]?.name}</strong> stok menipis, sudah di bawah batas minimum.{' '}
          <strong>{lowStockList.length} produk</strong> perlu restock.
        </AlertBanner>
      )}

      {/* ── 2 KPI Cards (solid) ── */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <KpiCard
          variant="solid"
          label="Total Penjualan"
          value={money(totalPenjualan)}
          icon="💰"
          color="orange"
          sub={`${jumlahTransaksi} transaksi`}
          pct={pctChange(totalPenjualan, prevTotal)}
          pctLabel="vs periode sebelumnya"
        />
        <KpiCard
          variant="solid"
          label="Jumlah Transaksi"
          value={jumlahTransaksi}
          icon="🧾"
          color="teal"
          sub={`${jumlahVoid > 0 ? `${jumlahVoid} void · ` : ''}${jumlahItems} item terjual`}
          pct={pctChange(jumlahTransaksi, prevCount)}
          pctLabel="vs periode sebelumnya"
        />
      </div>

      {/* ── 4 Stat Cards (putih) — admin only ── */}
      {isManager && (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
          <StatCard label="Rata-rata per Transaksi" value={money(avgPerTx)}                   icon="💵" sub={`${labelDate(from)} – ${labelDate(to)}`} />
          <StatCard label="Item Terjual"            value={jumlahItems}                        icon="🛒" sub={`${labelDate(from)} – ${labelDate(to)}`} />
          <StatCard label="Stok Menipis"            value={lowStockList.length}                icon="⚠️" sub={lowStockList.length === 0 ? 'semua aman' : 'perlu restock'} />
          <StatCard label="Total Produk Aktif"      value={allProducts?.totalElements ?? '—'}  icon="🏷️" sub="di database" />
        </div>
      )}

      {/* ── Chart + Overall Info ── */}
      {isManager && (
        <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">

          {/* Bar chart */}
          <div className="col-span-2 rounded-xl bg-white shadow-sm">
            <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-100 px-5 py-4">
              <h2 className="text-sm font-semibold text-slate-700">📊 Grafik Penjualan</h2>
              <span className="rounded-full bg-orange-100 px-2.5 py-0.5 text-xs font-medium text-orange-700">
                {money(chartTotal)}
              </span>
            </div>
            <div className="flex items-center gap-4 px-5 pt-3 text-xs text-slate-500">
              <span className="flex items-center gap-1.5">
                <span className="inline-block h-3 w-3 rounded-sm bg-orange-500" />
                Total Penjualan
              </span>
              <span className="text-slate-300">|</span>
              <span className="text-slate-400">{labelDate(from)} — {labelDate(to)}</span>
            </div>
            <div className="h-56 w-full px-2 pb-4 pt-2">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData} margin={{ top: 4, right: 8, left: 0, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                  <XAxis dataKey="date" fontSize={10} tick={{ fill: '#94a3b8' }} axisLine={false} tickLine={false} />
                  <YAxis
                    fontSize={10}
                    tick={{ fill: '#94a3b8' }}
                    axisLine={false}
                    tickLine={false}
                    tickFormatter={v => v === 0 ? '0' : compact(v)}
                  />
                  <Tooltip content={<ChartTooltip />} cursor={{ fill: '#fff7ed' }} />
                  <Bar dataKey="total" fill="#f97316" radius={[4, 4, 0, 0]} maxBarSize={36} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Overall Info + Donut */}
          <div className="flex flex-col gap-4">
            <SectionCard title="Informasi Umum">
              <div className="grid grid-cols-3 divide-x divide-slate-100 px-2 py-4">
                {[
                  { icon: '🏷️', label: 'Kategori',  value: allCategories?.totalElements ?? '—' },
                  { icon: '📦', label: 'Produk',     value: allProducts?.totalElements   ?? '—' },
                  { icon: '👥', label: 'Pelanggan',  value: allCustomers?.totalElements  ?? '—' },
                ].map(item => (
                  <div key={item.label} className="flex flex-col items-center gap-1 px-2">
                    <span className="text-2xl">{item.icon}</span>
                    <span className="text-lg font-bold text-slate-800">{item.value}</span>
                    <span className="text-xs text-slate-500">{item.label}</span>
                  </div>
                ))}
              </div>
            </SectionCard>

            <SectionCard title="Overview Transaksi" className="flex-1">
              <div className="flex items-center justify-center gap-6 px-4 py-4">
                <div className="relative shrink-0">
                  <PieChart width={110} height={110}>
                    <Pie data={donutData} cx={55} cy={55} innerRadius={32} outerRadius={50} dataKey="value" strokeWidth={2} stroke="#fff">
                      {donutData.map((entry, i) => <Cell key={`c-${i}`} fill={entry.color} />)}
                    </Pie>
                  </PieChart>
                </div>
                <div className="space-y-3">
                  <div>
                    <p className="text-xl font-bold text-emerald-600">{jumlahTransaksi}</p>
                    <p className="text-xs text-slate-500">● Lunas</p>
                    {totalDonut > 0 && <p className="text-xs font-medium text-emerald-600">▲ {((jumlahTransaksi / totalDonut) * 100).toFixed(0)}%</p>}
                  </div>
                  <div>
                    <p className="text-xl font-bold text-red-500">{jumlahVoid}</p>
                    <p className="text-xs text-slate-500">● Void</p>
                    {totalDonut > 0 && jumlahVoid > 0 && <p className="text-xs font-medium text-red-500">▼ {((jumlahVoid / totalDonut) * 100).toFixed(0)}%</p>}
                  </div>
                </div>
              </div>
            </SectionCard>
          </div>
        </div>
      )}

      {/* ── Transaksi Terakhir + Stok Menipis ── */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">

        <SectionCard title="Transaksi Terakhir" badge="8 terbaru" className={isManager ? 'col-span-2' : 'col-span-3'}>
          {recentList.length === 0 ? (
            <p className="p-5 text-xs text-slate-400">Belum ada transaksi.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-slate-100 bg-slate-50 text-left">
                    <th className="px-5 py-2.5 text-xs font-semibold text-slate-500">Tanggal</th>
                    <th className="px-3 py-2.5 text-xs font-semibold text-slate-500">No. Invoice</th>
                    <th className="px-3 py-2.5 text-xs font-semibold text-slate-500">Kasir</th>
                    <th className="px-3 py-2.5 text-xs font-semibold text-slate-500">Status</th>
                    <th className="px-5 py-2.5 text-right text-xs font-semibold text-slate-500">Total</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-50">
                  {recentList.map(s => (
                    <tr key={s.id} className="transition-colors hover:bg-slate-50/50">
                      <td className="px-5 py-3 text-xs text-slate-500">{formatDate(s.saleDate)}</td>
                      <td className="px-3 py-3 font-mono text-xs text-slate-700">{s.invoiceNumber || '-'}</td>
                      <td className="px-3 py-3 text-xs text-slate-600">{s.cashierName || '-'}</td>
                      <td className="px-3 py-3"><StatusBadge status={s.status} /></td>
                      <td className="px-5 py-3 text-right text-sm font-bold text-slate-800">{money(s.total)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </SectionCard>

        {isManager && (
          <div className="space-y-4">
            {/* Top produk */}
            <SectionCard
              title="🏆 Produk Terlaris"
              badge={<span className="rounded-full bg-orange-100 px-2 py-0.5 text-xs font-medium text-orange-700">{labelDate(from)} – {labelDate(to)}</span>}
            >
              {topProducts.length === 0 ? (
                <p className="px-5 py-4 text-xs text-slate-400">Belum ada data.</p>
              ) : (
                <ul className="divide-y divide-slate-50">
                  {topProducts.map((p, i) => (
                    <li key={p.name} className="flex items-center gap-3 px-5 py-2.5">
                      <span className={`flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-xs font-bold ${
                        i === 0 ? 'bg-orange-500 text-white' : i === 1 ? 'bg-slate-300 text-slate-700' : 'bg-slate-100 text-slate-500'
                      }`}>{i + 1}</span>
                      <div className="min-w-0 flex-1">
                        <p className="truncate text-xs font-medium text-slate-700">{p.name}</p>
                        <p className="text-xs text-slate-400">{money(p.total)}</p>
                      </div>
                      <span className="shrink-0 rounded-full bg-orange-50 px-2 py-0.5 text-xs font-semibold text-orange-700">{p.qty}×</span>
                    </li>
                  ))}
                </ul>
              )}
            </SectionCard>

            {/* Stok menipis */}
            <SectionCard
              title="⚠️ Stok Menipis"
              badge={<span className="rounded-full bg-amber-100 px-2 py-0.5 text-xs font-semibold text-amber-700">{lowStockList.length}</span>}
            >
              {lowStockList.length === 0 ? (
                <p className="px-5 py-3 text-xs text-emerald-600">✅ Semua stok aman.</p>
              ) : (
                <ul className="divide-y divide-slate-50">
                  {lowStockList.slice(0, 5).map(p => (
                    <li key={p.id} className="flex items-center justify-between gap-2 px-5 py-2.5">
                      <div className="min-w-0">
                        <p className="truncate text-xs font-medium text-slate-700">{p.name}</p>
                        <p className="text-xs text-slate-400">{p.category?.name}</p>
                      </div>
                      <span className="shrink-0 text-right text-xs font-semibold text-red-600">
                        {p.measured
                          ? `${new Intl.NumberFormat('id-ID', { maximumFractionDigits: 1 }).format(p.materialStock)} ${p.category?.baseUnit}`
                          : `${p.stock} ${p.unit?.symbol || ''}`}
                      </span>
                    </li>
                  ))}
                </ul>
              )}
            </SectionCard>
          </div>
        )}
      </div>
    </div>
  )
}
