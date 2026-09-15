# POS App

Aplikasi Point of Sale (kasir) untuk toko dengan 1 outlet - backend Spring Boot (Java), frontend React, PostgreSQL, siap dijalankan via Docker.

- 3 role pengguna (**Super Admin**, **Admin**, **Kasir/User**), masing-masing diarahkan ke halaman berbeda setelah login.
- Master data: produk, kategori, satuan, pelanggan, supplier, pajak, diskon, pengguna.
- Transaksi kasir dengan perhitungan otomatis (subtotal, diskon, pajak, kembalian) dan pengurangan stok atomik.
- Cetak struk: ESC/POS ke printer thermal (via Web Serial API) dengan fallback cetak browser.
- Ekspor laporan CSV (penjualan & stok).
- REST API terdokumentasi via Swagger/OpenAPI, dilengkapi automated test (JUnit/Mockito).

## Dokumentasi

| Dokumen | Isi |
|---|---|
| [`docs/BRD.md`](docs/BRD.md) | Business Requirement Document - latar belakang, ruang lingkup, flowchart, ERD, topologi deployment, matriks hak akses |
| [`docs/TECHNICAL.md`](docs/TECHNICAL.md) | Dokumentasi teknis - arsitektur, tech stack, cara setup, konfigurasi, deployment |
| [`docs/USER_GUIDE.md`](docs/USER_GUIDE.md) | Panduan pengguna non-teknis per role |

## Quickstart (Docker Compose)

```bash
cp .env.example .env
# (opsional) sunting .env - minimal ganti JWT_SECRET sebelum dipakai di produksi
docker compose up --build
```

| Layanan | URL |
|---|---|
| Frontend | http://localhost:8081 |
| Backend REST API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| PostgreSQL | `localhost:5432` |

### Akun default (ganti setelah instalasi pertama)

| Username | Password | Role |
|---|---|---|
| `superadmin` | `Password123!` | Super Admin |
| `admin` | `Password123!` | Admin |
| `kasir` | `Password123!` | Kasir (User) |

## Menjalankan Manual (development)

```bash
# Backend (butuh Java 17 + Maven + PostgreSQL lokal)
cd backend
mvn spring-boot:run

# Frontend (butuh Node.js 20+), di terminal terpisah
cd frontend
npm install
npm run dev   # http://localhost:5173
```

## Menjalankan Test

```bash
cd backend
mvn test
```

## Struktur Proyek

```
pos-app-java/
├── backend/    # Spring Boot REST API (layered architecture, JWT, JUnit, Swagger)
├── frontend/   # React + Vite + Tailwind CSS
├── docs/       # BRD, dokumentasi teknis, panduan pengguna
└── docker-compose.yml
```

Detail lengkap struktur folder & arsitektur ada di [`docs/TECHNICAL.md`](docs/TECHNICAL.md).
