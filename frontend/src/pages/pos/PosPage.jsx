import { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { productsApi, customersApi, taxesApi, discountsApi } from '../../api/resources'
import { createSale, getReceiptView } from '../../api/sales'
import { errorMessage } from '../../api/client'
import ReceiptModal from '../../components/ReceiptModal'

function money(value) {
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(
    value || 0,
  )
}

export default function PosPage() {
  const [searchTerm, setSearchTerm] = useState('')
  const [cart, setCart] = useState([]) // [{productId, name, price, qty, stock, unit}]
  const [customerId, setCustomerId] = useState('')
  const [taxId, setTaxId] = useState('')
  const [discountId, setDiscountId] = useState('')
  const [paymentMethod, setPaymentMethod] = useState('CASH')
  const [amountPaid, setAmountPaid] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [receiptState, setReceiptState] = useState(null) // { saleId, receipt }

  const { data: searchResults } = useQuery({
    queryKey: ['pos', 'product-search', searchTerm],
    queryFn: () => productsApi.list({ search: searchTerm, size: 8 }),
    enabled: searchTerm.trim().length > 0,
  })

  const { data: customers } = useQuery({
    queryKey: ['pos', 'customers'],
    queryFn: () => customersApi.list({ size: 100 }),
  })
  const { data: taxes } = useQuery({ queryKey: ['pos', 'taxes'], queryFn: () => taxesApi.listActive() })
  const { data: discounts } = useQuery({
    queryKey: ['pos', 'discounts'],
    queryFn: () => discountsApi.listActive(),
  })

  const selectedTax = taxes?.find((t) => t.id === taxId)
  const selectedDiscount = discounts?.find((d) => d.id === discountId)

  const totals = useMemo(() => {
    const subtotal = cart.reduce((sum, item) => sum + item.price * item.qty, 0)
    let discountAmount = 0
    if (selectedDiscount) {
      discountAmount =
        selectedDiscount.type === 'PERCENTAGE'
          ? (subtotal * Number(selectedDiscount.value)) / 100
          : Number(selectedDiscount.value)
      discountAmount = Math.min(discountAmount, subtotal)
    }
    const taxable = subtotal - discountAmount
    const taxAmount = selectedTax ? (taxable * Number(selectedTax.rate)) / 100 : 0
    const total = taxable + taxAmount
    const change = Number(amountPaid || 0) - total
    return { subtotal, discountAmount, taxAmount, total, change }
  }, [cart, selectedTax, selectedDiscount, amountPaid])

  function addToCart(product) {
    setCart((prev) => {
      const existing = prev.find((item) => item.productId === product.id)
      if (existing) {
        if (existing.qty >= product.stock) return prev
        return prev.map((item) =>
          item.productId === product.id ? { ...item, qty: item.qty + 1 } : item,
        )
      }
      if (product.stock <= 0) return prev
      return [
        ...prev,
        {
          productId: product.id,
          name: product.name,
          price: Number(product.sellPrice),
          qty: 1,
          stock: product.stock,
          unit: product.unit?.symbol || '',
        },
      ]
    })
    setSearchTerm('')
  }

  function updateQty(productId, qty) {
    setCart((prev) =>
      prev
        .map((item) =>
          item.productId === productId
            ? { ...item, qty: Math.max(1, Math.min(qty, item.stock)) }
            : item,
        )
        .filter(Boolean),
    )
  }

  function removeItem(productId) {
    setCart((prev) => prev.filter((item) => item.productId !== productId))
  }

  function resetForm() {
    setCart([])
    setCustomerId('')
    setTaxId('')
    setDiscountId('')
    setPaymentMethod('CASH')
    setAmountPaid('')
  }

  async function handleCheckout() {
    setError('')
    if (cart.length === 0) {
      setError('Keranjang masih kosong')
      return
    }
    if (Number(amountPaid || 0) < totals.total) {
      setError('Jumlah bayar kurang dari total tagihan')
      return
    }
    setSubmitting(true)
    try {
      const payload = {
        customerId: customerId || null,
        taxId: taxId || null,
        discountId: discountId || null,
        paymentMethod,
        amountPaid: Number(amountPaid),
        items: cart.map((item) => ({ productId: item.productId, quantity: item.qty })),
      }
      const sale = await createSale(payload)
      const receipt = await getReceiptView(sale.id)
      setReceiptState({ saleId: sale.id, receipt })
      resetForm()
    } catch (err) {
      setError(errorMessage(err, 'Gagal menyimpan transaksi'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
      {/* Product search & results */}
      <div className="lg:col-span-2">
        <div className="rounded-xl bg-white p-4 shadow-sm">
          <input
            autoFocus
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Cari produk (nama, SKU, atau scan barcode)..."
            className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm focus:border-emerald-500 focus:outline-none"
          />
          {searchTerm && (
            <div className="mt-3 max-h-80 divide-y divide-slate-100 overflow-y-auto">
              {searchResults?.content?.length === 0 && (
                <div className="py-3 text-center text-sm text-slate-400">Produk tidak ditemukan</div>
              )}
              {searchResults?.content?.map((product) => (
                <button
                  key={product.id}
                  onClick={() => addToCart(product)}
                  disabled={product.stock <= 0}
                  className="flex w-full items-center justify-between py-2.5 text-left text-sm hover:bg-slate-50 disabled:opacity-40"
                >
                  <div>
                    <div className="font-medium text-slate-800">{product.name}</div>
                    <div className="text-xs text-slate-500">
                      {product.sku} &middot; Stok {product.stock} {product.unit?.symbol}
                    </div>
                  </div>
                  <div className="font-semibold text-emerald-600">{money(product.sellPrice)}</div>
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="mt-4 overflow-x-auto rounded-xl bg-white shadow-sm">
          <table className="min-w-full divide-y divide-slate-200 text-sm">
            <thead className="bg-slate-50">
              <tr>
                <th className="px-4 py-2 text-left font-medium text-slate-600">Produk</th>
                <th className="px-4 py-2 text-center font-medium text-slate-600">Qty</th>
                <th className="px-4 py-2 text-right font-medium text-slate-600">Harga</th>
                <th className="px-4 py-2 text-right font-medium text-slate-600">Subtotal</th>
                <th className="px-4 py-2" />
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {cart.length === 0 && (
                <tr>
                  <td colSpan={5} className="px-4 py-6 text-center text-slate-400">
                    Keranjang kosong &mdash; cari produk di atas untuk menambah
                  </td>
                </tr>
              )}
              {cart.map((item) => (
                <tr key={item.productId}>
                  <td className="px-4 py-2 text-slate-700">{item.name}</td>
                  <td className="px-4 py-2 text-center">
                    <div className="inline-flex items-center gap-1">
                      <button
                        onClick={() => updateQty(item.productId, item.qty - 1)}
                        className="h-6 w-6 rounded-md bg-slate-100 text-slate-600 hover:bg-slate-200"
                      >
                        −
                      </button>
                      <span className="w-8 text-center">{item.qty}</span>
                      <button
                        onClick={() => updateQty(item.productId, item.qty + 1)}
                        disabled={item.qty >= item.stock}
                        className="h-6 w-6 rounded-md bg-slate-100 text-slate-600 hover:bg-slate-200 disabled:opacity-40"
                      >
                        +
                      </button>
                    </div>
                  </td>
                  <td className="px-4 py-2 text-right">{money(item.price)}</td>
                  <td className="px-4 py-2 text-right font-medium">{money(item.price * item.qty)}</td>
                  <td className="px-4 py-2 text-right">
                    <button onClick={() => removeItem(item.productId)} className="text-red-500 hover:underline">
                      Hapus
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Checkout panel */}
      <div className="space-y-4">
        <div className="rounded-xl bg-white p-4 shadow-sm">
          <h2 className="mb-3 text-sm font-semibold text-slate-700">Detail Transaksi</h2>

          <label className="mb-3 block text-sm">
            <span className="mb-1 block text-slate-600">Pelanggan (opsional)</span>
            <select
              value={customerId}
              onChange={(e) => setCustomerId(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            >
              <option value="">- Umum -</option>
              {customers?.content?.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </label>

          <label className="mb-3 block text-sm">
            <span className="mb-1 block text-slate-600">Diskon</span>
            <select
              value={discountId}
              onChange={(e) => setDiscountId(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            >
              <option value="">- Tanpa Diskon -</option>
              {discounts?.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.name}
                </option>
              ))}
            </select>
          </label>

          <label className="mb-3 block text-sm">
            <span className="mb-1 block text-slate-600">Pajak</span>
            <select
              value={taxId}
              onChange={(e) => setTaxId(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            >
              <option value="">- Tanpa Pajak -</option>
              {taxes?.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.name}
                </option>
              ))}
            </select>
          </label>

          <label className="mb-3 block text-sm">
            <span className="mb-1 block text-slate-600">Metode Pembayaran</span>
            <select
              value={paymentMethod}
              onChange={(e) => setPaymentMethod(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            >
              <option value="CASH">Tunai</option>
              <option value="DEBIT">Kartu Debit</option>
              <option value="CREDIT">Kartu Kredit</option>
              <option value="QRIS">QRIS</option>
              <option value="TRANSFER">Transfer</option>
            </select>
          </label>

          <label className="block text-sm">
            <span className="mb-1 block text-slate-600">Jumlah Bayar</span>
            <input
              type="number"
              min="0"
              value={amountPaid}
              onChange={(e) => setAmountPaid(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            />
          </label>
        </div>

        <div className="space-y-2 rounded-xl bg-white p-4 shadow-sm text-sm">
          <div className="flex justify-between">
            <span className="text-slate-500">Subtotal</span>
            <span>{money(totals.subtotal)}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-slate-500">Diskon</span>
            <span>-{money(totals.discountAmount)}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-slate-500">Pajak</span>
            <span>{money(totals.taxAmount)}</span>
          </div>
          <div className="flex justify-between border-t border-slate-100 pt-2 text-base font-semibold">
            <span>Total</span>
            <span>{money(totals.total)}</span>
          </div>
          <div className="flex justify-between text-emerald-600">
            <span>Kembalian</span>
            <span>{money(Math.max(totals.change, 0))}</span>
          </div>

          {error && <div className="rounded-lg bg-red-50 px-3 py-2 text-red-700">{error}</div>}

          <button
            onClick={handleCheckout}
            disabled={submitting}
            className="mt-2 w-full rounded-lg bg-emerald-600 px-4 py-3 font-semibold text-white hover:bg-emerald-700 disabled:opacity-60"
          >
            {submitting ? 'Memproses...' : 'Bayar & Cetak Struk'}
          </button>
        </div>
      </div>

      {receiptState && (
        <ReceiptModal
          saleId={receiptState.saleId}
          receipt={receiptState.receipt}
          onClose={() => setReceiptState(null)}
        />
      )}
    </div>
  )
}
