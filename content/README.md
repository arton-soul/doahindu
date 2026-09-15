# Pembaruan Konten Doa Hindu

Sumber konten utama tetap berada di
`app/src/main/assets/doahindu1.sqlite` agar instalasi pertama dapat digunakan
tanpa internet.

Alur publikasi yang disiapkan:

1. Perbarui database SQLite tersebut tanpa mengubah `topic_id` konten lama.
2. Commit dan push ke branch `main` atau `master`.
3. GitHub Actions memeriksa integritas, tabel/kolom wajib, ID duplikat, dan
   relasi kategori.
4. Jika valid, workflow membuat database berversi dan `manifest.json`, lalu
   menerbitkannya melalui GitHub Pages.
5. Aplikasi memeriksa manifest maksimal sekali setiap 24 jam atau saat pengguna
   memilih menu **Perbarui Konten**.

Nomor `contentVersion` mengikuti nomor eksekusi workflow. Perubahan database,
workflow, atau dokumen ini memicu publikasi baru; tombol **Run workflow** juga
dapat digunakan untuk pengujian tanpa mengubah database sumber.

Sebelum rilis, aktifkan GitHub Pages dengan sumber **GitHub Actions**. Endpoint
produksi yang telah dikonfigurasi pada `app/build.gradle` adalah:

```text
https://arton-soul.github.io/doahindu/manifest.json
```

Database yang gagal checksum, melebihi 25 MiB, menggunakan schema yang tidak
didukung, rusak, memiliki `topic_id` duplikat, atau merujuk kategori yang tidak
ada akan ditolak. Database aktif sebelumnya disimpan sebagai file `.backup`
untuk rollback.

## Audit dan pratinjau ayat terstruktur

Sebelum menambahkan tabel ayat ke database utama, jalankan alat audit dalam mode
baca-saja:

```text
node --no-warnings content/tools/audit-content-sections.mjs
```

Alat mengenali nomor pada baris tersendiri (`1` atau `1.`) serta nomor dan awal
isi pada baris yang sama (`2. Sanjaya uvaca:` atau `25.bhisma...`). Hasilnya
ditulis ke:

- `content/reports/section-audit.json` untuk diagnosis format dan urutan nomor;
- `content/reports/content-sections-preview.json` untuk pratinjau baris
  `tbl_content_sections`.

Database sumber selalu dibuka dalam mode baca-saja. Pratinjau wajib diperiksa
sebelum dipakai pada tahap migrasi database.

Pengujian parser dapat dijalankan dengan:

```text
node --no-warnings --test content/tools/audit-content-sections.test.mjs
```

Setelah laporan dan pratinjau disetujui, migrasi dapat diperiksa tanpa mengubah
database:

```text
node --no-warnings content/tools/migrate-content-sections.mjs
```

Opsi `--apply` baru boleh diberikan setelah persetujuan pemilik. Sebelum
transaksi dimulai, alat membuat backup eksklusif di `.backups/` dan membatalkan
migrasi jika backup atau tabel tujuan sudah ada. Isi `tbl_topics` dibandingkan
sebelum dan sesudah migrasi untuk memastikan konten lama serta `topic_id` tidak
berubah.

Setelah tabel ayat tersedia, pratinjau data quote harian dapat diperiksa dengan:

```text
node --no-warnings content/tools/migrate-daily-quotes.mjs
```

Migrasi aktual memerlukan opsi `--apply` dan membuat backup terpisah sebelum
menambahkan `tbl_daily_quotes`. Pemilihan quote di aplikasi menggunakan hari
dalam tahun dan urutan baris aktif, sehingga hasilnya konsisten sepanjang hari
tanpa koneksi internet.

## Penyuntingan melalui spreadsheet/CSV

Sumber editorial berada di `content/source/`:

- `topics.csv` untuk ID, kategori, judul, dan isi lama;
- `content_sections.csv` untuk ayat terstruktur;
- `daily_quotes.csv` untuk daftar quote aktif.

Ketiga berkas mendukung sel multibaris dan dapat dibuka oleh aplikasi
spreadsheet. Untuk memvalidasi tanpa menghasilkan database:

```text
node --no-warnings content/tools/convert-content-csv.mjs
```

Laporan ditulis ke `content/reports/content-conversion-report.json`. Jika valid,
hasilkan database ke lokasi baru—bukan langsung menimpa database dasar:

```text
node --no-warnings content/tools/convert-content-csv.mjs --output content/generated/doahindu1.sqlite
```

Setelah hasil ditinjau dan dicadangkan, berkas keluaran dapat dijadikan aset
konten. GitHub Actions menjalankan mode `--check-base`, sehingga publikasi akan
ditolak jika CSV editorial tidak sama dengan SQLite yang hendak diterbitkan.
Gambar kategori dan topik tetap diwarisi dari database dasar.
