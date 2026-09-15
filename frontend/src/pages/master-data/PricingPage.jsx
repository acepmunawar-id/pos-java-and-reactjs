import { useState } from 'react'
import CrudPage from '../../components/CrudPage'
import Field, { inputClass } from '../../components/Field'
import { taxesApi, discountsApi } from '../../api/resources'

const taxForm = { name: '', rate: 0, active: true }
const discountForm = { name: '', type: 'PERCENTAGE', value: 0, active: true }

function TaxesTab() {
  return (
    <CrudPage
      title="Pajak"
      resourceApi={taxesApi}
      queryKey="taxes"
      emptyForm={taxForm}
      columns={[
        { key: 'name', label: 'Nama' },
        { key: 'rate', label: 'Tarif (%)' },
        { key: 'active', label: 'Status', render: (r) => (r.active ? 'Aktif' : 'Nonaktif') },
      ]}
      renderForm={({ form, setForm }) => (
        <>
          <Field label="Nama Pajak">
            <input
              required
              className={inputClass}
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
            />
          </Field>
          <Field label="Tarif (%)">
            <input
              type="number"
              step="0.01"
              min="0"
              max="100"
              required
              className={inputClass}
              value={form.rate}
              onChange={(e) => setForm({ ...form, rate: e.target.value })}
            />
          </Field>
          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={form.active}
              onChange={(e) => setForm({ ...form, active: e.target.checked })}
            />
            Aktif (muncul sebagai pilihan di halaman kasir)
          </label>
        </>
      )}
    />
  )
}

function DiscountsTab() {
  return (
    <CrudPage
      title="Diskon"
      resourceApi={discountsApi}
      queryKey="discounts"
      emptyForm={discountForm}
      columns={[
        { key: 'name', label: 'Nama' },
        { key: 'type', label: 'Tipe', render: (r) => (r.type === 'PERCENTAGE' ? 'Persentase' : 'Nominal') },
        {
          key: 'value',
          label: 'Nilai',
          render: (r) => (r.type === 'PERCENTAGE' ? `${r.value}%` : `Rp${r.value}`),
        },
        { key: 'active', label: 'Status', render: (r) => (r.active ? 'Aktif' : 'Nonaktif') },
      ]}
      renderForm={({ form, setForm }) => (
        <>
          <Field label="Nama Diskon">
            <input
              required
              className={inputClass}
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
            />
          </Field>
          <Field label="Tipe">
            <select
              className={inputClass}
              value={form.type}
              onChange={(e) => setForm({ ...form, type: e.target.value })}
            >
              <option value="PERCENTAGE">Persentase (%)</option>
              <option value="NOMINAL">Nominal (Rp)</option>
            </select>
          </Field>
          <Field label="Nilai">
            <input
              type="number"
              step="0.01"
              min="0"
              required
              className={inputClass}
              value={form.value}
              onChange={(e) => setForm({ ...form, value: e.target.value })}
            />
          </Field>
          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={form.active}
              onChange={(e) => setForm({ ...form, active: e.target.checked })}
            />
            Aktif (muncul sebagai pilihan di halaman kasir)
          </label>
        </>
      )}
    />
  )
}

export default function PricingPage() {
  const [tab, setTab] = useState('tax')
  return (
    <div className="space-y-4">
      <div className="flex gap-2">
        <button
          onClick={() => setTab('tax')}
          className={`rounded-lg px-4 py-2 text-sm font-medium ${
            tab === 'tax' ? 'bg-emerald-600 text-white' : 'bg-white text-slate-600'
          }`}
        >
          Pajak
        </button>
        <button
          onClick={() => setTab('discount')}
          className={`rounded-lg px-4 py-2 text-sm font-medium ${
            tab === 'discount' ? 'bg-emerald-600 text-white' : 'bg-white text-slate-600'
          }`}
        >
          Diskon
        </button>
      </div>
      {tab === 'tax' ? <TaxesTab /> : <DiscountsTab />}
    </div>
  )
}
