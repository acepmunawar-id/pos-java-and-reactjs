import client from './client'
import { createResourceApi } from './resource'

export const productsApi = createResourceApi('/products')
productsApi.lowStock = async () => {
  const { data } = await client.get('/products/low-stock')
  return data.data
}
productsApi.findByBarcode = async (barcode) => {
  const { data } = await client.get(`/products/barcode/${encodeURIComponent(barcode)}`)
  return data.data
}
export const categoriesApi = createResourceApi('/categories')
export const unitsApi = createResourceApi('/units')
export const customersApi = createResourceApi('/customers')
export const suppliersApi = createResourceApi('/suppliers')
export const taxesApi = createResourceApi('/taxes')
export const discountsApi = createResourceApi('/discounts')
export const usersApi = createResourceApi('/users')
