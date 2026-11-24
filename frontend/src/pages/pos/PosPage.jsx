import { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { productsApi, customersApi, taxesApi, discountsApi, categoriesApi } from '../../api/resources'
import { createSale, getReceiptView } from '../../api/sales'
import { errorMessage } from '../../api/client'
import ReceiptModal from '../../components/ReceiptModal'
import { formatAmount } from '../../utils/measured'

function money(v) {
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(v || 0)
}

/* warna badge per index kategori */
const BADGE_COLORS = [
  'bg-orange-100 text-orange-700',
  'bg-blue-100 text-blue-700',
  'bg-green-100 text-green-700',
  'bg-pink-100 text-pink-700',
  'bg-purple-100 text-purple-700',
  'bg-yellow-100 text-yellow-700',
  'bg-teal-100 text-teal-700',
  'bg-red-100 text-red-700',
]

export default function PosPage() {
  const [searchTerm, setSearchTerm]       = useState('')
  const [activeCatId, setActiveCatId]     = useState(null)
  const [cart, setCart]                   = useState([])
  const [customerId, setCustomerId]       = useState('')
  const [taxId, setTaxId]                 = useState('')
  const [discountId, setDiscountId]       = useState('')
  const [paymentMode, setPaymentMode]     = useState('CASH') // 'CASH' | 'DEBT'
  const [paymentMethod, setPaymentMethod] = useState('CASH')
  const [amountPaid, setAmountPaid]       = useState('')
  const [submitting, setSubmitting]       = useState(false)
  const [error, setError]                 = useState('')
  const [receiptState, setReceiptState]   = useState(null)
  const [showPayment, setShowPayment]     = useState(false)
  const [productPage, setProductPage]     = useState(0)

  /* ── Queries ── */
  const { data: categories } = useQuery({
    queryKey: ['pos', 'categories'],
    queryFn: () => categoriesApi.list({ size: 100 }),
  })

  const { data: products, isLoading: loadingProducts } = useQuery({
    queryKey: ['pos', 'products', activeCatId, searchTerm, productPage],
    queryFn: () => productsApi.list({ search: searchTerm || undefined, categoryId: activeCatId || undefined, size: 12, page: productPage }),
  })

  const { data: customers } = useQuery({ queryKey: ['pos', 'customers'], queryFn: () => customersApi.list({ size: 100 }) })
  const { data: taxes }     = useQuery({ queryKey: ['pos', 'taxes'],     queryFn: () => taxesApi.listActive() })
  const { data: discounts } = useQuery({ queryKey: ['pos', 'discounts'], queryFn: () => discountsApi.listActive() })

  const catList      = categories?.content ?? []
  const selectedTax  = taxes?.find(t => t.id === taxId)
  const selDiscount  = discounts?.find(d => d.id === discountId)

  /* ── Totals ── */
  const totals = useMemo(() => {
    const subtotal = cart.reduce((s, i) => s + i.price * i.qty, 0)
    let disc = 0
    if (selDiscount) {
      disc = selDiscount.type === 'PERCENTAGE' ? (subtotal * Number(selDiscount.value)) / 100 : Number(selDiscount.value)
      disc = Math.min(disc, subtotal)
    }
    const taxable   = subtotal - disc
    const taxAmt    = selectedTax ? (taxable * Number(selectedTax.rate)) / 100 : 0
    const total     = taxable + taxAmt
    return { subtotal, disc, taxAmt, total, change: Number(amountPaid || 0) - total }
  }, [cart, selectedTax, selDiscount, amountPaid])

  /* ── Cart ── */
  function addToCart(product, size) {
    const key   = size ? `${product.id}:${size.sizeAmount}` : product.id
    const stock = size ? size.available : product.stock
    setCart(prev => {
      const ex = prev.find(i => i.key === key)
      if (ex) return ex.qty >= stock ? prev : prev.map(i => i.key === key ? { ...i, qty: i.qty + 1 } : i)
      if (stock <= 0) return prev
      return [...prev, {
        key, productId: product.id, sizeAmount: size?.sizeAmount ?? null,
        name: size ? `${product.name} ${formatAmount(size.sizeAmount)} ${product.category.baseUnit}` : product.name,
        price: Number(size ? size.sellPrice : product.sellPrice), qty: 1, stock,
      }]
    })
  }
  const updateQty  = (key, qty) => setCart(p => p.map(i => i.key === key ? { ...i, qty: Math.max(1, Math.min(qty, i.stock)) } : i))
  const removeItem = (key)      => setCart(p => p.filter(i => i.key !== key))
  const resetForm  = ()         => { setCart([]); setCustomerId(''); setTaxId(''); setDiscountId(''); setPaymentMode('CASH'); setPaymentMethod('CASH'); setAmountPaid(''); setShowPayment(false) }

  async function handleCheckout() {
    setError('')
    if (cart.length === 0) { setError('Keranjang masih kosong'); return }
    if (paymentMode === 'DEBT' && !customerId) { setError('Pilih pelanggan untuk transaksi hutang'); return }
    if (paymentMode === 'CASH' && Number(amountPaid || 0) < totals.total) { setError('Jumlah bayar kurang dari total tagihan'); return }
    setSubmitting(true)
    try {
      const isDebt = paymentMode === 'DEBT'
      const payload = {
        customerId: customerId || null, taxId: taxId || null, discountId: discountId || null,
        paymentStatus: isDebt ? 'UNPAID' : 'PAID',
        paymentMethod: isDebt ? null : paymentMethod,
        amountPaid: isDebt ? 0 : Number(amountPaid),
        items: cart.map(i => ({ productId: i.productId, quantity: i.qty, sizeAmount: i.sizeAmount })),
      }
      const sale    = await createSale(payload)
      const receipt = await getReceiptView(sale.id)
      setReceiptState({ saleId: sale.id, receipt })
      resetForm()
    } catch (err) {
      setError(errorMessage(err, 'Gagal menyimpan transaksi'))
    } finally {
      setSubmitting(false)
    }
  }

  /* ── Render ── */
  return (
    <div className="flex h-[calc(100vh-7rem)] gap-4 overflow-hidden">

      {/* ══ PANEL KIRI — Produk ══ */}
      <div className="flex flex-1 flex-col gap-3 overflow-hidden">

        {/* Tab kategori */}
        <div className="flex gap-2 overflow-x-auto pb-1 shrink-0">
          {/* Semua */}
          <button
            onClick={() => { setActiveCatId(null); setProductPage(0) }}
            className={`flex shrink-0 flex-col items-center gap-1 rounded-xl border-2 px-4 py-2.5 text-center transition ${
              activeCatId === null
                ? 'border-blue-500 bg-blue-50'
                : 'border-transparent bg-white hover:border-slate-200'
            }`}
          >
            <span className={`flex h-9 w-9 items-center justify-center rounded-lg text-xl ${activeCatId === null ? 'bg-blue-500' : 'bg-slate-100'}`}>
              🛍️
            </span>
            <span className={`text-xs font-semibold ${activeCatId === null ? 'text-blue-600' : 'text-slate-700'}`}>Semua</span>
            <span className="text-[10px] text-slate-400">{products?.totalElements ?? 0} Item</span>
          </button>

          {catList.map((cat, idx) => {
            const isActive = activeCatId === cat.id
            return (
              <button
                key={cat.id}
                onClick={() => { setActiveCatId(cat.id); setProductPage(0) }}
                className={`flex shrink-0 flex-col items-center gap-1 rounded-xl border-2 px-4 py-2.5 text-center transition ${
                  isActive ? 'border-blue-500 bg-blue-50' : 'border-transparent bg-white hover:border-slate-200'
                }`}
              >
                <span className={`flex h-9 w-9 items-center justify-center rounded-lg text-xl ${isActive ? 'bg-blue-500' : 'bg-slate-100'}`}>
                  {['📦','🧴','🍶','💊','🛒','🧃','🫙','🗂️'][idx % 8]}
                </span>
                <span className={`whitespace-nowrap text-xs font-semibold ${isActive ? 'text-blue-600' : 'text-slate-700'}`}>{cat.name}</span>
                <span className="text-[10px] text-slate-400">{cat.productCount ?? '—'} Item</span>
              </button>
            )
          })}
        </div>

        {/* Search */}
        <div className="relative shrink-0">
          <input
            autoFocus
            value={searchTerm}
            onChange={e => { setSearchTerm(e.target.value); setProductPage(0) }}
            placeholder="Cari produk..."
            className="w-full rounded-xl border border-slate-200 bg-white px-4 py-2.5 pr-10 text-sm shadow-sm focus:border-blue-400 focus:outline-none"
          />
          <span className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-slate-400">🔍</span>
        </div>

        {/* Grid produk */}
        <div className="flex-1 overflow-y-auto">
          {loadingProducts ? (
            <div className="flex h-40 items-center justify-center text-sm text-slate-400">Memuat…</div>
          ) : products?.content?.length === 0 ? (
            <div className="flex h-40 items-center justify-center text-sm text-slate-400">Produk tidak ditemukan</div>
          ) : (
            <div className="grid grid-cols-2 gap-3 pb-3 sm:grid-cols-3 lg:grid-cols-4">
              {products?.content?.map((product, pIdx) => {
                const catIdx   = catList.findIndex(c => c.id === product.category?.id)
                const badgeClr = BADGE_COLORS[(catIdx >= 0 ? catIdx : pIdx) % BADGE_COLORS.length]
                const outOfStock = product.measured
                  ? product.sizes?.every(s => s.available <= 0)
                  : product.stock <= 0

                return (
                  <div
                    key={product.id}
                    onClick={() => !outOfStock && !product.measured && addToCart(product)}
                    className={`group flex flex-col rounded-xl bg-white shadow-sm transition hover:shadow-md ${outOfStock ? 'opacity-50' : 'cursor-pointer'}`}
                  >
                    {/* Foto */}
                    <div className="relative h-32 overflow-hidden rounded-t-xl bg-slate-50">
                      {product.imageUrl ? (
                        <img
                          src={product.imageUrl}
                          alt={product.name}
                          className="h-full w-full object-cover transition-transform duration-200 group-hover:scale-105"
                          onError={e => { e.target.style.display = 'none'; e.target.nextSibling.style.display = 'flex' }}
                        />
                      ) : null}
                      <div className={`absolute inset-0 items-center justify-center text-5xl ${product.imageUrl ? 'hidden' : 'flex'}`}>
                        🛍️
                      </div>
                      {outOfStock && (
                        <div className="absolute inset-0 flex items-center justify-center bg-black/40">
                          <span className="rounded-full bg-red-500 px-2 py-0.5 text-[10px] font-bold text-white">Habis</span>
                        </div>
                      )}
                    </div>

                    {/* Info */}
                    <div className="flex flex-1 flex-col p-2.5">
                      <p className="mb-1.5 line-clamp-1 text-xs font-semibold text-slate-800">{product.name}</p>

                      {product.measured ? (
                        <div className="mt-auto flex flex-wrap gap-1">
                          {product.sizes?.map(size => (
                            <button
                              key={size.sizeAmount}
                              disabled={size.available <= 0}
                              onClick={e => { e.stopPropagation(); addToCart(product, size) }}
                              className="rounded-md bg-blue-500 px-2 py-1 text-[10px] font-semibold text-white hover:bg-blue-600 disabled:opacity-40"
                            >
                              {formatAmount(size.sizeAmount)}{product.category?.baseUnit}
                            </button>
                          ))}
                        </div>
                      ) : (
                        <div className="mt-auto flex items-center justify-between gap-1">
                          <span className={`rounded-full px-1.5 py-0.5 text-[10px] font-medium ${badgeClr}`}>
                            {product.category?.name ?? '—'}
                          </span>
                          <span className="text-xs font-bold text-slate-800">{money(product.sellPrice)}</span>
                        </div>
                      )}
                    </div>
                  </div>
                )
              })}
            </div>
          )}

          {/* Pagination produk */}
          {products && products.totalPages > 1 && (
            <div className="mt-3 flex items-center justify-between">
              <button
                disabled={productPage === 0}
                onClick={() => setProductPage(p => p - 1)}
                className="flex h-8 w-8 items-center justify-center rounded-lg bg-white text-slate-600 shadow-sm hover:bg-slate-50 disabled:opacity-30"
              >‹</button>
              <span className="text-xs text-slate-500">
                {productPage + 1} / {products.totalPages}
              </span>
              <button
                disabled={products.last}
                onClick={() => setProductPage(p => p + 1)}
                className="flex h-8 w-8 items-center justify-center rounded-lg bg-white text-slate-600 shadow-sm hover:bg-slate-50 disabled:opacity-30"
              >›</button>
            </div>
          )}
        </div>
      </div>

      {/* ══ PANEL KANAN — Order ══ */}
      <div className="flex w-72 shrink-0 flex-col overflow-hidden rounded-2xl bg-white shadow-sm xl:w-80">

        {/* Header order */}
        <div className="border-b border-slate-100 p-4">
          <div className="flex items-start justify-between">
            <div>
              <p className="text-xs text-slate-400">Pelanggan</p>
              <select
                value={customerId}
                onChange={e => setCustomerId(e.target.value)}
                className="mt-0.5 w-full rounded-lg border border-slate-200 bg-slate-50 px-2 py-1.5 text-xs text-slate-700 focus:outline-none"
              >
                <option value="">— Umum —</option>
                {customers?.content?.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </div>
          </div>

          <div className="mt-3 grid grid-cols-2 gap-2">
            <select value={discountId} onChange={e => setDiscountId(e.target.value)}
              className="rounded-lg border border-slate-200 bg-slate-50 px-2 py-1.5 text-[11px] text-slate-600 focus:outline-none">
              <option value="">Tanpa Diskon</option>
              {discounts?.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
            <select value={taxId} onChange={e => setTaxId(e.target.value)}
              className="rounded-lg border border-slate-200 bg-slate-50 px-2 py-1.5 text-[11px] text-slate-600 focus:outline-none">
              <option value="">Tanpa Pajak</option>
              {taxes?.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
            </select>
          </div>
        </div>

        {/* Item list */}
        <div className="flex-1 overflow-y-auto p-4">
          {cart.length === 0 ? (
            <div className="flex h-full flex-col items-center justify-center gap-2 text-slate-300">
              <span className="text-5xl">🛒</span>
              <p className="text-sm font-medium">Belum ada item</p>
            </div>
          ) : (
            <div className="space-y-3">
              {cart.map(item => (
                <div key={item.key} className="flex items-center gap-2">
                  <div className="flex-1 min-w-0">
                    <p className="truncate text-xs font-semibold text-slate-700">{item.name}</p>
                    <p className="text-[11px] text-slate-400">{money(item.price)}</p>
                  </div>
                  <div className="flex items-center gap-1 shrink-0">
                    <button onClick={() => updateQty(item.key, item.qty - 1)}
                      className="flex h-6 w-6 items-center justify-center rounded-full bg-slate-100 text-slate-600 hover:bg-slate-200 text-sm">−</button>
                    <span className="w-5 text-center text-xs font-bold">{item.qty}</span>
                    <button onClick={() => updateQty(item.key, item.qty + 1)}
                      disabled={item.qty >= item.stock}
                      className="flex h-6 w-6 items-center justify-center rounded-full bg-blue-500 text-white hover:bg-blue-600 disabled:opacity-40 text-sm">+</button>
                  </div>
                  <span className="w-16 shrink-0 text-right text-xs font-bold text-slate-700">{money(item.price * item.qty)}</span>
                  <button onClick={() => removeItem(item.key)}
                    className="shrink-0 text-slate-300 hover:text-red-400 text-base leading-none">✕</button>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Totals + Bayar */}
        <div className="border-t border-slate-100 p-4">
          <div className="mb-3 space-y-1.5 text-xs text-slate-500">
            <div className="flex justify-between">
              <span>Subtotal</span>
              <span className="font-medium text-slate-700">{money(totals.subtotal)}</span>
            </div>
            {totals.disc > 0 && (
              <div className="flex justify-between text-green-600">
                <span>Diskon</span><span>-{money(totals.disc)}</span>
              </div>
            )}
            <div className="flex justify-between">
              <span>Pajak</span>
              <span className="font-medium text-slate-700">{money(totals.taxAmt)}</span>
            </div>
            <div className="flex justify-between border-t border-slate-100 pt-2 text-sm font-bold text-slate-800">
              <span>TOTAL</span><span>{money(totals.total)}</span>
            </div>
          </div>

          {/* Metode bayar + jumlah */}
          {showPayment && (
            <div className="mb-3 space-y-2">
              {/* Toggle Tunai / Hutang */}
              <div className="flex rounded-lg border border-slate-200 overflow-hidden text-xs font-semibold">
                <button type="button"
                  onClick={() => setPaymentMode('CASH')}
                  className={`flex-1 py-2 transition ${paymentMode === 'CASH' ? 'bg-blue-600 text-white' : 'bg-white text-slate-500 hover:bg-slate-50'}`}>
                  💵 Tunai
                </button>
                <button type="button"
                  onClick={() => setPaymentMode('DEBT')}
                  className={`flex-1 py-2 transition ${paymentMode === 'DEBT' ? 'bg-orange-500 text-white' : 'bg-white text-slate-500 hover:bg-slate-50'}`}>
                  📋 Hutang
                </button>
              </div>

              {paymentMode === 'DEBT' ? (
                <div className="rounded-lg bg-orange-50 border border-orange-200 px-3 py-2 text-xs text-orange-700">
                  {customerId
                    ? <span>✓ Hutang atas nama <strong>{customers?.content?.find(c => c.id === customerId)?.name}</strong></span>
                    : <span>⚠ Pilih pelanggan di atas untuk transaksi hutang</span>
                  }
                </div>
              ) : (
                <>
                  <select value={paymentMethod} onChange={e => setPaymentMethod(e.target.value)}
                    className="w-full rounded-lg border border-slate-200 px-3 py-2 text-xs focus:outline-none">
                    <option value="CASH">Tunai</option>
                    <option value="DEBIT">Kartu Debit</option>
                    <option value="CREDIT">Kartu Kredit</option>
                    <option value="QRIS">QRIS</option>
                    <option value="TRANSFER">Transfer</option>
                  </select>
                  <input type="number" min="0" value={amountPaid} onChange={e => setAmountPaid(e.target.value)}
                    placeholder="Jumlah bayar"
                    className="w-full rounded-lg border border-slate-200 px-3 py-2 text-xs focus:border-blue-400 focus:outline-none" />
                  {Number(amountPaid || 0) >= totals.total && totals.total > 0 && (
                    <div className="flex justify-between rounded-lg bg-green-50 px-3 py-2 text-xs font-semibold text-green-700">
                      <span>Kembalian</span><span>{money(Math.max(totals.change, 0))}</span>
                    </div>
                  )}
                </>
              )}
            </div>
          )}

          {error && <p className="mb-2 rounded-lg bg-red-50 px-3 py-2 text-xs text-red-600">{error}</p>}

          {!showPayment ? (
            <button
              onClick={() => { if (cart.length === 0) { setError('Keranjang masih kosong'); return } setError(''); setShowPayment(true) }}
              className="w-full rounded-xl bg-blue-600 py-3 text-sm font-bold text-white hover:bg-blue-700 transition active:scale-95"
            >
              Lanjut ke Pembayaran
            </button>
          ) : (
            <div className="flex gap-2">
              <button onClick={() => setShowPayment(false)}
                className="flex-1 rounded-xl border border-slate-200 py-3 text-xs font-semibold text-slate-600 hover:bg-slate-50 transition">
                Kembali
              </button>
              <button onClick={handleCheckout} disabled={submitting}
                className={`flex-1 rounded-xl py-3 text-xs font-bold text-white disabled:opacity-60 transition active:scale-95 ${paymentMode === 'DEBT' ? 'bg-orange-500 hover:bg-orange-600' : 'bg-blue-600 hover:bg-blue-700'}`}>
                {submitting ? 'Proses…' : paymentMode === 'DEBT' ? 'Simpan Hutang' : 'Bayar & Struk'}
              </button>
            </div>
          )}
        </div>
      </div>

      {receiptState && (
        <ReceiptModal saleId={receiptState.saleId} receipt={receiptState.receipt} onClose={() => setReceiptState(null)} />
      )}
    </div>
  )
}
