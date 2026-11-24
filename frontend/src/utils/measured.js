// Helper untuk kategori/produk PER TAKARAN (mis. parfum refill, minyak, gula curah).
// Rumus sama dengan backend (Category.materialAmountFor / Product.unitCostFor).

/** Ubah input form ('' / '12,5' / 12.5) menjadi number atau null. */
export function toNumber(value) {
  if (value === '' || value === null || value === undefined) return null
  const n = Number(String(value).replace(',', '.'))
  return Number.isFinite(n) ? n : null
}

/** 51.667 -> "51,7" ; 10 -> "10" */
export function formatAmount(value, digits = 1) {
  return new Intl.NumberFormat('id-ID', { maximumFractionDigits: digits }).format(Number(value || 0))
}

export function sizeKey(sizeAmount) {
  return String(Number(sizeAmount))
}

/** true bila kategori memakai bahan campuran. */
export function categoryHasMixer(category) {
  return Number(category?.mixerRatio) > 0 && Number(category?.materialRatio) > 0
}

/** Takaran bahan utama & campuran, serta modal satu kemasan. */
export function computeSize(category, sizeAmount, materialPricePerUnit, packagingPrice = 0) {
  const size = Number(sizeAmount)
  const hasMixer = categoryHasMixer(category)
  const material = hasMixer
    ? (size * Number(category.materialRatio)) / (Number(category.materialRatio) + Number(category.mixerRatio))
    : size
  const mixer = hasMixer ? size - material : 0
  const cost =
    material * Number(materialPricePerUnit || 0) +
    mixer * Number(category?.mixerPricePerUnit || 0) +
    Number(packagingPrice || 0)
  return { material, mixer, cost }
}
