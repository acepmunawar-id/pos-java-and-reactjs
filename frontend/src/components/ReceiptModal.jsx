import { useState } from 'react'
import Modal from './Modal'
import ReceiptPrintView from './ReceiptPrintView'
import { usePrinter } from './usePrinter'
import { getReceiptEscPos } from '../api/sales'
import { errorMessage } from '../api/client'

export default function ReceiptModal({ saleId, receipt, onClose }) {
  const printer = usePrinter()
  const [printError, setPrintError] = useState('')

  async function handleThermalPrint() {
    setPrintError('')
    try {
      const bytes = await getReceiptEscPos(saleId)
      await printer.send(bytes)
    } catch (err) {
      setPrintError(errorMessage(err, 'Gagal mencetak ke printer thermal'))
    }
  }

  function handleBrowserPrint() {
    window.print()
  }

  return (
    <Modal open={Boolean(receipt)} title="Struk Transaksi" onClose={onClose}>
      <div className="space-y-4">
        <ReceiptPrintView receipt={receipt} />

        {printError && <div className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{printError}</div>}

        <div className="flex flex-col gap-2 sm:flex-row">
          <button
            onClick={handleThermalPrint}
            disabled={printer.busy}
            className="flex-1 rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-60"
            title={printer.supported ? '' : 'Perlu browser Chrome/Edge dan printer thermal via USB/Serial'}
          >
            {printer.busy ? 'Mengirim...' : '🖨️ Cetak Thermal (ESC/POS)'}
          </button>
          <button
            onClick={handleBrowserPrint}
            className="flex-1 rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
          >
            🖶 Cetak Browser (fallback)
          </button>
        </div>
        {!printer.supported && (
          <p className="text-xs text-slate-400">
            Browser ini tidak mendukung Web Serial API - gunakan tombol "Cetak Browser" atau buka dengan Chrome/Edge
            terbaru untuk cetak langsung ke printer thermal.
          </p>
        )}
        <button onClick={onClose} className="w-full rounded-lg bg-slate-100 px-4 py-2 text-sm text-slate-600">
          Tutup
        </button>
      </div>
    </Modal>
  )
}
