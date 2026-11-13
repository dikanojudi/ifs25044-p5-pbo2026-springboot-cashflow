# Panduan Menjalankan Proyek Cashflow (ifs25044)

## 1. Buat database
Di pgAdmin (server `localhost`), buat database baru bernama `pbo2026_cashflow`
(klik kanan Databases, Create, Database...). Tabel `users` dan `transactions`
dibuat otomatis oleh aplikasi.

## 2. Buat `.env`
Salin `.env.example` menjadi `.env`, lalu pastikan `DB_PORT` dan `DB_PASSWORD`
sama dengan server PostgreSQL yang Anda pakai (di pgAdmin Anda: port 5432, password `postgres`).

## 3. Jalankan
```
.\mvnw.cmd clean
.\mvnw.cmd spring-boot:run
```
Run pertama lama (Vaadin membangun frontend, 5 sampai 10 menit). Buka http://localhost:8080.
Login admin: `admin` / `admin`. User biasa: daftar lewat `/register`.

## 4. Uji (sesuai checklist modul)
- Register, login, logout, refresh tetap login
- `/`, `/summary`, `/profile` tanpa login: dialihkan ke `/login`
- `/login` atau `/register` saat login: dialihkan ke `/`
- User biasa membuka `/users` atau `/user-transactions/1`: dialihkan ke `/`
- Tambah Pemasukan dan Pengeluaran, upload bukti, klik gambar untuk zoom
- Cari keterangan, urutkan lewat kotak "Urutkan" atau header kolom
- Saldo bertambah saat pemasukan, berkurang saat pengeluaran, kembali setelah hapus
- Jumlah kosong, 0, negatif, atau huruf: muncul notifikasi error
- Admin: menu Daftar Pengguna, tombol Lihat Transaksi (read-only)
- Restart aplikasi: data tetap ada

## 5. Uji otomatis dan coverage
```
.\mvnw.cmd clean verify
```
Pipeline Jenkins menolak build jika Jacoco < 100%, dan tanpa build sukses tidak ada deployment.

## 6. Deployment dan penilaian
- Nama job Jenkins harus `ifs25044-p5-pbo2026-springboot-cashflow`
  (Jenkinsfile memakai `JOB_NAME` sebagai nama aplikasi).
- Penilaian memakai URL `https://ifs25044-p5-pbo2026-springboot-cashflow...`,
  localhost tidak dihitung. Aplikasi harus sudah ter-deploy sebelum 17 November 2026 23:59.
- Pastikan akun uji yang dipakai penilai bisa login lewat form (`#login-username`,
  `#login-password`, `#login-submit`).
