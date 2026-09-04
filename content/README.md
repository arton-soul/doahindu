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

Sebelum rilis, aktifkan GitHub Pages dengan sumber **GitHub Actions**, kemudian
isi `CONTENT_MANIFEST_URL` pada `app/build.gradle` menggunakan pola:

```text
https://NAMA-PENGGUNA.github.io/NAMA-REPOSITORY/manifest.json
```

Database yang gagal checksum, melebihi 25 MiB, menggunakan schema yang tidak
didukung, rusak, memiliki `topic_id` duplikat, atau merujuk kategori yang tidak
ada akan ditolak. Database aktif sebelumnya disimpan sebagai file `.backup`
untuk rollback.
