import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { categoriesApi } from '../../api/resources'
import ProductsTab from './ProductsTab'

export default function ProductsPage() {
  const [activeCatId, setActiveCatId] = useState(null)

  const { data: categories } = useQuery({
    queryKey: ['categories', 'all-for-select'],
    queryFn: () => categoriesApi.list({ size: 100 }),
  })

  const catList = categories?.content ?? []

  return (
    <div className="space-y-4">
      {/* Tab kategori */}
      <div className="flex flex-wrap gap-2">
        <button
          onClick={() => setActiveCatId(null)}
          className={`rounded-full px-4 py-1.5 text-sm font-medium transition ${
            activeCatId === null
              ? 'bg-emerald-600 text-white shadow-sm'
              : 'bg-white text-slate-600 hover:bg-slate-100'
          }`}
        >
          Semua
        </button>
        {catList.map(cat => (
          <button
            key={cat.id}
            onClick={() => setActiveCatId(cat.id)}
            className={`rounded-full px-4 py-1.5 text-sm font-medium transition ${
              activeCatId === cat.id
                ? 'bg-emerald-600 text-white shadow-sm'
                : 'bg-white text-slate-600 hover:bg-slate-100'
            }`}
          >
            {cat.name}
          </button>
        ))}
      </div>

      {/* Tabel produk — filter & default category mengikuti tab aktif */}
      <ProductsTab
        filterCategoryId={activeCatId}
        defaultCategoryId={activeCatId}
      />
    </div>
  )
}
