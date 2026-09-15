import CrudPage from '../../components/CrudPage'
import Field, { inputClass } from '../../components/Field'
import { unitsApi } from '../../api/resources'

const emptyForm = { name: '', symbol: '' }

export default function UnitsPage() {
  return (
    <CrudPage
      title="Satuan"
      resourceApi={unitsApi}
      queryKey="units"
      emptyForm={emptyForm}
      columns={[
        { key: 'name', label: 'Nama' },
        { key: 'symbol', label: 'Simbol' },
      ]}
      renderForm={({ form, setForm }) => (
        <>
          <Field label="Nama Satuan">
            <input
              required
              className={inputClass}
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
            />
          </Field>
          <Field label="Simbol (mis. pcs, kg)">
            <input
              required
              className={inputClass}
              value={form.symbol}
              onChange={(e) => setForm({ ...form, symbol: e.target.value })}
            />
          </Field>
        </>
      )}
    />
  )
}
