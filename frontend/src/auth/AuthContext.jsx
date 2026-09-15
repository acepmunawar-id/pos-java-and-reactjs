import { createContext, useContext, useMemo, useState } from 'react'
import { login as loginRequest } from '../api/auth'
import { setToken, getToken } from '../api/client'

const USER_KEY = 'pos.auth.user'

const AuthContext = createContext(null)

function loadStoredUser() {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

/** Where each role lands right after login - this is the "redirect per role" requirement. */
export function landingPathFor(role) {
  if (role === 'USER') return '/pos'
  return '/dashboard'
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(loadStoredUser)

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: Boolean(user && getToken()),
      hasRole: (...roles) => Boolean(user) && roles.includes(user.role),
      async login(username, password) {
        const result = await loginRequest(username, password)
        const nextUser = {
          id: result.userId,
          username: result.username,
          fullName: result.fullName,
          role: result.role,
        }
        setToken(result.token)
        localStorage.setItem(USER_KEY, JSON.stringify(nextUser))
        setUser(nextUser)
        return nextUser
      },
      logout() {
        setToken(null)
        localStorage.removeItem(USER_KEY)
        setUser(null)
      },
    }),
    [user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
