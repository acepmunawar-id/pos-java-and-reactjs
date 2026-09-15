import axios from 'axios'

const TOKEN_KEY = 'pos.auth.token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
}

const client = axios.create({
  baseURL: '/api',
})

client.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired/invalid: clear it and bounce back to login.
      setToken(null)
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  },
)

/** Extracts the readable message from the backend's uniform ApiResponse error envelope. */
export function errorMessage(error, fallback = 'Terjadi kesalahan, silakan coba lagi') {
  return error?.response?.data?.message || error?.message || fallback
}

export default client
