import { useState } from 'react'
import { useCrud } from '../hooks/useCrud'
import { errorMessage } from '../api/client'
import Modal from './Modal'

/**
 * Generic master-data screen: search box, paginated table, and an add/edit
 * modal - reused by Products, Categories, Units, Customers, Suppliers,
 * Taxes, Discounts and Users so each page only supplies its columns and
 * form fields instead of re-building the CRUD scaffolding. `toPayload` lets a
 * page reshape the form state (e.g. parse numbers) right before it is sent.
 */
export default function CrudPage({
  title,
  resourceApi,
  queryKey,
  columns,
  emptyForm,
  renderForm,
  canWrite = true,
  toPayload = (form) => form,
}) {
  const crud = useCrud(resourceApi, queryKey)
  const [modalOpen, setModalOpen] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [formError, setFormError] = useState('')

  const page = crud.data

  function openCreate() {
    setEditingId(null)
    setForm(emptyForm)
    setFormError('')
    setModalOpen(true)
  }

  function openEdit(row) {
    setEditingId(row.id)
    setForm({ ...emptyForm, ...row })
    setFormError('')
    setModalOpen(true)
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setFormError('')
    try {
      const payload = toPayload(form)
      if (editingId) {
        await crud.update({ id: editingId, payload })
      } else {
        await crud.create(payload)
      }
      setModalOpen(false)
    } catch (err) {
      setFormError(errorMessage(err))
    }
  }

  async function handleDelete(row) {
    if (!window.confirm(`Hapus "${row.name || row.username || row.invoiceNumber}"?`)) return
    try {
      await crud.remove(row.id)
    } catch (err) {
      window.alert(errorMessage(err, 'Gagal menghapus data'))
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-xl font-semibold text-slate-800">{title}</h1>
        <div className="flex flex-col gap-2 sm:flex-row">
          <input
            value={crud.search}
            onChange={(e) => crud.setSearch(e.target.value)}
            placeholder="Cari..."
            className="rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none"
          />
          {canWrite && (
            <button
              onClick={openCreate}
              className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700"
            >
              + Tambah
            </button>
          )}
        </div>
      </div>

      <div className="overflow-x-auto rounded-xl bg-white shadow-sm">
        <table className="min-w-full divide-y divide-slate-200 text-sm">
          <thead className="bg-slate-50">
            <tr>
              {columns.map((col) => (
                <th key={col.key} className="px-4 py-3 text-left font-medium text-slate-600">
                  {col.label}
                </th>
              ))}
              {canWrite && <th className="px-4 py-3 text-right font-medium text-slate-600">Aksi</th>}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {crud.isLoading && (
              <tr>
                <td colSpan={columns.length + 1} className="px-4 py-6 text-center text-slate-400">
                  Memuat data...
                </td>
              </tr>
            )}
            {!crud.isLoading && page?.content?.length === 0 && (
              <tr>
                <td colSpan={columns.length + 1} className="px-4 py-6 text-center text-slate-400">
                  Tidak ada data
                </td>
              </tr>
            )}
            {page?.content?.map((row) => (
              <tr key={row.id} className="hover:bg-slate-50">
                {columns.map((col) => (
                  <td key={col.key} className="px-4 py-3 text-slate-700">
                    {col.render ? col.render(row) : row[col.key]}
                  </td>
                ))}
                {canWrite && (
                  <td className="px-4 py-3 text-right">
                    <button
                      onClick={() => openEdit(row)}
                      className="mr-3 text-emerald-600 hover:underline"
                    >
                      Ubah
                    </button>
                    <button onClick={() => handleDelete(row)} className="text-red-600 hover:underline">
                      Hapus
                    </button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {page && page.totalPages > 1 && (
        <div className="flex items-center justify-between text-sm text-slate-600">
          <span>
            Halaman {page.pageNumber + 1} dari {page.totalPages} ({page.totalElements} data)
          </span>
          <div className="flex gap-2">
            <button
              disabled={page.pageNumber === 0}
              onClick={() => crud.setPage(page.pageNumber - 1)}
              className="rounded-md border border-slate-300 px-3 py-1 disabled:opacity-40"
            >
              Sebelumnya
            </button>
            <button
              disabled={page.last}
              onClick={() => crud.setPage(page.pageNumber + 1)}
              className="rounded-md border border-slate-300 px-3 py-1 disabled:opacity-40"
            >
              Berikutnya
            </button>
          </div>
        </div>
      )}

      <Modal open={modalOpen} title={editingId ? `Ubah ${title}` : `Tambah ${title}`} onClose={() => setModalOpen(false)}>
        <form onSubmit={handleSubmit} className="space-y-4">
          {formError && (
            <div className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{formError}</div>
          )}
          {renderForm({ form, setForm, editingId })}
          <div className="flex justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={() => setModalOpen(false)}
              className="rounded-lg border border-slate-300 px-4 py-2 text-sm text-slate-600 hover:bg-slate-50"
            >
              Batal
            </button>
            <button
              type="submit"
              disabled={crud.isSaving}
              className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-60"
            >
              {crud.isSaving ? 'Menyimpan...' : 'Simpan'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
