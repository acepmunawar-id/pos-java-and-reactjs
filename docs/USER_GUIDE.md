# Panduan Pengguna - POS App

Dokumen ini untuk pengguna sehari-hari aplikasi (pemilik toko, admin, kasir) - **tidak memerlukan latar belakang teknis**. Untuk detail arsitektur/instalasi, lihat [`TECHNICAL.md`](./TECHNICAL.md).

## 1. Cara Login

1. Buka aplikasi POS App di browser (disarankan Chrome atau Edge versi terbaru agar fitur cetak struk thermal berfungsi optimal).
2. Masukkan **Username** dan **Password**.
3. Klik **Masuk**.
4. Sistem otomatis mengarahkan Anda ke halaman yang sesuai dengan peran (role) akun Anda:
   - **Super Admin** & **Admin** → halaman **Dashboard**.
   - **Kasir** → langsung ke halaman **Kasir (POS)**, siap untuk melayani transaksi.

> Jika lupa password, minta Super Admin untuk mereset password Anda melalui menu Manajemen User.

## 2. Mengenal 3 Peran (Role) Pengguna

| Role | Bisa Apa Saja? |
|---|---|
| **Super Admin** | Semua fitur, termasuk membuat/menghapus akun pengguna lain dan menentukan role-nya. |
| **Admin** | Mengelola data produk, kategori, satuan, pelanggan, supplier, pajak, dan diskon; memproses transaksi; melihat semua riwayat transaksi; membatalkan transaksi; mengunduh laporan. **Tidak bisa** mengelola akun pengguna. |
| **Kasir (User)** | Memproses transaksi penjualan dan mencetak struk; melihat riwayat transaksi **miliknya sendiri** saja. **Tidak bisa** mengubah data master atau melihat laporan. |

Menu yang tampil di sisi kiri layar (sidebar) otomatis menyesuaikan dengan peran Anda - jika suatu menu tidak muncul, berarti peran Anda memang tidak memiliki akses ke fitur tersebut.

## 3. Menggunakan Halaman Kasir (POS) - Membuat Transaksi

Ini halaman yang paling sering dipakai kasir setiap hari.

1. **Cari produk**: ketik nama produk, kode SKU, atau scan barcode pada kotak pencarian di bagian atas.
2. Klik produk yang muncul di daftar hasil pencarian untuk **menambahkannya ke keranjang** di sebelah kiri.
3. Ulangi langkah 1-2 untuk setiap produk yang dibeli pelanggan.
4. Di dalam keranjang, Anda bisa:
   - Menambah/mengurangi jumlah (qty) dengan tombol `+` / `−`.
   - Menghapus produk dari keranjang dengan tombol **Hapus**.
5. Di panel kanan ("Detail Transaksi"), isi (semuanya opsional kecuali metode pembayaran):
   - **Pelanggan** - pilih jika transaksi atas nama pelanggan tertentu, atau biarkan "- Umum -".
   - **Diskon** - pilih skema diskon yang berlaku (jika ada).
   - **Pajak** - pilih tarif pajak yang berlaku (jika ada).
   - **Metode Pembayaran** - Tunai, Kartu Debit, Kartu Kredit, QRIS, atau Transfer.
   - **Jumlah Bayar** - nominal uang yang diterima dari pelanggan.
6. Sistem otomatis menghitung **Subtotal**, **Diskon**, **Pajak**, **Total**, dan **Kembalian** secara real-time.
7. Klik **Bayar & Cetak Struk**.
   - Jika stok produk tidak mencukupi atau jumlah bayar kurang dari total, sistem akan menampilkan pesan error - perbaiki lalu coba lagi.
8. Setelah transaksi berhasil, jendela **Struk Transaksi** akan muncul otomatis. Lanjut ke bagian 4 untuk mencetaknya.

## 4. Mencetak Struk

Setiap struk menampilkan dua tombol cetak:

- **🖨️ Cetak Thermal (ESC/POS)** - mengirim data struk langsung ke printer thermal (58/80mm) yang terhubung ke komputer/perangkat Anda via kabel USB atau serial.
  - Saat pertama kali dipakai, browser akan menampilkan jendela untuk **memilih perangkat printer** - pilih printer Anda lalu izinkan akses.
  - Fitur ini hanya berfungsi di **Chrome atau Edge** versi terbaru (browser lain belum mendukung teknologi Web Serial).
- **🖶 Cetak Browser (fallback)** - membuka dialog cetak bawaan browser, mencetak tampilan struk sebagai halaman biasa (cocok untuk printer non-thermal, atau saat printer thermal tidak tersedia/terhubung).

Gunakan tombol **Tutup** setelah selesai mencetak untuk kembali ke halaman kasir dan melayani transaksi berikutnya.

## 5. Melihat Riwayat Transaksi

Menu **Riwayat Transaksi** menampilkan daftar transaksi berdasarkan rentang tanggal yang dipilih.

