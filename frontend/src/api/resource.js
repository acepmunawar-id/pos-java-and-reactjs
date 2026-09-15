import client from './client'

/**
 * Builds the standard CRUD calls (list/get/create/update/remove) for a REST
 * resource that follows the backend's uniform pattern. Every master-data page
 * (products, categories, units, customers, suppliers, taxes, discounts, users)
 * shares this exact shape, so we generate the client once instead of
 * hand-writing eight near-identical modules.
 */
export function createResourceApi(basePath) {
  return {
    list: async ({ search, page = 0, size = 10, sort } = {}) => {
      const { data } = await client.get(basePath, { params: { search, page, size, sort } })
      return data.data // PageResponse { content, pageNumber, pageSize, totalElements, totalPages, last }
    },
    listActive: async () => {
      const { data } = await client.get(`${basePath}/active`)
      return data.data
    },
    get: async (id) => {
      const { data } = await client.get(`${basePath}/${id}`)
      return data.data
    },
    create: async (payload) => {
      const { data } = await client.post(basePath, payload)
      return data.data
    },
    update: async (id, payload) => {
      const { data } = await client.put(`${basePath}/${id}`, payload)
      return data.data
    },
    remove: async (id) => {
      await client.delete(`${basePath}/${id}`)
    },
  }
}
