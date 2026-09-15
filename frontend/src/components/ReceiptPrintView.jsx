function money(value) {
  return `Rp${Math.round(Number(value || 0)).toLocaleString('id-ID')}`
}

/** 80mm receipt layout - only visible on screen inside the print modal; the
 * `@media print` rule in index.css hides everything else when printing. */
export default function ReceiptPrintView({ receipt }) {
  if (!receipt) return null

  return (
    <div id="receipt-print-area" className="mx-auto w-[80mm] bg-white p-3 text-xs text-black">
      <div className="text-center">
        <div className="text-sm font-bold">{receipt.storeName}</div>
        <div>{receipt.storeAddress}</div>
        <div>{receipt.storePhone}</div>
      </div>
      <div className="my-2 border-t border-dashed border-black" />
      <div>No: {receipt.invoiceNumber}</div>
      <div>Tgl: {new Date(receipt.saleDate).toLocaleString('id-ID')}</div>
      <div>Kasir: {receipt.cashierName}</div>
      {receipt.customerName && <div>Pelanggan: {receipt.customerName}</div>}
      <div className="my-2 border-t border-dashed border-black" />
      {receipt.items.map((item, idx) => (
        <div key={idx} className="mb-1">
          <div>{item.productName}</div>
          <div className="flex justify-between">
            <span>
              {item.quantity} x {money(item.price)}
            </span>
            <span>{money(item.subtotal)}</span>
          </div>
        </div>
      ))}
      <div className="my-2 border-t border-dashed border-black" />
      <div className="flex justify-between">
        <span>Subtotal</span>
        <span>{money(receipt.subtotal)}</span>
      </div>
      {Number(receipt.discountAmount) > 0 && (
        <div className="flex justify-between">
          <span>Diskon</span>
          <span>-{money(receipt.discountAmount)}</span>
        </div>
      )}
      {Number(receipt.taxAmount) > 0 && (
        <div className="flex justify-between">
          <span>Pajak</span>
          <span>{money(receipt.taxAmount)}</span>
        </div>
      )}
      <div className="flex justify-between font-bold">
        <span>TOTAL</span>
        <span>{money(receipt.total)}</span>
      </div>
      <div className="flex justify-between">
        <span>Bayar ({receipt.paymentMethod})</span>
        <span>{money(receipt.amountPaid)}</span>
      </div>
      <div className="flex justify-between">
        <span>Kembali</span>
        <span>{money(receipt.changeAmount)}</span>
      </div>
      <div className="my-2 border-t border-dashed border-black" />
      <div className="text-center">Terima kasih atas kunjungan Anda</div>
    </div>
  )
}
