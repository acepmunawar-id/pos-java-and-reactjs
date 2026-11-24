import { useState } from 'react'
import CategoriesPage from './CategoriesPage'
import UnitsPage from './UnitsPage'

const TABS = [
  { key: 'categories', label: 'Kategori',  render: () => <CategoriesPage /> },
  { key: 'units',      label: 'Satuan',    render: () => <UnitsPage /> },
]

export default function MasterCategoryPage() {
  const [tab, setTab] = useState('categories')
  const active = TABS.find(t => t.key === tab)

  return (
    <div className="space-y-4">
      <div className="flex gap-2">
        {TABS.map(t => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={`rounded-lg px-4 py-2 text-sm font-medium transition ${
              tab === t.key ? 'bg-emerald-600 text-white' : 'bg-white text-slate-600 hover:bg-slate-100'
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
