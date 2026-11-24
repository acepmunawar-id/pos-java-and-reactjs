import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { settingsApi } from '../../api/settings'
import { errorMessage } from '../../api/client'
import { AlertBanner, SectionCard } from '../../components/ui'

/* Label & tipe tiap setting key */
const FIELD_META = {
  modal_awal:  { label: 'Modal Awal (Rp)',    type: 'number', placeholder: '0',               desc: 'Uang tunai awal sebelum ada transaksi. Digunakan untuk menghitung Saldo Sekarang di Laporan.' },
  nama_toko:   { label: 'Nama Toko',          type: 'text',   placeholder: 'Shafel Parfum',   desc: 'Tampil di header struk cetak.' },
  alamat_toko: { label: 'Alamat Toko',        type: 'text',   placeholder: 'Jl. ...',          desc: 'Tampil di struk cetak.' },
  no_telp:     { label: 'Nomor Telepon',      type: 'text',   placeholder: '08xx-xxxx-xxxx',  desc: 'Tampil di struk cetak.' },
}

export default function SettingsPage() {
  const qc = useQueryClient()
  const [form,    setForm]    = useState({})
  const [saved,   setSaved]   = useState('')
  const [saveErr, setSaveErr] = useState('')

  const { data: settings = [], isLoading } = useQuery({
    queryKey: ['app-settings'],
    queryFn:  settingsApi.list,
  })

  /* Isi form dari data server */
  useEffect(() => {
    if (settings.length > 0) {
      const init = {}
      settings.forEach(s => { init[s.key] = s.value ?? '' })
      setForm(init)
    }
  }, [settings])

  const saveMut = useMutation({
    mutationFn: ({ key, value }) => settingsApi.update(key, value),
    onSuccess: (_, { key }) => {
      qc.invalidateQueries({ queryKey: ['app-settings'] })
      qc.invalidateQueries({ queryKey: ['financial-summary'] })
      setSaved(key)
      setSaveErr('')
      setTimeout(() => setSaved(''), 2500)
    },
    onError: (err) => setSaveErr(errorMessage(err, 'Gagal menyimpan')),
  })

  function handleSave(key) {
    setSaveErr('')
    saveMut.mutate({ key, value: form[key] ?? '' })
  }

  if (isLoading) return <p className="p-6 text-sm text-slate-400">Memuat pengaturan…</p>

  /* Urutan tampil */
  const orderedKeys = ['modal_awal', 'nama_toko', 'alamat_toko', 'no_telp']

  return (
    <div className="space-y-5">
      <h1 className="text-xl font-semibold text-slate-800">Pengaturan Aplikasi</h1>

      {saveErr && <AlertBanner type="error">{saveErr}</AlertBanner>}

      <SectionCard title="⚙️ Konfigurasi">
        <div className="divide-y divide-slate-50">
          {orderedKeys.map(key => {
            const meta = FIELD_META[key] ?? { label: key, type: 'text', placeholder: '', desc: '' }
            const isSaving = saveMut.isPending && saveMut.variables?.key === key
            const justSaved = saved === key

            return (
              <div key={key} className="flex flex-wrap items-start gap-4 px-6 py-5">
                <div className="min-w-0 flex-1">
                  <label className="block text-sm font-medium text-slate-700">{meta.label}</label>
                  {meta.desc && <p className="mt-0.5 text-xs text-slate-400">{meta.desc}</p>}
                  <input
                    type={meta.type}
                    value={form[key] ?? ''}
                    min={meta.type === 'number' ? '0' : undefined}
                    step={meta.type === 'number' ? '1000' : undefined}
                    placeholder={meta.placeholder}
                    onChange={e => setForm(f => ({ ...f, [key]: e.target.value }))}
                    onKeyDown={e => e.key === 'Enter' && handleSave(key)}
                    className="mt-2 w-full max-w-sm rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-orange-400"
                  />
                </div>
                <div className="flex shrink-0 items-center gap-2 pt-7">
                  {justSaved && (
                    <span className="text-xs font-medium text-emerald-600">✓ Tersimpan</span>
                  )}
                  <button
                    type="button"
                    disabled={isSaving}
                    onClick={() => handleSave(key)}
                    className="rounded-lg bg-orange-500 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-orange-600 disabled:opacity-60"
                  >
                    {isSaving ? 'Menyimpan…' : 'Simpan'}
                  </button>
                </div>
              </div>
            )
          })}
        </div>
      </SectionCard>
    </div>
  )
}
