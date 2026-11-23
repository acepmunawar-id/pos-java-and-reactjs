import client from './client'

export const settingsApi = {
  list: async () => {
    const { data } = await client.get('/settings')
    return data.data
  },
  update: async (key, value) => {
    const { data } = await client.put(`/settings/${key}`, { value: String(value) })
    return data.data
  },
}
