import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useMemo, useState } from 'react'
import { expensesApi } from '../../api/resources'
import { errorMessage } from '../../api/client'
import { SectionCard, AlertBanner } from '../../components/ui'

/* ── helpers ── */
const _now   = new Date()
const TODAY  = _now.toISOString().slice(0, 10)
const MONTH_START = new Date(_now.getFullYear(), _now.getMonth(), 1).toISOString().slice(0, 10)

function money(v) {
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(v || 0)
}
function fmtDate(str) {
  if (!str) return '-'
  return new Date(str).toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric' })
}

const EMPTY_FORM = {
  expenseDate: TODAY,
  categoryId:  '',
  description: '',
  amount:      '',
  supplierId:  '',
}

export default function ExpensesPage() {
  const qc = useQueryClient()

  /* ── filter ── */
  const [filterFrom, setFilterFrom] = useState(MONTH_START)
  const [filterTo,   setFilterTo]   = useState(TODAY)
  const [filterCat,  setFilterCat]  = useState('')
  const [page,       setPage]       = useState(0)

  /* ── form / modal ── */
  const [modalOpen,  setModalOpen]  = useState(false)
  const [editingId,  setEditingId]  = useState(null)
  const [form,       setForm]       = useState(EMPTY_FORM)
  const [formError,  setFormError]  = useState('')

  /* ── queries ── */
  const { data: categories = [] } = useQuery({
    queryKey: ['expense-categories'],
    queryFn:  expensesApi.categories,
  })

  const { data: expPage, isFetching } = useQuery({
    queryKey: ['expenses', filterFrom, filterTo, filterCat, page],
    queryFn:  () => expensesApi.list({
      from:       filterFrom,
      to:         filterTo,
      categoryId: filterCat || undefined,
      page,
      size: 15,
    }),
  })

  const expenses   = useMemo(() => expPage?.content   || [], [expPage])
  const totalPages = expPage?.totalPages ?? 1
  const totalItems = expPage?.totalElements ?? 0

  /* ── mutations ── */
  function invalidate() { qc.invalidateQueries({ queryKey: ['expenses'] }) }

  const saveMut = useMutation({
    mutationFn: (payload) => editingId
      ? expensesApi.update(editingId, payload)
      : expensesApi.create(payload),
    onSuccess: () => { invalidate(); setModalOpen(false) },
    onError: (err) => setFormError(errorMessage(err, 'Gagal menyimpan pengeluaran')),
  })

  const deleteMut = useMutation({
    mutationFn: expensesApi.remove,
    onSuccess: invalidate,
  })

  /* ── handlers ── */
  function openCreate() {
    setEditingId(null)
    setForm(EMPTY_FORM)
    setFormError('')
    setModalOpen(true)
  }

  function openEdit(row) {
    setEditingId(row.id)
    setForm({
      expenseDate: row.expenseDate,
      categoryId:  row.categoryId,
      description: row.description || '',
      amount:      String(row.amount),
      supplierId:  row.supplierId || '',
    })
    setFormError('')
    setModalOpen(true)
  }

  function handleChange(e) {
    setForm(f => ({ ...f, [e.target.name]: e.target.value }))
  }

  function handleSubmit(e) {
    e.preventDefault()
    setFormError('')
    saveMut.mutate({
      expenseDate: form.expenseDate,
      categoryId:  form.categoryId,
      description: form.description || null,
      amount:      parseFloat(form.amount),
      supplierId:  form.supplierId || null,
    })
  }

  function confirmDelete(row) {
    if (window.confirm(`Hapus pengeluaran "${row.description || row.categoryName}" ?`)) {
      deleteMut.mutate(row.id)
    }
  }

  /* ═══════════════════════ RENDER ═══════════════════════ */
  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-xl font-semibold text-slate-800">Pengeluaran</h1>
        <button
          type="button"
          onClick={openCreate}
          className="rounded-lg bg-orange-500 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-orange-600"
        >
          + Catat Pengeluaran
        </button>
      </div>

      {/* Filter */}
      <div className="flex flex-wrap items-center gap-2 rounded-xl bg-white px-4 py-3 shadow-sm text-sm">
        <span className="font-medium text-slate-500">📅 Filter</span>
        <input type="date" value={filterFrom} onChange={e => { setFilterFrom(e.target.value); setPage(0) }}
          className="rounded-lg border border-slate-200 px-2.5 py-1.5 text-xs text-slate-700 focus:outline-none focus:ring-2 focus:ring-orange-400" />
        <span className="text-slate-400">—</span>
        <input type="date" value={filterTo} onChange={e => { setFilterTo(e.target.value); setPage(0) }}
          className="rounded-lg border border-slate-200 px-2.5 py-1.5 text-xs text-slate-700 focus:outline-none focus:ring-2 focus:ring-orange-400" />
        <select value={filterCat} onChange={e => { setFilterCat(e.target.value); setPage(0) }}
          className="rounded-lg border border-slate-200 px-2.5 py-1.5 text-xs text-slate-700 focus:outline-none focus:ring-2 focus:ring-orange-400">
          <option value="">Semua Kategori</option>
          {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <span className="ml-auto text-xs text-slate-400">{totalItems} data</span>
      </div>

      {/* Tabel */}
      <SectionCard>
        {isFetching && <div className="px-5 py-2 text-xs text-slate-400">Memuat…</div>}
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-100 bg-slate-50 text-left">
                <th className="px-5 py-3 text-xs font-semibold text-slate-500">Tanggal</th>
                <th className="px-3 py-3 text-xs font-semibold text-slate-500">Kategori</th>
                <th className="px-3 py-3 text-xs font-semibold text-slate-500">Keterangan</th>
                <th className="px-3 py-3 text-xs font-semibold text-slate-500">Supplier</th>
                <th className="px-5 py-3 text-right text-xs font-semibold text-slate-500">Jumlah</th>
                <th className="px-3 py-3 text-xs font-semibold text-slate-500"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50">
              {expenses.length === 0 ? (
                <tr><td colSpan={6} className="px-5 py-8 text-center text-sm text-slate-400">Belum ada data pengeluaran.</td></tr>
              ) : expenses.map(row => (
                <tr key={row.id} className="hover:bg-slate-50/60">
                  <td className="px-5 py-3 text-xs text-slate-500">{fmtDate(row.expenseDate)}</td>
                  <td className="px-3 py-3">
                    <span className="rounded-full bg-orange-100 px-2 py-0.5 text-xs font-medium text-orange-700">
                      {row.categoryName}
                    </span>
                  </td>
                  <td className="px-3 py-3 text-xs text-slate-700">{row.description || '-'}</td>
                  <td className="px-3 py-3 text-xs text-slate-500">{row.supplierName || '-'}</td>
                  <td className="px-5 py-3 text-right text-sm font-semibold text-slate-800">{money(row.amount)}</td>
                  <td className="px-3 py-3">
                    <div className="flex items-center gap-1">
                      <button type="button" onClick={() => openEdit(row)}
                        className="rounded px-2 py-1 text-xs text-blue-600 hover:bg-blue-50">Edit</button>
                      <button type="button" onClick={() => confirmDelete(row)}
                        className="rounded px-2 py-1 text-xs text-red-500 hover:bg-red-50">Hapus</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex items-center justify-center gap-2 border-t border-slate-100 px-5 py-3">
            <button type="button" disabled={page === 0} onClick={() => setPage(p => p - 1)}
              className="rounded-lg border border-slate-200 px-3 py-1 text-xs disabled:opacity-40 hover:bg-slate-50">‹ Sebelumnya</button>
            <span className="text-xs text-slate-500">{page + 1} / {totalPages}</span>
            <button type="button" disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}
              className="rounded-lg border border-slate-200 px-3 py-1 text-xs disabled:opacity-40 hover:bg-slate-50">Berikutnya ›</button>
          </div>
        )}
      </SectionCard>

      {/* Modal tambah/edit */}
      {modalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="w-full max-w-md rounded-2xl bg-white shadow-xl">
            <div className="flex items-center justify-between border-b border-slate-100 px-6 py-4">
              <h2 className="text-base font-semibold text-slate-800">
                {editingId ? 'Edit Pengeluaran' : 'Catat Pengeluaran'}
              </h2>
              <button type="button" onClick={() => setModalOpen(false)}
                className="text-slate-400 hover:text-slate-600">✕</button>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4 px-6 py-5">
              {formError && <AlertBanner type="error">{formError}</AlertBanner>}

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="mb-1 block text-xs font-medium text-slate-600">Tanggal *</label>
                  <input type="date" name="expenseDate" value={form.expenseDate} onChange={handleChange} required
                    className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-orange-400" />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-medium text-slate-600">Jumlah (Rp) *</label>
                  <input type="number" name="amount" value={form.amount} onChange={handleChange} required min="1" step="500"
                    placeholder="0"
                    className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-orange-400" />
                </div>
              </div>

              <div>
                <label className="mb-1 block text-xs font-medium text-slate-600">Kategori *</label>
                <select name="categoryId" value={form.categoryId} onChange={handleChange} required
                  className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-orange-400">
                  <option value="">— Pilih kategori —</option>
                  {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>

              <div>
                <label className="mb-1 block text-xs font-medium text-slate-600">Keterangan</label>
                <input type="text" name="description" value={form.description} onChange={handleChange}
                  placeholder="mis. Biang Dior Sauvage 50ml"
                  className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-orange-400" />
              </div>

              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setModalOpen(false)}
                  className="flex-1 rounded-lg border border-slate-200 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50">
                  Batal
                </button>
                <button type="submit" disabled={saveMut.isPending}
                  className="flex-1 rounded-lg bg-orange-500 py-2 text-sm font-medium text-white hover:bg-orange-600 disabled:opacity-60">
                  {saveMut.isPending ? 'Menyimpan…' : 'Simpan'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
