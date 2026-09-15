import PartnerPage from './PartnerPage'
import { suppliersApi } from '../../api/resources'

export default function SuppliersPage() {
  return <PartnerPage title="Supplier" resourceApi={suppliersApi} queryKey="suppliers" />
}
