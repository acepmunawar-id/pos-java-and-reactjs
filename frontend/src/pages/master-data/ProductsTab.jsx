import { useQuery } from '@tanstack/react-query'
import CrudPage from '../../components/CrudPage'
import Field, { inputClass } from '../../components/Field'
import { productsApi, categoriesApi, unitsApi } from '../../api/resources'
import { computeSize, formatAmount, sizeKey, toNumber } from '../../utils/measured'

const emptyForm = {
  sku: '',
  barcode: '',
  name: '',
  categoryId: '',
  unitId: '',
  purchasePrice: 0,
  sellPrice: 0,
  stock: 0,
  minStock: 0,
  active: true,
  // per takaran
  materialPricePerUnit: '',
  materialStock: 0,
  minMaterialStock: 0,
  priceBySize: null, // { "35": "35000" } - diisi dari row.sizes saat edit
}

function money(value) {
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(
    value || 0,
  )
}

/** Harga jual per ukuran: dari input form bila sudah diubah, kalau belum dari data produk (row.sizes). */
function priceFor(form, sizeAmount) {
  const key = sizeKey(sizeAmount)
  if (form.priceBySize && key in form.priceBySize) return form.priceBySize[key]
  const existing = form.sizes?.find((s) => sizeKey(s.sizeAmount) === key)
  return existing ? existing.sellPrice : ''
}

