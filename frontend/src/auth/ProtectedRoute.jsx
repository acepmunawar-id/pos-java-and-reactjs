import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from './AuthContext'

/**
 * Guards a route subtree. With no `roles` prop it only requires login;
 * with `roles` it also enforces the role matrix (e.g. master data write
 * pages are SUPER_ADMIN/ADMIN only, user management is SUPER_ADMIN only).
 */
export default function ProtectedRoute({ roles }) {
  const { isAuthenticated, user } = useAuth()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }
  if (roles && !roles.includes(user.role)) {
    return <Navigate to="/forbidden" replace />
  }
  return <Outlet />
}
