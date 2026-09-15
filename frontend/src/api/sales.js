import client from './client'

export async function listSales({ from, to, page = 0, size = 10 } = {}) {
  const { data } = await client.get('/sales', { params: { from, to, page, size } })
  return data.data
}

export async function getSale(id) {
  const { data } = await client.get(`/sales/${id}`)
  return data.data
}

export async function createSale(payload) {
  const { data } = await client.post('/sales', payload)
  return data.data
}

export async function voidSale(id) {
  const { data } = await client.post(`/sales/${id}/void`)
  return data.data
}

export async function getReceiptView(saleId) {
  const { data } = await client.get(`/sales/${saleId}/receipt`)
  return data.data
}

/** Raw ESC/POS bytes for direct thermal printing (via Web Serial). */
export async function getReceiptEscPos(saleId) {
  const { data } = await client.get(`/sales/${saleId}/receipt/escpos`, {
    responseType: 'arraybuffer',
  })
  return new Uint8Array(data)
}
