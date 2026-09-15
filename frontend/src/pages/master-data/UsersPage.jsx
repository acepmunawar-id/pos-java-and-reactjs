import CrudPage from '../../components/CrudPage'
import Field, { inputClass } from '../../components/Field'
import { usersApi } from '../../api/resources'
import { ROLE_LABEL } from '../../layouts/navConfig'

const emptyForm = { username: '', password: '', fullName: '', email: '', role: 'USER', active: true }

export default function UsersPage() {
  return (
    <CrudPage
      title="Pengguna"
      resourceApi={usersApi}
      queryKey="users"
      emptyForm={emptyForm}
      columns={[
        { key: 'username', label: 'Username' },
        { key: 'fullName', label: 'Nama Lengkap' },
        { key: 'role', label: 'Role', render: (r) => ROLE_LABEL[r.role] || r.role },
        { key: 'active', label: 'Status', render: (r) => (r.active ? 'Aktif' : 'Nonaktif') },
      ]}
      renderForm={({ form, setForm, editingId }) => (
        <>
          <Field label="Username">
            <input
              required
              disabled={Boolean(editingId)}
              className={`${inputClass} disabled:bg-slate-100`}
              value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
            />
          </Field>
          <Field label={editingId ? 'Password (kosongkan jika tidak diubah)' : 'Password'}>
            <input
              type="password"
              required={!editingId}
              className={inputClass}
              value={form.password || ''}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
            />
          </Field>
          <Field label="Nama Lengkap">
            <input
              required
              className={inputClass}
              value={form.fullName}
              onChange={(e) => setForm({ ...form, fullName: e.target.value })}
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
          <Field label="Role">
            <select
              className={inputClass}
              value={form.role}
              onChange={(e) => setForm({ ...form, role: e.target.value })}
            >
              <option value="SUPER_ADMIN">Super Admin</option>
              <option value="ADMIN">Admin</option>
              <option value="USER">Kasir (User)</option>
            </select>
          </Field>
          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={form.active}
              onChange={(e) => setForm({ ...form, active: e.target.checked })}
            />
            Aktif (dapat login)
          </label>
        </>
      )}
    />
  )
}
