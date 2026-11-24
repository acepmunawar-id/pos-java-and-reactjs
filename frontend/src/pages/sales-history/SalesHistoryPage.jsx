import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { listSales, getReceiptView, voidSale } from '../../api/sales'
import { errorMessage } from '../../api/client'
import { useAuth } from '../../auth/AuthContext'
import ReceiptModal from '../../components/ReceiptModal'

function money(value) {
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(
    value || 0,
  )
}

function isoDate(date) {
  return date.toISOString().slice(0, 10)
}

export default function SalesHistoryPage() {
  const { hasRole } = useAuth()
  const canVoid = hasRole('SUPER_ADMIN', 'ADMIN')
  const queryClient = useQueryClient()

  const [from, setFrom] = useState(isoDate(new Date(Date.now() - 6 * 24 * 60 * 60 * 1000)))
  const [to, setTo] = useState(isoDate(new Date()))
  const [page, setPage] = useState(0)
  const [receiptState, setReceiptState] = useState(null)

  const { data, isLoading } = useQuery({
    queryKey: ['sales-history', from, to, page],
    queryFn: () => listSales({ from, to, page, size: 10 }),
  })

  async function viewReceipt(saleId) {
    const receipt = await getReceiptView(saleId)
    setReceiptState({ saleId, receipt })
  }

  async function handleVoid(sale) {
    if (!window.confirm(`Batalkan transaksi ${sale.invoiceNumber}? Stok produk akan dikembalikan.`)) return
    try {
      await voidSale(sale.id)
      queryClient.invalidateQueries({ queryKey: ['sales-history'] })
    } catch (err) {
      window.alert(errorMessage(err, 'Gagal membatalkan transaksi'))
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-xl font-semibold text-slate-800">Riwayat Transaksi</h1>
        <div className="flex flex-wrap items-center gap-2 text-sm">
          <input
            type="date"
            value={from}
            onChange={(e) => setFrom(e.target.value)}
            className="flex-1 rounded-lg border border-slate-300 px-2 py-1.5 text-xs sm:flex-none sm:text-sm"
          />
          <span className="text-slate-400">s/d</span>
          <input
            type="date"
            value={to}
            onChange={(e) => setTo(e.target.value)}
            className="flex-1 rounded-lg border border-slate-300 px-2 py-1.5 text-xs sm:flex-none sm:text-sm"
          />
        </div>
      </div>

      <div className="overflow-x-auto rounded-xl bg-white shadow-sm">
        <table className="min-w-full divide-y divide-slate-200 text-sm">
          <thead className="bg-slate-50">
            <tr>
              <th className="px-4 py-3 text-left font-medium text-slate-600">No Invoice</th>
              <th className="px-4 py-3 text-left font-medium text-slate-600">Tanggal</th>
              <th className="px-4 py-3 text-left font-medium text-slate-600">Kasir</th>
              <th className="px-4 py-3 text-right font-medium text-slate-600">Total</th>
              <th className="px-4 py-3 text-left font-medium text-slate-600">Status</th>
              <th className="px-4 py-3 text-right font-medium text-slate-600">Aksi</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {isLoading && (
              <tr>
                <td colSpan={6} className="px-4 py-6 text-center text-slate-400">
                  Memuat data...
                </td>
              </tr>
            )}
            {!isLoading && data?.content?.length === 0 && (
              <tr>
                <td colSpan={6} className="px-4 py-6 text-center text-slate-400">
                  Belum ada transaksi pada rentang tanggal ini
                </td>
              </tr>
            )}
            {data?.content?.map((sale) => (
              <tr key={sale.id} className="hover:bg-slate-50">
                <td className="px-4 py-3 font-medium text-slate-700">{sale.invoiceNumber}</td>
                <td className="px-4 py-3 text-slate-600">
                  {new Date(sale.saleDate).toLocaleString('id-ID')}
                </td>
                <td className="px-4 py-3 text-slate-600">{sale.cashierName}</td>
                <td className="px-4 py-3 text-right font-medium">{money(sale.total)}</td>
                <td className="px-4 py-3">
                  <span
                    className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                      sale.status === 'VOID' ? 'bg-red-100 text-red-700' : 'bg-emerald-100 text-emerald-700'
                    }`}
                  >
                    {sale.status === 'VOID' ? 'Dibatalkan' : 'Selesai'}
                  </span>
                </td>
                <td className="px-4 py-3 text-right">
                  <button onClick={() => viewReceipt(sale.id)} className="mr-3 text-emerald-600 hover:underline">
                    Lihat Struk
                  </button>
                  {canVoid && sale.status !== 'VOID' && (
                    <button onClick={() => handleVoid(sale)} className="text-red-600 hover:underline">
                      Batalkan
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-between text-sm text-slate-600">
          <span>
            Halaman {data.pageNumber + 1} dari {data.totalPages}
          </span>
          <div className="flex gap-2">
            <button
              disabled={data.pageNumber === 0}
              onClick={() => setPage(data.pageNumber - 1)}
              className="rounded-md border border-slate-300 px-3 py-1 disabled:opacity-40"
            >
              Sebelumnya
            </button>
            <button
              disabled={data.last}
              onClick={() => setPage(data.pageNumber + 1)}
              className="rounded-md border border-slate-300 px-3 py-1 disabled:opacity-40"
            >
              Berikutnya
            </button>
          </div>
        </div>
      )}

      {receiptState && (
        <ReceiptModal
          saleId={receiptState.saleId}
          receipt={receiptState.receipt}
          onClose={() => setReceiptState(null)}
        />
      )}
    </div>
  )
}