- **Kasir** hanya melihat transaksi yang ia proses sendiri.
- **Admin/Super Admin** melihat seluruh transaksi semua kasir, dan dapat:
  - Klik **Lihat Struk** untuk membuka kembali & mencetak ulang struk transaksi tertentu.
  - Klik **Batalkan** untuk membatalkan transaksi (misalnya karena kesalahan input atau barang dikembalikan). Stok produk yang terkait akan otomatis dikembalikan, dan status transaksi berubah menjadi "Dibatalkan".

> Pembatalan transaksi tidak bisa dilakukan dua kali pada transaksi yang sama.

## 6. Mengelola Master Data (Admin & Super Admin)

Menu **Produk & Kategori**, **Pelanggan**, **Supplier**, dan **Pajak & Diskon** semuanya memakai pola yang sama:

1. Gunakan kotak **Cari...** untuk mencari data yang sudah ada.
2. Klik **+ Tambah** untuk membuat data baru - isi form yang muncul lalu klik **Simpan**.
3. Klik **Ubah** pada baris data untuk mengedit, atau **Hapus** untuk menghapusnya (akan diminta konfirmasi terlebih dahulu).
4. Gunakan tombol **Sebelumnya/Berikutnya** di bagian bawah tabel untuk berpindah halaman jika data cukup banyak.

Khusus halaman **Produk & Kategori**, terdapat 3 tab: **Produk**, **Kategori**, dan **Satuan** - kelola kategori dan satuan terlebih dahulu sebelum menambah produk baru agar bisa dipilih di form produk.

Khusus halaman **Pajak & Diskon**, centang kotak **Aktif** agar tarif pajak/skema diskon tersebut muncul sebagai pilihan di halaman kasir.

## 7. Mengelola Akun Pengguna (Khusus Super Admin)

Menu **Manajemen User** hanya tampil untuk akun Super Admin.

1. Klik **+ Tambah** untuk membuat akun baru: isi Username, Password, Nama Lengkap, Email (opsional), dan pilih **Role** (Super Admin / Admin / Kasir).
2. Untuk mengubah akun yang sudah ada, klik **Ubah** - kosongkan kolom Password jika tidak ingin menggantinya.
3. Nonaktifkan akun (hapus centang **Aktif**) untuk mencegah pengguna login tanpa harus menghapus datanya secara permanen.

## 8. Mengunduh Laporan (Admin & Super Admin)

Menu **Laporan** menyediakan 3 jenis unduhan dalam format **CSV** (dapat dibuka langsung di Excel/Google Sheets):

| Laporan | Isi |
|---|---|
| **Ringkasan Penjualan** | Satu baris per transaksi (nomor invoice, tanggal, kasir, subtotal, diskon, pajak, total, status) dalam rentang tanggal yang dipilih. |
| **Rincian Item Terjual** | Satu baris per produk yang terjual (untuk melihat produk apa saja yang paling laku). |
| **Stok Produk Saat Ini** | Daftar seluruh produk beserta harga dan sisa stok saat ini - untuk keperluan audit gudang/stok opname. |

Cara pakai: atur **rentang tanggal** (untuk laporan penjualan), lalu klik **⬇ Unduh CSV** pada kartu laporan yang diinginkan. File akan otomatis terunduh ke folder Download browser Anda.

## 9. Dashboard (Admin & Super Admin)

Halaman **Dashboard** menampilkan ringkasan cepat kondisi toko hari ini:

- Jumlah transaksi & total penjualan hari ini.
- Jumlah produk dengan **stok menipis** (stok ≤ stok minimum yang ditentukan) - segera lakukan restock jika daftar ini muncul.
- Grafik total penjualan 7 hari terakhir.

## 10. Pertanyaan Umum (FAQ)

**Q: Saya login sebagai Kasir tapi tidak melihat menu Produk/Laporan - apakah ada yang salah?**
A: Tidak, ini memang perilaku normal. Akun Kasir sengaja dibatasi hanya untuk transaksi dan riwayat transaksi miliknya sendiri (lihat tabel role di bagian 2).

**Q: Tombol "Cetak Thermal" tidak muncul/tidak berfungsi.**
A: Pastikan Anda menggunakan Chrome atau Edge versi terbaru. Jika tetap tidak bisa, gunakan tombol **Cetak Browser** sebagai cadangan.

**Q: Transaksi gagal dengan pesan "stok tidak mencukupi".**
A: Stok produk yang dipilih sudah habis atau kurang dari jumlah yang diminta. Kurangi jumlah, atau minta Admin memperbarui stok terlebih dahulu di menu Produk.

**Q: Bagaimana cara mengganti password akun saya sendiri?**
A: Saat ini penggantian password dilakukan oleh Super Admin melalui menu Manajemen User. Hubungi Super Admin Anda.
