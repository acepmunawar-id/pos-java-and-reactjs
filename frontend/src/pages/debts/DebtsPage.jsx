import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { debtsApi } from '../../api/resources'
import { errorMessage } from '../../api/client'

function money(v) {
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(v || 0)
}

function fmtDate(iso) {
  return new Date(iso).toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric' })
}

function PayModal({ sale, onClose }) {
  const [amount, setAmount] = useState(String(sale.remaining))
  const [notes, setNotes]   = useState('')
  const [err, setErr]       = useState('')
  const qc = useQueryClient()

  const { mutate, isPending } = useMutation({
    mutationFn: () => debtsApi.pay({ saleId: sale.saleId, amount: Number(amount), notes: notes || null }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['debts'] }); onClose() },
    onError: (e) => setErr(errorMessage(e, 'Gagal mencatat pembayaran')),
  })

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/50 p-4 sm:items-center">
      <div className="w-full max-w-sm rounded-2xl bg-white shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="bg-gradient-to-br from-emerald-500 to-emerald-600 px-6 py-5 text-center">
          <div className="mx-auto mb-2 flex h-14 w-14 items-center justify-center rounded-full bg-white/20">
            <span className="text-3xl">💳</span>
          </div>
          <h3 className="text-lg font-bold text-white">Bayar Hutang</h3>
          <p className="mt-1 text-sm text-emerald-100">{sale.invoiceNumber}</p>
        </div>

        <div className="px-6 py-5 space-y-3">
          <div className="rounded-lg bg-slate-50 px-4 py-3 text-sm space-y-1">
            <div className="flex justify-between text-slate-500"><span>Total transaksi</span><span className="font-medium text-slate-800">{money(sale.total)}</span></div>
            <div className="flex justify-between text-slate-500"><span>Sudah dibayar</span><span className="font-medium text-slate-800">{money(sale.totalPaid)}</span></div>
            <div className="flex justify-between border-t border-slate-200 pt-1 text-orange-700 font-semibold"><span>Sisa hutang</span><span>{money(sale.remaining)}</span></div>
          </div>

          <div>
            <label className="mb-1 block text-xs font-medium text-slate-600">Jumlah Bayar</label>
            <input type="number" min="0.01" max={sale.remaining} value={amount}
              onChange={e => setAmount(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none" />
          </div>
          <div>
            <label className="mb-1 block text-xs font-medium text-slate-600">Catatan (opsional)</label>
            <input type="text" value={notes} onChange={e => setNotes(e.target.value)} placeholder="misal: bayar lewat transfer"
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none" />
          </div>

          {err && <p className="rounded-lg bg-red-50 px-3 py-2 text-xs text-red-600">{err}</p>}

          <div className="flex gap-3 pt-1">
            <button onClick={onClose} disabled={isPending}
              className="flex-1 rounded-xl border border-slate-200 py-2.5 text-sm font-semibold text-slate-600 hover:bg-slate-50 transition">
              Batal
            </button>
            <button onClick={() => mutate()} disabled={isPending || !amount || Number(amount) <= 0}
              className="flex-1 rounded-xl bg-emerald-600 py-2.5 text-sm font-bold text-white hover:bg-emerald-700 disabled:opacity-60 transition active:scale-95">
              {isPending ? 'Menyimpan…' : 'Konfirmasi'}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default function DebtsPage() {
  const [payTarget, setPayTarget] = useState(null)
  const [expanded, setExpanded]   = useState({})

  const { data: debtList = [], isLoading } = useQuery({
    queryKey: ['debts'],
    queryFn: debtsApi.list,
  })

  const totalAllDebt = debtList.reduce((s, c) => s + Number(c.totalDebt), 0)

  function toggleExpand(id) {
    setExpanded(p => ({ ...p, [id]: !p[id] }))
  }

  return (
    <div className="space-y-5">
      <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-xl font-semibold text-slate-800">Hutang Pelanggan</h1>
        {debtList.length > 0 && (
          <div className="rounded-xl bg-orange-50 border border-orange-200 px-4 py-2 text-sm font-semibold text-orange-700">
            Total hutang: {money(totalAllDebt)}
          </div>
        )}
      </div>

      {isLoading && (
        <div className="rounded-xl bg-white p-8 text-center text-slate-400 shadow-sm">Memuat data…</div>
      )}

      {!isLoading && debtList.length === 0 && (
        <div className="rounded-xl bg-white p-10 text-center shadow-sm">
          <div className="text-4xl mb-3">🎉</div>
          <p className="font-semibold text-slate-700">Tidak ada hutang</p>
          <p className="text-sm text-slate-400 mt-1">Semua pelanggan sudah melunasi transaksinya</p>
        </div>
      )}

      {debtList.map(customer => (
        <div key={customer.customerId} className="rounded-xl bg-white shadow-sm overflow-hidden">
          {/* Header pelanggan */}
          <button
            onClick={() => toggleExpand(customer.customerId)}
            className="flex w-full items-center justify-between px-5 py-4 hover:bg-slate-50 transition"
          >
            <div className="flex items-center gap-3 text-left">
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-orange-100 text-lg">👤</div>
              <div>
                <p className="font-semibold text-slate-800">{customer.customerName}</p>
                {customer.customerPhone && <p className="text-xs text-slate-400">{customer.customerPhone}</p>}
              </div>
            </div>
            <div className="flex items-center gap-3">
              <div className="text-right">
                <p className="text-xs text-slate-400">{customer.sales.length} transaksi</p>
                <p className="text-sm font-bold text-orange-600">{money(customer.totalDebt)}</p>
              </div>
              <span className="text-slate-400 text-lg">{expanded[customer.customerId] ? '▲' : '▼'}</span>
            </div>
          </button>

          {/* Detail transaksi */}
          {expanded[customer.customerId] && (
            <div className="border-t border-slate-100 divide-y divide-slate-50">
              {customer.sales.map(sale => (
                <div key={sale.saleId} className="flex items-center justify-between gap-3 px-5 py-3">
                  <div className="min-w-0">
                    <p className="text-sm font-medium text-slate-700">{sale.invoiceNumber}</p>
                    <p className="text-xs text-slate-400">{fmtDate(sale.saleDate)}</p>
                    <div className="mt-0.5 flex gap-3 text-xs text-slate-500">
                      <span>Total: <strong>{money(sale.total)}</strong></span>
                      {sale.totalPaid > 0 && <span>Bayar: <strong className="text-emerald-600">{money(sale.totalPaid)}</strong></span>}
                    </div>
                  </div>
                  <div className="flex items-center gap-3 shrink-0">
                    <div className="text-right">
                      <p className="text-xs text-slate-400">Sisa</p>
                      <p className="text-sm font-bold text-orange-600">{money(sale.remaining)}</p>
                    </div>
                    <button
                      onClick={() => setPayTarget(sale)}
                      className="rounded-lg bg-emerald-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-emerald-700 transition active:scale-95"
                    >
                      Lunasi
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      ))}

      {payTarget && <PayModal sale={payTarget} onClose={() => setPayTarget(null)} />}
    </div>
  )
}
