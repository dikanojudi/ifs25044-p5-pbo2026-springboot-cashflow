# ifs25044-p5-pbo2026-springboot-cashflow

Aplikasi web **Cashflow (catatan keuangan pribadi)** berbasis **Spring Boot**, **Maven**, **Vaadin**, dan **JDBC (PostgreSQL)**, dengan arsitektur **Clean Architecture** (domain, usecase, adapter, framework).

| | |
| --- | --- |
| Folder / artifact | `ifs25044-p5-pbo2026-springboot-cashflow` |
| Package | `org.delcom.ifs25044springbootcashflow` |
| Database | `pbo2026_cashflow` |

## Prasyarat

- JDK 25
- PostgreSQL, lalu buat database sebelum menjalankan aplikasi:

```sql
CREATE DATABASE pbo2026_cashflow;
```

## Konfigurasi `.env`

Salin `.env.example` menjadi `.env` (file `.env` diabaikan git), lalu sesuaikan `DB_PASSWORD`.

Akun admin di-seed otomatis dari `ROOT_USERNAME` / `ROOT_PASSWORD`. Upload (foto profil dan bukti transaksi) disimpan di `UPLOAD_DIR` dan dilayani di `/uploads/**`. `DotEnv` hanya mengisi system property jika variabel lingkungan dengan nama yang sama belum ada, sehingga di server nilai dari environment platform yang dipakai.

Opsional: `APP_COOKIE_SECRET` untuk kunci penandatangan cookie sesi. Jika kosong, kunci diturunkan dari `ROOT_PASSWORD`, `DB_PASSWORD`, dan `DB_DATABASE`.

## Menjalankan

```
.\mvnw.cmd spring-boot:run        # Windows
./mvnw spring-boot:run            # Linux / Mac
```

Buka http://localhost:8080. Build JAR produksi:

```
.\mvnw.cmd clean package -Pproduction
```

Uji dan coverage (Jacoco 100%, kecuali `framework/view` dan `framework/auth`):

```
.\mvnw.cmd clean verify
```

## Rute

| Path | View | Akses |
| --- | --- | --- |
| `/login`, `/register` | LoginView, RegisterView | Publik; jika sudah login dialihkan ke `/` |
| `/uploads/**`, `/icons/**`, `styles.css` | Aset statis | Publik |
| `/` | FinanceView | USER dan ADMIN |
| `/summary` | SummaryView (ringkasan per bulan) | USER dan ADMIN |
| `/profile` | ProfileView | USER dan ADMIN |
| `/users` | UsersView | ADMIN |
| `/user-transactions/{id}` | UserTransactionsView (read-only) | ADMIN |

## Struktur

```
domain/       entity, exception, repository (port), PasswordHasher
usecase/      AuthUseCase, FinanceUseCase, UserUseCase
adapter/      presenter, repository (InMemory dan JDBC), storage
framework/    auth (AuthSession), view (Vaadin)
config/       AppConfig (composition root)
```

## Format uang

`Rp 1,000,000.00`: pemisah ribuan koma dan dua digit desimal (negatif: `-Rp 25,000.00`).
