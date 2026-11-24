import { useQuery } from '@tanstack/react-query'
import { useMemo, useState } from 'react'
import { reportsApi } from '../../api/reports'
import { expensesApi } from '../../api/resources'
import { errorMessage } from '../../api/client'
import { KpiCard, SectionCard } from '../../components/ui'

/* ── helpers ── */
const _now  = new Date()
const TODAY = _now.toISOString().slice(0, 10)
const MONTH_START = new Date(_now.getFullYear(), _now.getMonth(), 1).toISOString().slice(0, 10)
const MONTH_NAMES = ['Jan','Feb','Mar','Apr','Mei','Jun','Jul','Ags','Sep','Okt','Nov','Des']

function money(v) {
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(v || 0)
}
function isoDate(d) { return d.toISOString().slice(0, 10) }

function monthRange(year, month) {
  const from = isoDate(new Date(year, month, 1))
  const last  = new Date(year, month + 1, 0)
  const to    = isoDate(last > _now ? _now : last)
  return { from, to }
}

const DEFAULT_YEAR  = _now.getFullYear()
const DEFAULT_MONTH = _now.getMonth()

export default function ReportsPage() {
  /* ── filter state ── */
  const [selYear,   setSelYear]   = useState(DEFAULT_YEAR)
  const [selMonth,  setSelMonth]  = useState(DEFAULT_MONTH)
  const [inputFrom, setInputFrom] = useState(MONTH_START)
  const [inputTo,   setInputTo]   = useState(TODAY)
  const [from,      setFrom]      = useState(MONTH_START)
  const [to,        setTo]        = useState(TODAY)

  function selectMonth(year, month) {
    const { from: f, to: t } = monthRange(year, month)
    setSelYear(year); setSelMonth(month)
    setInputFrom(f); setInputTo(t)
    setFrom(f); setTo(t)
  }
  function applyFilter() {
    if (inputFrom && inputTo && inputFrom <= inputTo) { setFrom(inputFrom); setTo(inputTo) }
  }

  /* ── CSV state ── */
  const [busyKey, setBusyKey] = useState('')
  const [csvError, setCsvError] = useState('')

  async function runCsv(key, action) {
    setCsvError(''); setBusyKey(key)
    try { await action() }
    catch (err) { setCsvError(errorMessage(err, 'Gagal membuat file CSV')) }
    finally { setBusyKey('') }
  }

  /* ── query ringkasan keuangan ── */
  const { data: summary, isFetching: loadingSummary } = useQuery({
    queryKey: ['financial-summary', from, to],
    queryFn:  () => expensesApi.summary({ from, to }),
  })

  const totalMasuk  = useMemo(() => Number(summary?.totalMasuk  || 0), [summary])
  const totalKeluar = useMemo(() => Number(summary?.totalKeluar || 0), [summary])
  const selisih     = useMemo(() => Number(summary?.selisih     || 0), [summary])
  const saldo       = useMemo(() => Number(summary?.saldo       || 0), [summary])

  const rangeLabel = `${new Date(from).toLocaleDateString('id-ID',{day:'numeric',month:'short',year:'numeric'})} — ${new Date(to).toLocaleDateString('id-ID',{day:'numeric',month:'short',year:'numeric'})}`

  /* ── CSV reports ── */
  const csvReports = [
    { key: 'summary', title: 'Ringkasan Penjualan',    desc: 'Satu baris per transaksi (invoice, kasir, total) dalam rentang tanggal.', action: () => reportsApi.salesSummaryCsv(from, to) },
    { key: 'detail',  title: 'Rincian Item Terjual',   desc: 'Satu baris per produk yang terjual — untuk analisis produk terlaris.',   action: () => reportsApi.salesDetailCsv(from, to) },
    { key: 'stock',   title: 'Stok Produk Saat Ini',   desc: 'Daftar seluruh produk beserta harga dan stok — untuk audit inventori.',  action: () => reportsApi.productStockCsv() },
  ]

  /* ═══════════════════════════ RENDER ═══════════════════════════ */
  return (
    <div className="space-y-5">

      {/* Header + filter */}
      <div className="flex flex-wrap items-start justify-between gap-3">
        <h1 className="text-xl font-semibold text-slate-800">Laporan Keuangan</h1>

        {/* Filter bulan + tanggal manual */}
        <div className="w-full flex flex-col gap-2 rounded-xl bg-slate-100 px-3 py-2.5 sm:w-auto">
          <div className="flex items-center gap-1.5 overflow-x-auto pb-0.5">
            <button type="button" onClick={() => setSelYear(y => y - 1)}
              className="flex h-6 w-6 shrink-0 items-center justify-center rounded-md text-slate-500 hover:bg-slate-200">‹</button>
            <span className="min-w-[3.5rem] shrink-0 text-center text-xs font-semibold text-slate-600">{selYear}</span>
            <button type="button" onClick={() => setSelYear(y => Math.min(y + 1, DEFAULT_YEAR))}
              disabled={selYear >= DEFAULT_YEAR}
              className="flex h-6 w-6 shrink-0 items-center justify-center rounded-md text-slate-500 hover:bg-slate-200 disabled:opacity-30">›</button>
            <span className="mx-1 h-4 w-px shrink-0 bg-slate-300" />
            {MONTH_NAMES.map((name, idx) => {
              const isFuture = selYear === DEFAULT_YEAR && idx > DEFAULT_MONTH
              const isActive = idx === selMonth && from === monthRange(selYear, idx).from
              return (
                <button key={name} type="button" disabled={isFuture}
                  onClick={() => selectMonth(selYear, idx)}
                  className={`shrink-0 rounded-md px-2 py-1 text-xs font-medium transition-all ${
                    isActive ? 'bg-orange-500 text-white shadow-sm'
                    : isFuture ? 'cursor-not-allowed text-slate-300'
                    : 'text-slate-500 hover:bg-slate-200'}`}>
                  {name}
                </button>
              )
            })}
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <span className="text-xs text-slate-400">📅</span>
            <input type="date" value={inputFrom} max={inputTo} onChange={e => setInputFrom(e.target.value)}
              className="flex-1 rounded-lg border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-700 shadow-sm focus:outline-none focus:ring-2 focus:ring-orange-400 sm:flex-none" />
            <span className="text-xs text-slate-400">—</span>
            <input type="date" value={inputTo} min={inputFrom} max={TODAY} onChange={e => setInputTo(e.target.value)}
              className="flex-1 rounded-lg border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-700 shadow-sm focus:outline-none focus:ring-2 focus:ring-orange-400 sm:flex-none" />
            <button type="button" onClick={applyFilter}
              className="flex items-center gap-1.5 rounded-lg bg-orange-500 px-3 py-1.5 text-xs font-medium text-white shadow-sm hover:bg-orange-600">
              {loadingSummary
                ? <span className="inline-block h-3 w-3 animate-spin rounded-full border-2 border-white/40 border-t-white" />
                : null}
              Terapkan
            </button>
          </div>
        </div>
      </div>

      {/* 4 card ringkasan keuangan */}
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <KpiCard
          variant="solid"
          label="Total Masuk"
          value={money(totalMasuk)}
          icon="💰"
          color="emerald"
          sub={rangeLabel}
        />
        <KpiCard
          variant="solid"
          label="Total Keluar"
          value={money(totalKeluar)}
          icon="💸"
          color="amber"
          sub={rangeLabel}
        />
        <KpiCard
          variant="solid"
          label="Selisih"
          value={money(selisih)}
          icon={selisih >= 0 ? '📈' : '📉'}
          color={selisih >= 0 ? 'teal' : 'violet'}
          sub="Masuk − Keluar"
        />
        <KpiCard
          variant="solid"
          label="Saldo Sekarang"
          value={money(saldo)}
          icon="🏦"
          color={saldo >= 0 ? 'blue' : 'violet'}
          sub="Kumulatif all-time"
        />
      </div>

      {/* Unduh CSV */}
      <SectionCard title="📥 Unduh Laporan CSV" badge={<span className="text-xs text-slate-400">{rangeLabel}</span>}>
        {csvError && (
          <div className="mx-5 mt-4 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{csvError}</div>
        )}
        <div className="grid grid-cols-1 gap-4 p-5 md:grid-cols-3">
          {csvReports.map(r => (
            <div key={r.key} className="flex flex-col rounded-xl border border-slate-100 bg-slate-50 p-4">
              <h3 className="mb-1 text-sm font-semibold text-slate-800">{r.title}</h3>
              <p className="mb-4 flex-1 text-xs text-slate-500">{r.desc}</p>
              <button
                type="button"
                onClick={() => runCsv(r.key, r.action)}
                disabled={busyKey === r.key}
                className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-60"
              >
                {busyKey === r.key ? 'Menyiapkan…' : '⬇ Unduh CSV'}
              </button>
            </div>
          ))}
        </div>
      </SectionCard>
    </div>
  )
}
