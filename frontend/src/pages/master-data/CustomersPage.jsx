import PartnerPage from './PartnerPage'
import { customersApi } from '../../api/resources'

export default function CustomersPage() {
  return <PartnerPage title="Pelanggan" resourceApi={customersApi} queryKey="customers" />
}
