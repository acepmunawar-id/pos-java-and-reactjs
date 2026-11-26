import client from './client'

async function downloadCsv(path, params, filename) {
  const response = await client.get(path, { params, responseType: 'blob' })
  const url = window.URL.createObjectURL(new Blob([response.data], { type: 'text/csv' }))
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', filename)
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}

export const reportsApi = {
  salesSummaryCsv: (from, to) =>
    downloadCsv('/reports/sales/summary.csv', { from, to }, 'laporan-penjualan.csv'),
  salesDetailCsv: (from, to) =>
    downloadCsv('/reports/sales/detail.csv', { from, to }, 'laporan-item-terjual.csv'),
  productStockCsv: () => downloadCsv('/reports/products/stock.csv', {}, 'laporan-stok-produk.csv'),
  financialCsv: (from, to) =>
    downloadCsv('/reports/financial.csv', { from, to }, 'laporan-keuangan.csv'),
  expensesCsv: (from, to) =>
    downloadCsv('/reports/expenses.csv', { from, to }, 'laporan-pengeluaran.csv'),
}
