import CrudPage from '../../components/CrudPage'
import Field, { inputClass } from '../../components/Field'
import { categoriesApi } from '../../api/resources'

const emptyForm = { name: '', description: '' }

export default function CategoriesPage() {
  return (
    <CrudPage
      title="Kategori Produk"
      resourceApi={categoriesApi}
      queryKey="categories"
      emptyForm={emptyForm}
      columns={[
        { key: 'name', label: 'Nama' },
        { key: 'description', label: 'Deskripsi' },
      ]}
      renderForm={({ form, setForm }) => (
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
        </>
      )}
    />
  )
}
