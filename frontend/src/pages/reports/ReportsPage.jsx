import { useState } from 'react'
import { reportsApi } from '../../api/reports'
import { errorMessage } from '../../api/client'

function isoDate(date) {
  return date.toISOString().slice(0, 10)
}

export default function ReportsPage() {
  const [from, setFrom] = useState(isoDate(new Date(Date.now() - 29 * 24 * 60 * 60 * 1000)))
  const [to, setTo] = useState(isoDate(new Date()))
  const [busyKey, setBusyKey] = useState('')
  const [error, setError] = useState('')

  async function run(key, action) {
    setError('')
    setBusyKey(key)
    try {
      await action()
    } catch (err) {
      setError(errorMessage(err, 'Gagal membuat file CSV'))
    } finally {
      setBusyKey('')
    }
  }

  const reports = [
    {
      key: 'summary',
      title: 'Ringkasan Penjualan',
      description: 'Satu baris per transaksi (invoice, kasir, subtotal, diskon, pajak, total) dalam rentang tanggal.',
      action: () => reportsApi.salesSummaryCsv(from, to),
    },
    {
      key: 'detail',
      title: 'Rincian Item Terjual',
      description: 'Satu baris per produk yang terjual dalam rentang tanggal - untuk analisis produk terlaris.',
      action: () => reportsApi.salesDetailCsv(from, to),
    },
    {
      key: 'stock',
      title: 'Stok Produk Saat Ini',
      description: 'Daftar seluruh produk beserta harga dan stok saat ini - untuk audit inventori.',
      action: () => reportsApi.productStockCsv(),
    },
  ]

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold text-slate-800">Laporan</h1>

      <div className="flex flex-wrap items-center gap-2 rounded-xl bg-white p-4 text-sm shadow-sm">
        <span className="text-slate-600">Rentang tanggal (untuk laporan penjualan):</span>
        <input
          type="date"
          value={from}
          onChange={(e) => setFrom(e.target.value)}
          className="rounded-lg border border-slate-300 px-2 py-1.5"
        />
        <span className="text-slate-400">s/d</span>
        <input
          type="date"
          value={to}
          onChange={(e) => setTo(e.target.value)}
          className="rounded-lg border border-slate-300 px-2 py-1.5"
        />
      </div>

      {error && <div className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</div>}

      <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
        {reports.map((report) => (
          <div key={report.key} className="flex flex-col rounded-xl bg-white p-5 shadow-sm">
            <h2 className="mb-1 text-sm font-semibold text-slate-800">{report.title}</h2>
            <p className="mb-4 flex-1 text-xs text-slate-500">{report.description}</p>
            <button
              onClick={() => run(report.key, report.action)}
              disabled={busyKey === report.key}
              className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-60"
            >
              {busyKey === report.key ? 'Menyiapkan...' : '⬇ Unduh CSV'}
            </button>
          </div>
        ))}
      </div>
    </div>
  )
}
