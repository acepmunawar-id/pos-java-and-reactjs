// Central menu definition - each item's `roles` decides who sees it in the
// sidebar, which is how "each role sees a different set of pages" is realized
// on top of the shared layout.
export const NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard', icon: '📊', roles: ['SUPER_ADMIN', 'ADMIN', 'USER'] },
  { to: '/pos', label: 'Kasir (POS)', icon: '🧾', roles: ['SUPER_ADMIN', 'ADMIN', 'USER'] },
  { to: '/sales-history', label: 'Riwayat Transaksi', icon: '🕒', roles: ['SUPER_ADMIN', 'ADMIN', 'USER'] },
  { to: '/master-data/products',   label: 'Produk',   icon: '📦', roles: ['SUPER_ADMIN', 'ADMIN'] },
  { to: '/master-data/categories', label: 'Kategori', icon: '🗂️', roles: ['SUPER_ADMIN', 'ADMIN'] },
  { to: '/master-data/customers', label: 'Pelanggan', icon: '🧑‍🤝‍🧑', roles: ['SUPER_ADMIN', 'ADMIN'] },
  { to: '/master-data/suppliers', label: 'Supplier', icon: '🚚', roles: ['SUPER_ADMIN', 'ADMIN'] },
  { to: '/master-data/pricing', label: 'Pajak & Diskon', icon: '💲', roles: ['SUPER_ADMIN', 'ADMIN'] },
  { to: '/master-data/users', label: 'Manajemen User', icon: '👤', roles: ['SUPER_ADMIN'] },
  { to: '/settings',          label: 'Pengaturan',     icon: '⚙️',  roles: ['SUPER_ADMIN'] },
  { to: '/expenses', label: 'Pengeluaran', icon: '💸', roles: ['SUPER_ADMIN', 'ADMIN'] },
  { to: '/reports',  label: 'Laporan',     icon: '📈', roles: ['SUPER_ADMIN', 'ADMIN'] },
]

export const ROLE_LABEL = {
  SUPER_ADMIN: 'Super Admin',
  ADMIN: 'Admin',
  USER: 'Kasir',
}
