import CrudPage from '../../components/CrudPage'
import Field, { inputClass } from '../../components/Field'

const emptyForm = { name: '', phone: '', email: '', address: '' }

/** Shared screen for Customer and Supplier - both are the same "partner" shape. */
export default function PartnerPage({ title, resourceApi, queryKey }) {
  return (
    <CrudPage
      title={title}
      resourceApi={resourceApi}
      queryKey={queryKey}
      emptyForm={emptyForm}
      columns={[
        { key: 'name', label: 'Nama' },
        { key: 'phone', label: 'Telepon' },
        { key: 'email', label: 'Email' },
        { key: 'address', label: 'Alamat' },
      ]}
      renderForm={({ form, setForm }) => (
        <>
          <Field label="Nama">
            <input
              required
              className={inputClass}
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
            />
          </Field>
          <Field label="Telepon">
            <input
              className={inputClass}
              value={form.phone || ''}
              onChange={(e) => setForm({ ...form, phone: e.target.value })}
            />
          </Field>
          <Field label="Email">
            <input
              type="email"
              className={inputClass}
              value={form.email || ''}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
            />
          </Field>
          <Field label="Alamat">
            <textarea
              rows={2}
              className={inputClass}
              value={form.address || ''}
              onChange={(e) => setForm({ ...form, address: e.target.value })}
            />
          </Field>
        </>
      )}
    />
  )
}
