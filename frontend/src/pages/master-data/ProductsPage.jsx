import { useState } from 'react'
import ProductsTab from './ProductsTab'
import CategoriesPage from './CategoriesPage'
import UnitsPage from './UnitsPage'

const TABS = [
  { key: 'products', label: 'Produk', render: () => <ProductsTab /> },
  { key: 'categories', label: 'Kategori', render: () => <CategoriesPage /> },
  { key: 'units', label: 'Satuan', render: () => <UnitsPage /> },
]

export default function ProductsPage() {
  const [tab, setTab] = useState('products')
  const active = TABS.find((t) => t.key === tab)

  return (
    <div className="space-y-4">
      <div className="flex gap-2">
        {TABS.map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={`rounded-lg px-4 py-2 text-sm font-medium ${
              tab === t.key ? 'bg-emerald-600 text-white' : 'bg-white text-slate-600'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>
      {active.render()}
    </div>
  )
}
