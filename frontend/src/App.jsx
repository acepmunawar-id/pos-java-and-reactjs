import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth, landingPathFor } from './auth/AuthContext'
import ProtectedRoute from './auth/ProtectedRoute'
import AppLayout from './layouts/AppLayout'
import LoginPage from './pages/login/LoginPage'
import DashboardPage from './pages/dashboard/DashboardPage'
import PosPage from './pages/pos/PosPage'
import SalesHistoryPage from './pages/sales-history/SalesHistoryPage'
import ReportsPage from './pages/reports/ReportsPage'
import ExpensesPage from './pages/expenses/ExpensesPage'
import DebtsPage from './pages/debts/DebtsPage'
import ProductsPage from './pages/master-data/ProductsPage'
import MasterCategoryPage from './pages/master-data/MasterCategoryPage'
import CustomersPage from './pages/master-data/CustomersPage'
import SuppliersPage from './pages/master-data/SuppliersPage'
import PricingPage from './pages/master-data/PricingPage'
import UsersPage from './pages/master-data/UsersPage'
import SettingsPage from './pages/settings/SettingsPage'
import ForbiddenPage from './pages/ForbiddenPage'

const MANAGER_ROLES = ['SUPER_ADMIN', 'ADMIN']

export default function App() {
  const { isAuthenticated, user } = useAuth()

  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/forbidden" element={<ForbiddenPage />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/pos" element={<PosPage />} />
          <Route path="/sales-history" element={<SalesHistoryPage />} />
          <Route path="/debts" element={<DebtsPage />} />

          <Route element={<ProtectedRoute roles={MANAGER_ROLES} />}>
            <Route path="/reports" element={<ReportsPage />} />
            <Route path="/expenses" element={<ExpensesPage />} />
            <Route path="/master-data/products" element={<ProductsPage />} />
            <Route path="/master-data/categories" element={<MasterCategoryPage />} />
            <Route path="/master-data/customers" element={<CustomersPage />} />
            <Route path="/master-data/suppliers" element={<SuppliersPage />} />
            <Route path="/master-data/pricing" element={<PricingPage />} />
          </Route>

          <Route element={<ProtectedRoute roles={['SUPER_ADMIN']} />}>
            <Route path="/master-data/users" element={<UsersPage />} />
            <Route path="/settings" element={<SettingsPage />} />
          </Route>
        </Route>
      </Route>

      <Route
        path="/"
        element={<Navigate to={isAuthenticated ? landingPathFor(user.role) : '/login'} replace />}
      />
      <Route
        path="*"
        element={<Navigate to={isAuthenticated ? landingPathFor(user.role) : '/login'} replace />}
      />
    </Routes>
  )
}
