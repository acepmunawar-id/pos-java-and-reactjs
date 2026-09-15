import { useQuery } from '@tanstack/react-query'
import CrudPage from '../../components/CrudPage'
import Field, { inputClass } from '../../components/Field'
import { productsApi, categoriesApi, unitsApi } from '../../api/resources'

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
}

function money(value) {
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(
    value || 0,
  )
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

  return (
    <CrudPage
      title="Produk"
      resourceApi={productsApi}
      queryKey="products"
      emptyForm={emptyForm}
      columns={[
        { key: 'sku', label: 'SKU' },
        { key: 'name', label: 'Nama' },
        { key: 'category', label: 'Kategori', render: (r) => r.category?.name || '-' },
        { key: 'sellPrice', label: 'Harga Jual', render: (r) => money(r.sellPrice) },
        {
          key: 'stock',
          label: 'Stok',
          render: (r) => (
            <span className={r.lowStock ? 'font-semibold text-red-600' : ''}>
              {r.stock} {r.unit?.symbol || ''}
              {r.lowStock && ' (menipis)'}
            </span>
          ),
        },
        { key: 'active', label: 'Status', render: (r) => (r.active ? 'Aktif' : 'Nonaktif') },
      ]}
      renderForm={({ form, setForm }) => (
        <>
          <div className="grid grid-cols-2 gap-3">
            <Field label="SKU">
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
          <Field label="Nama Produk">
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
                  </option>
                ))}
              </select>
            </Field>
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
          </div>
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
          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={form.active}
              onChange={(e) => setForm({ ...form, active: e.target.checked })}
            />
            Aktif (muncul di halaman kasir)
          </label>
        </>
      )}
    />
  )
}
