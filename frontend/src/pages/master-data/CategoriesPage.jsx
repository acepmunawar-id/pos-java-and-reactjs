import CrudPage from '../../components/CrudPage'
import Field, { inputClass } from '../../components/Field'
import { categoriesApi } from '../../api/resources'
import { formatAmount, toNumber } from '../../utils/measured'

const emptyForm = {
  name: '',
  description: '',
  type: 'GENERAL',
  baseUnit: 'ml',
  hasMixer: false,
  materialRatio: 2,
  mixerRatio: 1,
  mixerName: '',
  mixerPricePerUnit: 0,
  mixerStock: 0,
  sizes: [],
}

function money(value) {
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(
    value || 0,
  )
}

/** Bentuk payload sesuai CategoryRequest backend. */
function toPayload(form) {
  const measured = form.type === 'MEASURED'
  const withMixer = measured && form.hasMixer
  return {
    name: form.name,
    description: form.description,
    type: form.type,
    baseUnit: measured ? form.baseUnit : null,
    materialRatio: withMixer ? toNumber(form.materialRatio) : null,
    mixerRatio: withMixer ? toNumber(form.mixerRatio) : null,
    mixerName: withMixer ? form.mixerName : null,
    mixerPricePerUnit: withMixer ? toNumber(form.mixerPricePerUnit) : null,
    mixerStock: withMixer ? toNumber(form.mixerStock) : null,
    sizes: measured
      ? (form.sizes || [])
          .filter((s) => toNumber(s.sizeAmount))
          .map((s) => ({ sizeAmount: toNumber(s.sizeAmount), packagingPrice: toNumber(s.packagingPrice) ?? 0 }))
      : [],
  }
}

function summary(row) {
  if (row.type !== 'MEASURED') return 'Harga & stok per barang'
  const sizes = (row.sizes || []).map((s) => `${formatAmount(s.sizeAmount)} ${row.baseUnit}`).join(' / ')
  const mixer = row.hasMixer
    ? ` · ${row.mixerName || 'campuran'} ${formatAmount(row.materialRatio)}:${formatAmount(row.mixerRatio)} (stok ${formatAmount(row.mixerStock)} ${row.baseUnit})`
    : ''
  return `Ukuran ${sizes}${mixer}`
}

function SizesEditor({ form, setForm }) {
  const sizes = form.sizes || []
  const update = (index, patch) =>
    setForm({ ...form, sizes: sizes.map((s, i) => (i === index ? { ...s, ...patch } : s)) })
  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between">
        <span className="text-sm font-medium text-slate-600">Ukuran Kemasan yang Dijual</span>
        <button
          type="button"
          onClick={() => setForm({ ...form, sizes: [...sizes, { sizeAmount: '', packagingPrice: 0 }] })}
          className="text-sm font-medium text-emerald-600 hover:underline"
        >
          + Tambah ukuran
        </button>
      </div>
      {sizes.length === 0 && (
        <p className="rounded-lg bg-slate-50 px-3 py-2 text-xs text-slate-500">
          Belum ada ukuran. Contoh: 10 / 35 / 50 ml, atau 250 / 500 / 1000 gram.
        </p>
      )}
      {sizes.map((size, index) => (
        <div key={index} className="grid grid-cols-[1fr_1fr_auto] items-end gap-2">
          <Field label={`Isi (${form.baseUnit || 'satuan'})`}>
            <input
              type="number"
              min="0"
              step="any"
              required
              className={inputClass}
              value={size.sizeAmount}
              onChange={(e) => update(index, { sizeAmount: e.target.value })}
            />
          </Field>
          <Field label="Harga kemasan kosong">
            <input
              type="number"
              min="0"
              className={inputClass}
              value={size.packagingPrice}
              onChange={(e) => update(index, { packagingPrice: e.target.value })}
            />
          </Field>
          <button
            type="button"
            onClick={() => setForm({ ...form, sizes: sizes.filter((_, i) => i !== index) })}
            className="mb-2 text-sm text-red-600 hover:underline"
          >
            Hapus
          </button>
        </div>
      ))}
    </div>
  )
}