export default function ProductsTab() {
  const { data: categories } = useQuery({
    queryKey: ['categories', 'all-for-select'],
    queryFn: () => categoriesApi.list({ size: 100 }),
  })
  const { data: units } = useQuery({
    queryKey: ['units', 'all-for-select'],
    queryFn: () => unitsApi.list({ size: 100 }),
  })

  const findCategory = (form) => {
    const id = form.categoryId || form.category?.id
    return categories?.content?.find((c) => c.id === id)
  }

  function toPayload(form) {
    const category = findCategory(form)
    const measured = category?.type === 'MEASURED'
    return {
      sku: form.sku,
      barcode: form.barcode,
      name: form.name,
      categoryId: form.categoryId || form.category?.id || null,
      unitId: form.unitId || form.unit?.id || null,
      active: form.active,
      purchasePrice: measured ? 0 : toNumber(form.purchasePrice) ?? 0,
      sellPrice: measured ? 0 : toNumber(form.sellPrice) ?? 0,
      stock: measured ? 0 : toNumber(form.stock) ?? 0,
      minStock: measured ? 0 : toNumber(form.minStock) ?? 0,
      materialPricePerUnit: measured ? toNumber(form.materialPricePerUnit) : null,
      materialStock: measured ? toNumber(form.materialStock) ?? 0 : null,
      minMaterialStock: measured ? toNumber(form.minMaterialStock) ?? 0 : null,
      sizePrices: measured
        ? category.sizes
            .map((s) => ({ sizeAmount: s.sizeAmount, sellPrice: toNumber(priceFor(form, s.sizeAmount)) }))
            .filter((p) => p.sellPrice !== null)
        : [],
    }
  }

  return (
    <CrudPage
      title="Produk"
      resourceApi={productsApi}
      queryKey="products"
      emptyForm={emptyForm}
      toPayload={toPayload}
      columns={[
        { key: 'sku', label: 'SKU' },
        { key: 'name', label: 'Nama' },
        { key: 'category', label: 'Kategori', render: (r) => r.category?.name || '-' },
        {
          key: 'sellPrice',
          label: 'Harga Jual',
          render: (r) =>
            r.measured ? (
              <div className="space-y-0.5 text-xs">
                {r.sizes.map((s) => (
                  <div key={s.sizeAmount}>
                    <span className="text-slate-500">
                      {formatAmount(s.sizeAmount)} {r.category.baseUnit}:
                    </span>{' '}
                    {money(s.sellPrice)}
                  </div>
                ))}
              </div>
            ) : (
              money(r.sellPrice)
            ),
        },
        {
          key: 'stock',
          label: 'Stok',
          render: (r) => (
            <span className={r.lowStock ? 'font-semibold text-red-600' : ''}>
              {r.measured
                ? `${formatAmount(r.materialStock)} ${r.category.baseUnit}`
                : `${r.stock} ${r.unit?.symbol || ''}`}
              {r.lowStock && ' (menipis)'}
              {r.measured && (
                <span className="block text-xs font-normal text-slate-500">
                  bisa dibuat:{' '}
                  {r.sizes.map((s) => `${s.available}×${formatAmount(s.sizeAmount)}`).join(', ')}
                </span>
              )}
            </span>
          ),
        },
        { key: 'active', label: 'Status', render: (r) => (r.active ? 'Aktif' : 'Nonaktif') },
      ]}
      renderForm={({ form, setForm }) => {
        const category = findCategory(form)
        const measured = category?.type === 'MEASURED'
        const unit = category?.baseUnit
        return (
          <>
            <div className="grid grid-cols-2 gap-3">
              <Field label={measured ? 'SKU / Kode' : 'SKU'}>
                <input
                  required
                  className={inputClass}
                  value={form.sku}
                  onChange={(e) => setForm({ ...form, sku: e.target.value })}
                />
              </Field>
              <Field label="Barcode">
                <input
                  className={inputClass}
                  value={form.barcode || ''}
                  onChange={(e) => setForm({ ...form, barcode: e.target.value })}
                />
              </Field>
            </div>
            <Field label={measured ? 'Nama Produk / Varian (mis. nama aroma)' : 'Nama Produk'}>
              <input
                required
                className={inputClass}
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
              />
            </Field>
            <div className="grid grid-cols-2 gap-3">
              <Field label="Kategori">
                <select
                  className={inputClass}
                  value={form.categoryId || form.category?.id || ''}
                  onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
                >
                  <option value="">- Pilih -</option>
                  {categories?.content?.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name}
                      {c.type === 'MEASURED' ? ' (per takaran)' : ''}
                    </option>
                  ))}
                </select>
              </Field>
              {!measured && (
                <Field label="Satuan">
                  <select
                    className={inputClass}
                    value={form.unitId || form.unit?.id || ''}
                    onChange={(e) => setForm({ ...form, unitId: e.target.value })}
                  >
                    <option value="">- Pilih -</option>
                    {units?.content?.map((u) => (
                      <option key={u.id} value={u.id}>
                        {u.name} ({u.symbol})
                      </option>
                    ))}
                  </select>
                </Field>
              )}
            </div>

            {!measured && (
              <>
                <div className="grid grid-cols-2 gap-3">
                  <Field label="Harga Beli">
                    <input
                      type="number"
                      min="0"
                      required
                      className={inputClass}
                      value={form.purchasePrice}
                      onChange={(e) => setForm({ ...form, purchasePrice: e.target.value })}
                    />
                  </Field>
                  <Field label="Harga Jual">
                    <input
                      type="number"
                      min="0"
                      required
                      className={inputClass}
                      value={form.sellPrice}
                      onChange={(e) => setForm({ ...form, sellPrice: e.target.value })}
                    />
                  </Field>
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <Field label="Stok">
                    <input
                      type="number"
                      min="0"
                      className={inputClass}
                      value={form.stock}
                      onChange={(e) => setForm({ ...form, stock: e.target.value })}
                    />
                  </Field>
                  <Field label="Stok Minimum">
                    <input
                      type="number"
                      min="0"
                      className={inputClass}
                      value={form.minStock}
                      onChange={(e) => setForm({ ...form, minStock: e.target.value })}
                    />
                  </Field>
                </div>
              </>
            )}

            {measured && (
              <div className="space-y-3 rounded-lg border border-violet-100 bg-violet-50/40 p-3">
                <div className="grid grid-cols-3 gap-3">
                  <Field label={`Harga bahan / ${unit}`}>
                    <input
                      type="number"
                      min="0"
                      step="any"
                      required
                      className={inputClass}
                      value={form.materialPricePerUnit ?? ''}
                      onChange={(e) => setForm({ ...form, materialPricePerUnit: e.target.value })}
                    />
                  </Field>
                  <Field label={`Stok bahan (${unit})`}>
                    <input
                      type="number"
                      min="0"
                      step="any"
                      className={inputClass}
                      value={form.materialStock ?? ''}
                      onChange={(e) => setForm({ ...form, materialStock: e.target.value })}
                    />
                  </Field>
                  <Field label={`Stok minimum (${unit})`}>
                    <input
                      type="number"
                      min="0"
                      step="any"
                      className={inputClass}
                      value={form.minMaterialStock ?? ''}
                      onChange={(e) => setForm({ ...form, minMaterialStock: e.target.value })}
                    />
                  </Field>
                </div>

                <div>
                  <div className="mb-1 text-sm font-medium text-slate-600">Harga jual per ukuran</div>
                  <p className="mb-2 text-xs text-slate-500">
                    Kosongkan harga bila ukuran itu tidak dijual.
                    {category.hasMixer &&
                      ` Takaran: ${formatAmount(category.materialRatio)} bagian bahan utama : ${formatAmount(category.mixerRatio)} bagian ${category.mixerName || 'campuran'}.`}
                  </p>
                  <table className="w-full text-sm">
                    <thead className="text-xs text-slate-500">
                      <tr>
                        <th className="pb-1 text-left font-medium">Ukuran</th>
                        <th className="pb-1 text-left font-medium">Harga jual</th>
                        <th className="pb-1 text-right font-medium">Modal</th>
                        <th className="pb-1 text-right font-medium">Untung</th>
                      </tr>
                    </thead>
                    <tbody>
                      {category.sizes.map((s) => {
                        const price = priceFor(form, s.sizeAmount)
                        const calc = computeSize(category, s.sizeAmount, form.materialPricePerUnit, s.packagingPrice)
                        const profit = toNumber(price) !== null ? toNumber(price) - calc.cost : null
                        return (
                          <tr key={s.sizeAmount} className="align-top">
                            <td className="py-1 pr-2">
                              <div className="font-medium">
                                {formatAmount(s.sizeAmount)} {unit}
                              </div>
                              {category.hasMixer && (
                                <div className="text-xs text-slate-500">
                                  {formatAmount(calc.material)} + {formatAmount(calc.mixer)} {unit}
                                </div>
                              )}
                            </td>
                            <td className="py-1 pr-2">
                              <input
                                type="number"
                                min="0"
                                className={inputClass}
                                value={price ?? ''}
                                onChange={(e) =>
                                  setForm({
                                    ...form,
                                    priceBySize: { ...(form.priceBySize || {}), [sizeKey(s.sizeAmount)]: e.target.value },
                                  })
                                }
                              />
                            </td>
                            <td className="whitespace-nowrap py-1 pl-2 text-right text-slate-600">{money(calc.cost)}</td>
                            <td
                              className={`whitespace-nowrap py-1 pl-3 text-right font-medium ${profit !== null && profit < 0 ? 'text-red-600' : 'text-emerald-600'}`}
                            >
                              {profit === null ? '-' : money(profit)}
                            </td>
                          </tr>
                        )
                      })}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            <label className="flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                checked={form.active}
                onChange={(e) => setForm({ ...form, active: e.target.checked })}
              />
              Aktif (muncul di halaman kasir)
            </label>
          </>
        )
      }}
    />
  )
}