export default function CategoriesPage() {
  return (
    <CrudPage
      title="Kategori Produk"
      resourceApi={categoriesApi}
      queryKey="categories"
      emptyForm={emptyForm}
      toPayload={toPayload}
      columns={[
        { key: 'name', label: 'Nama' },
        {
          key: 'type',
          label: 'Tipe',
          render: (r) =>
            r.type === 'MEASURED' ? (
              <span className="rounded-full bg-violet-50 px-2 py-0.5 text-xs font-medium text-violet-700">
                Per Takaran
              </span>
            ) : (
              <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600">Umum</span>
            ),
        },
        { key: 'summary', label: 'Pengaturan', render: summary },
        { key: 'description', label: 'Deskripsi' },
      ]}
      renderForm={({ form, setForm }) => {
        const measured = form.type === 'MEASURED'
        const unit = form.baseUnit || 'satuan'
        return (
          <>
            <Field label="Nama Kategori">
              <input
                required
                className={inputClass}
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
              />
            </Field>
            <Field label="Deskripsi">
              <textarea
                className={inputClass}
                rows={2}
                value={form.description || ''}
                onChange={(e) => setForm({ ...form, description: e.target.value })}
              />
            </Field>
            <Field label="Tipe Kategori">
              <select
                className={inputClass}
                value={form.type || 'GENERAL'}
                onChange={(e) => setForm({ ...form, type: e.target.value })}
              >
                <option value="GENERAL">Umum: harga & stok per barang (pcs)</option>
                <option value="MEASURED">Per Takaran: stok ml/gram, dijual per ukuran kemasan</option>
              </select>
            </Field>

            {measured && (
              <div className="space-y-4 rounded-lg border border-violet-100 bg-violet-50/40 p-3">
                <Field label="Satuan Dasar">
                  <input
                    required
                    list="base-units"
                    className={inputClass}
                    value={form.baseUnit || ''}
                    onChange={(e) => setForm({ ...form, baseUnit: e.target.value })}
                  />
                  <datalist id="base-units">
                    <option value="ml" />
                    <option value="gram" />
                    <option value="kg" />
                    <option value="liter" />
                  </datalist>
                </Field>

                <SizesEditor form={form} setForm={setForm} />

                <label className="flex items-center gap-2 text-sm">
                  <input
                    type="checkbox"
                    checked={!!form.hasMixer}
                    onChange={(e) => setForm({ ...form, hasMixer: e.target.checked })}
                  />
                  Pakai bahan campuran (mis. alkohol untuk parfum)
                </label>

                {form.hasMixer && (
                  <div className="space-y-3">
                    <Field label="Nama Bahan Campuran">
                      <input
                        className={inputClass}
                        placeholder="Alkohol Absolut"
                        value={form.mixerName || ''}
                        onChange={(e) => setForm({ ...form, mixerName: e.target.value })}
                      />
                    </Field>
                    <div className="grid grid-cols-2 gap-3">
                      <Field label="Rasio bahan utama">
                        <input
                          type="number"
                          min="0"
                          step="any"
                          required
                          className={inputClass}
                          value={form.materialRatio ?? ''}
                          onChange={(e) => setForm({ ...form, materialRatio: e.target.value })}
                        />
                      </Field>
                      <Field label="Rasio campuran">
                        <input
                          type="number"
                          min="0"
                          step="any"
                          required
                          className={inputClass}
                          value={form.mixerRatio ?? ''}
                          onChange={(e) => setForm({ ...form, mixerRatio: e.target.value })}
                        />
                      </Field>
                    </div>
                    <div className="grid grid-cols-2 gap-3">
                      <Field label={`Harga campuran per ${unit}`}>
                        <input
                          type="number"
                          min="0"
                          step="any"
                          className={inputClass}
                          value={form.mixerPricePerUnit ?? ''}
                          onChange={(e) => setForm({ ...form, mixerPricePerUnit: e.target.value })}
                        />
                      </Field>
                      <Field label={`Stok campuran (${unit})`}>
                        <input
                          type="number"
                          min="0"
                          step="any"
                          className={inputClass}
                          value={form.mixerStock ?? ''}
                          onChange={(e) => setForm({ ...form, mixerStock: e.target.value })}
                        />
                      </Field>
                    </div>
                    {toNumber(form.materialRatio) > 0 && toNumber(form.mixerRatio) > 0 && (
                      <p className="text-xs text-slate-500">
                        Contoh: kemasan 30 {unit} = {formatAmount((30 * form.materialRatio) / (Number(form.materialRatio) + Number(form.mixerRatio)))}{' '}
                        {unit} bahan utama + {formatAmount((30 * form.mixerRatio) / (Number(form.materialRatio) + Number(form.mixerRatio)))}{' '}
                        {unit} {form.mixerName || 'campuran'} (harga campuran {money(form.mixerPricePerUnit)}/{unit}).
                      </p>
                    )}
                  </div>
                )}
              </div>
            )}
          </>
        )
      }}
    />
  )
}
