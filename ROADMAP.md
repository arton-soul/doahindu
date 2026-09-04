# Roadmap Pengembangan Aplikasi Doa Hindu

Dokumen ini menjadi catatan utama audit, perencanaan, pelaksanaan, dan verifikasi pengembangan aplikasi Doa Hindu.

## Aturan Penggunaan

- `[x]` berarti pekerjaan sudah dilakukan dan, untuk implementasi, sudah diverifikasi.
- `[ ]` berarti pekerjaan belum dilakukan atau belum selesai diverifikasi.
- Setiap perubahan kode, konfigurasi, aset, database, atau layanan eksternal harus mendapat persetujuan pemilik aplikasi terlebih dahulu.
- Persetujuan atas dokumen perencanaan ini tidak otomatis menjadi persetujuan untuk mengimplementasikan seluruh item.
- Setiap pekerjaan yang disetujui harus dicatat pada bagian Riwayat Pekerjaan.
- Data pengguna, terutama favorit, riwayat baca, dan pengaturan, tidak boleh hilang selama migrasi.

## Kondisi Awal

- Nama aplikasi: Doa Hindu
- Application ID: `com.dearyoti.doahindu`
- Versi aplikasi: `3.4.1` (`versionCode 12`)
- Bahasa utama kode: Java
- UI: Android Views/XML
- `minSdk`: 23
- `compileSdk`: 35
- `targetSdk`: 35
- Database awal: `app/src/main/assets/doahindu1.sqlite`
- Ukuran database awal: sekitar 4 MB
- Jumlah kategori: 7
- Jumlah topik/doa: 59
- Jumlah konten terbaru: 3

## Status Audit

- [x] Memeriksa struktur proyek Android.
- [x] Memeriksa konfigurasi Gradle, SDK, dan versi aplikasi.
- [x] Memeriksa Android Manifest dan permission.
- [x] Mengidentifikasi fitur aplikasi yang sudah tersedia.
- [x] Memeriksa kelas database dan mekanisme penyalinan database dari assets.
- [x] Memeriksa skema dan integritas SQLite (`integrity_check: ok`).
- [x] Menghitung jumlah kategori, topik, dan konten terbaru.
- [x] Memeriksa keterkaitan topik dengan kategori.
- [x] Memeriksa judul dan isi kosong.
- [x] Menemukan satu judul duplikat: `Sarascamuscaya 3`.
- [x] Mengidentifikasi perlunya verifikasi ejaan `Sarascamuscaya`/`Sarasamuccaya`.
- [x] Mengidentifikasi bahwa sebagian BLOB gambar hanya berukuran 8 byte dan perlu divalidasi.
- [x] Memeriksa penyimpanan favorit dan riwayat di SQLite serta SharedPreferences.
- [x] Mengidentifikasi risiko data pengguna tertimpa ketika database konten diperbarui.
- [x] Menilai kelayakan GitHub sebagai sumber pembaruan konten.
- [x] Memeriksa persyaratan target API Google Play yang berlaku saat audit.
- [x] Menyusun rekomendasi awal fitur dan modernisasi.

## Fase 0 — Persiapan dan Baseline

Tujuan: menyediakan kondisi awal yang dapat dibandingkan sebelum perubahan dilakukan.

- [x] Mendapatkan persetujuan untuk memulai Fase 0.
- [x] Membuat atau memastikan tersedia backup source code dan database.
- [x] Memastikan proyek berada dalam repositori Git yang valid.
- [x] Menambahkan `.gitignore` yang sesuai tanpa memasukkan credential atau file lokal.
- [x] Mendokumentasikan konfigurasi build development dan release.
- [x] Membuat build debug dari kondisi awal.
- [ ] Menjalankan aplikasi pada emulator/perangkat API minimum yang dipilih.
- [ ] Menjalankan aplikasi pada Android versi terbaru.
- [ ] Mencatat screenshot dan alur utama sebagai baseline.
- [ ] Menguji kategori, pencarian, baca doa, favorit, riwayat, berbagi, notifikasi, dan iklan.
- [x] Mencatat temuan baseline yang dapat diperiksa dari build dan lint.

Kriteria selesai:

- [x] Source dan data awal memiliki backup lokal yang dapat dipulihkan.
- [ ] Build awal berhasil dan perilaku utama telah dicatat.
- [ ] Daftar bug baseline tersedia.

## Fase 1 — Pembaruan SDK dan Build System

Tujuan: membuat aplikasi kompatibel dengan SDK/API terbaru dan persyaratan Google Play.

- [ ] Mendapatkan persetujuan untuk memulai Fase 1.
- [ ] Memastikan versi API terbaru dan tenggat Google Play saat implementasi dimulai.
- [ ] Memperbarui `compileSdk` ke API yang disepakati (rencana awal: API 36).
- [ ] Memperbarui `targetSdk` ke API yang disepakati (rencana awal: API 36).
- [ ] Meninjau apakah `minSdk 23` tetap dipertahankan.
- [ ] Memastikan kompatibilitas Android Gradle Plugin, Gradle, dan JDK.
- [ ] Memperbarui dependency AndroidX, Material, Firebase, Google Mobile Ads, dan test.
- [ ] Menghapus atau mengganti dependency lama yang sudah tidak diperlukan.
- [ ] Menguji build debug dan release dengan R8/minification.
- [ ] Menangani warning dan error yang relevan dari lint/build.
- [ ] Menguji perubahan perilaku Android 16.

Kriteria selesai:

- [ ] Debug dan release build berhasil.
- [ ] Aplikasi berjalan pada versi Android minimum dan terbaru yang disepakati.
- [ ] Tidak ada crash pada alur utama.
- [ ] Target API memenuhi persyaratan Google Play saat rilis.

## Fase 2 — UI, Edge-to-Edge, dan Aksesibilitas

Tujuan: memastikan tampilan nyaman, adaptif, dan dapat diakses.

- [ ] Mendapatkan persetujuan untuk memulai Fase 2.
- [ ] Menerapkan edge-to-edge dan penanganan `WindowInsets`.
- [ ] Menguji status bar, navigation bar, dan display cutout.
- [ ] Menghapus pemaksaan `fontScale = 1`.
- [ ] Menambahkan dukungan ukuran teks pengguna.
- [ ] Menambahkan pengaturan ukuran teks khusus isi doa jika disetujui.
- [ ] Memeriksa kontras warna untuk mode terang dan gelap.
- [ ] Menambahkan `contentDescription` pada elemen interaktif yang memerlukannya.
- [ ] Memastikan ukuran target sentuh memadai.
- [ ] Membuat layout adaptif untuk ponsel, tablet, dan foldable.
- [ ] Meninjau kembali penguncian orientasi portrait.
- [ ] Memastikan teks tidak terpotong saat font diperbesar.
- [ ] Menguji dengan TalkBack.

Kriteria selesai:

- [ ] Tampilan tidak tertutup system bar.
- [ ] Aplikasi dapat digunakan dengan ukuran font besar.
- [ ] Alur utama dapat digunakan dengan TalkBack.
- [ ] Layout utama berfungsi pada ponsel dan layar besar.

## Fase 3 — Modernisasi Arsitektur Data Lokal

Tujuan: memisahkan konten aplikasi dari data pribadi pengguna dan membuat migrasi aman.

- [ ] Mendapatkan persetujuan untuk memulai Fase 3.
- [ ] Menentukan apakah menggunakan Room atau tetap memakai SQLite dengan lapisan repository.
- [ ] Menetapkan ID konten permanen yang tidak berubah antarversi.
- [ ] Memisahkan database konten dari data pengguna.
- [ ] Memindahkan favorit ke penyimpanan lokal pengguna yang terpisah.
- [ ] Memindahkan riwayat baca ke penyimpanan lokal pengguna yang terpisah.
- [ ] Menghindari penyimpanan objek konten lengkap di SharedPreferences.
- [ ] Menambahkan nomor versi skema database.
- [ ] Menambahkan strategi migrasi dari struktur lama.
- [ ] Menambahkan indeks untuk relasi kategori, pencarian, dan urutan.
- [ ] Mengaktifkan serta menguji foreign key.
- [ ] Memindahkan operasi database dari main/UI thread.
- [ ] Mengganti query pencarian concatenation dengan parameter query.
- [ ] Mengubah navigasi detail agar hanya mengirim ID, bukan BLOB dan isi doa melalui Intent.
- [ ] Menambahkan migration test dan repository test.

Kriteria selesai:

- [ ] Favorit dan riwayat lama tetap tersedia setelah migrasi.
- [ ] Database konten dapat diganti tanpa menghapus data pengguna.
- [ ] Tidak ada operasi database berat pada UI thread.
- [ ] Seluruh migration test lulus.

## Fase 4 — Sistem Pembaruan Konten dari GitHub

Tujuan: memungkinkan pembaruan konten tanpa menerbitkan APK baru sambil mempertahankan mode offline.

Rancangan awal:

```text
GitHub Pages atau GitHub Releases
├── manifest.json
├── content-vN.zip
└── images atau metadata tambahan
        ↓ HTTPS
Aplikasi memeriksa versi
        ↓
Unduh ke file sementara
        ↓
Validasi ukuran, checksum, dan skema
        ↓
Import atau penggantian atomik
        ↓
Database konten offline aktif
```

- [ ] Mendapatkan persetujuan untuk memulai Fase 4.
- [ ] Memilih GitHub Pages atau GitHub Releases sebagai sumber resmi.
- [ ] Menentukan apakah source konten menggunakan JSON, Markdown, atau SQLite.
- [ ] Menentukan struktur `manifest.json`.
- [ ] Menambahkan `schemaVersion`, `contentVersion`, `publishedAt`, dan `minimumAppVersion`.
- [ ] Menyediakan URL paket konten berversi.
- [ ] Menghasilkan dan memverifikasi checksum SHA-256.
- [ ] Menentukan ukuran unduhan maksimum.
- [ ] Mengunduh hanya melalui HTTPS.
- [ ] Menyimpan unduhan ke file sementara.
- [ ] Memvalidasi file SQLite atau paket konten sebelum digunakan.
- [ ] Menerapkan penggantian/import data secara atomik.
- [ ] Menyediakan rollback otomatis jika pembaruan gagal.
- [ ] Menampilkan status pembaruan dengan bahasa yang mudah dipahami.
- [ ] Mempertahankan database bawaan APK untuk penggunaan offline pertama.
- [ ] Menambahkan pengaturan pemeriksaan pembaruan otomatis/manual.
- [ ] Menambahkan GitHub Actions untuk validasi dan pembuatan paket konten jika disetujui.
- [ ] Menguji pembaruan normal, gagal unduh, checksum salah, skema salah, dan rollback.

Kriteria selesai:

- [ ] Konten dapat diperbarui tanpa update APK.
- [ ] Aplikasi tetap dapat dibuka tanpa internet.
- [ ] Paket rusak atau tidak sah tidak pernah menggantikan database aktif.
- [ ] Favorit, riwayat, dan pengaturan pengguna tetap utuh.
- [ ] Versi database sebelumnya dapat dipulihkan jika pembaruan gagal.

## Fase 5 — Perbaikan Konten

Tujuan: meningkatkan mutu, konsistensi, dan keterlacakan isi doa.

- [ ] Mendapatkan persetujuan untuk memulai Fase 5.
- [ ] Memeriksa duplikat `Sarascamuscaya 3`.
- [ ] Memverifikasi ejaan `Sarascamuscaya` atau `Sarasamuccaya` dengan editor konten.
- [ ] Memvalidasi seluruh BLOB gambar berukuran sangat kecil.
- [ ] Memeriksa doa yang sangat pendek untuk memastikan isinya lengkap.
- [ ] Menambahkan sumber/referensi pada setiap konten jika tersedia.
- [ ] Menambahkan urutan konten eksplisit.
- [ ] Menambahkan tanggal dibuat dan diperbarui.
- [ ] Menambahkan status draft/publikasi pada source konten.
- [ ] Menambahkan slug atau UUID permanen bila diperlukan.
- [ ] Menetapkan pedoman ejaan, kapitalisasi, transliterasi, dan HTML yang diperbolehkan.
- [ ] Memastikan konten telah ditinjau oleh pihak yang memahami materi keagamaan.

Kriteria selesai:

- [ ] Tidak ada duplikat yang tidak disengaja.
- [ ] Tidak ada gambar rusak atau isi terpotong.
- [ ] Konten memiliki identitas permanen dan metadata minimum.
- [ ] Konten yang dipublikasikan telah melalui pemeriksaan editorial.

## Fase 6 — Fitur Membaca dan Pencarian

Tujuan: meningkatkan manfaat utama aplikasi bagi pembaca.

- [ ] Mendapatkan persetujuan untuk memulai Fase 6.
- [ ] Menambahkan pilihan ukuran huruf isi doa.
- [ ] Menambahkan pilihan jarak baris.
- [ ] Menambahkan tema terang, gelap, dan mengikuti sistem.
- [ ] Menambahkan opsi menjaga layar tetap menyala saat membaca.
- [ ] Menyimpan posisi baca terakhir.
- [ ] Menambahkan pencarian judul dan isi doa.
- [ ] Menyorot kata yang ditemukan.
- [ ] Menambahkan filter kategori.
- [ ] Menambahkan histori pencarian jika disetujui.
- [ ] Menambahkan tampilan kosong dan pesan error yang informatif.
- [ ] Memperbaiki teks berbagi agar formatnya bersih dan konsisten.

Kriteria selesai:

- [ ] Pengaturan baca tersimpan dan dipulihkan dengan benar.
- [ ] Pencarian judul dan isi memberikan hasil yang relevan.
- [ ] Fitur baca tetap berfungsi secara offline.

## Fase 7 — Fitur Tambahan Opsional

Setiap kelompok fitur berikut memerlukan persetujuan terpisah.

### Audio doa/mantra

- [ ] Menentukan sumber audio dan hak penggunaannya.
- [ ] Menambahkan play, pause, seek, dan kecepatan pemutaran.
- [ ] Menambahkan unduhan audio untuk mode offline.
- [ ] Menambahkan pengelolaan storage dan penghapusan unduhan.

### Pengingat dan doa harian

- [ ] Menambahkan doa harian.
- [ ] Menambahkan pengingat lokal yang bersifat opt-in.
- [ ] Meminta izin notifikasi setelah konteksnya dijelaskan kepada pengguna.
- [ ] Menambahkan pengaturan hari, waktu, dan jenis pengingat.

### Favorit dan catatan

- [ ] Menambahkan folder atau koleksi favorit.
- [ ] Menambahkan catatan pribadi pada doa.
- [ ] Menambahkan ekspor/backup lokal jika disetujui.

### Bahasa dan teks

- [ ] Menambahkan teks asli bila tersedia.
- [ ] Menambahkan transliterasi Latin.
- [ ] Menambahkan arti atau terjemahan.
- [ ] Menambahkan pilihan tampilan bagian teks.

### Pelaporan koreksi

- [ ] Menambahkan aksi laporkan koreksi.
- [ ] Menyertakan ID dan versi konten dalam laporan.
- [ ] Menentukan kanal laporan: email, formulir, atau GitHub Issues.

## Fase 8 — Notifikasi, Iklan, Privasi, dan Keamanan

Tujuan: memastikan integrasi eksternal aman dan mematuhi kebijakan distribusi.

- [ ] Mendapatkan persetujuan untuk memulai Fase 8.
- [ ] Menambahkan deep link agar notifikasi dapat membuka doa tertentu.
- [ ] Menggunakan notification ID yang sesuai agar notifikasi tidak selalu saling menimpa.
- [ ] Memperbaiki waktu dan konteks permintaan izin notifikasi.
- [ ] Meninjau frekuensi dan posisi interstitial.
- [ ] Menambahkan atau memperbarui consent iklan melalui UMP jika diperlukan.
- [ ] Memastikan mode iklan sesuai consent pengguna.
- [ ] Meninjau penggunaan permission `AD_ID`.
- [ ] Meninjau `android:allowBackup` dan aturan backup.
- [ ] Memastikan tidak ada secret sensitif dalam source/repository publik.
- [ ] Memperbarui kebijakan privasi.
- [ ] Memperbarui formulir Data Safety Google Play.
- [ ] Memastikan URL eksternal hanya menggunakan HTTPS.
- [ ] Menguji konfigurasi release dan R8 untuk Firebase serta Ads.

Kriteria selesai:

- [ ] Alur consent sesuai wilayah dan kebijakan yang berlaku saat rilis.
- [ ] Kebijakan privasi sesuai dengan perilaku aplikasi aktual.
- [ ] Notifikasi dan iklan tidak mengganggu fungsi utama membaca.
- [ ] Tidak ada credential rahasia yang dipublikasikan.

## Fase 9 — Pengujian dan Persiapan Rilis

- [ ] Mendapatkan persetujuan untuk memulai Fase 9.
- [ ] Menambahkan unit test query dan repository.
- [ ] Menambahkan database migration test.
- [ ] Menambahkan test pembaruan konten dan rollback.
- [ ] Menambahkan UI test untuk alur utama.
- [ ] Menguji instalasi baru.
- [ ] Menguji upgrade dari versi aplikasi sebelumnya.
- [ ] Menguji kondisi offline, internet lambat, dan unduhan terputus.
- [ ] Menguji rotasi, multi-window, tablet, dan foldable.
- [ ] Menguji TalkBack dan ukuran font besar.
- [ ] Menguji notifikasi pada Android yang didukung.
- [ ] Menguji iklan test sebelum menggunakan unit iklan produksi.
- [ ] Menjalankan Android lint dan meninjau hasilnya.
- [ ] Menguji release build dengan R8/minification.
- [ ] Memperbarui version code, version name, changelog, dan store listing.
- [ ] Memastikan backup rilis dan mapping R8 tersimpan aman.
- [ ] Melakukan staged rollout jika tersedia.

Kriteria selesai:

- [ ] Seluruh test wajib lulus.
- [ ] Tidak ada bug blocker atau crash yang diketahui.
- [ ] Upgrade tidak menghilangkan data pengguna.
- [ ] Paket rilis memenuhi persyaratan Google Play.

## Keputusan yang Perlu Persetujuan Pemilik

- [ ] Target SDK/API final.
- [ ] Tetap menggunakan Java/XML atau migrasi bertahap ke Kotlin/Compose.
- [ ] Room atau SQLite dengan repository khusus.
- [ ] Format source konten: JSON, Markdown, atau SQLite.
- [ ] GitHub Pages atau GitHub Releases.
- [ ] Apakah pembaruan konten otomatis, manual, atau keduanya.
- [ ] Fitur baru yang masuk rilis pertama.
- [ ] Dukungan audio dan sumber audionya.
- [ ] Strategi iklan dan consent.
- [ ] Dukungan tablet/foldable dan orientasi layar.
- [ ] Mekanisme backup/sinkronisasi data pengguna.

## Riwayat Pekerjaan

Gunakan format berikut untuk setiap pekerjaan yang dilaksanakan:

```text
Tanggal:
Persetujuan pemilik:
Fase/item:
Perubahan:
File yang berubah:
Pengujian:
Hasil:
Catatan/risiko tersisa:
```

### Entri 1 — Audit dan Perencanaan Awal

- Tanggal: audit awal
- Persetujuan pemilik: audit read-only dan pembuatan dokumen perencanaan disetujui
- Fase/item: pemeriksaan awal dan pembuatan roadmap
- Perubahan: menambahkan dokumen `ROADMAP.md`; tidak ada perubahan kode atau database
- File yang berubah: `ROADMAP.md`
- Pengujian: pemeriksaan struktur proyek dan SQLite secara read-only
- Hasil: integritas database `ok`; roadmap awal tersedia
- Catatan/risiko tersisa: semua implementasi teknis masih menunggu persetujuan terpisah

### Entri 2 — Fase 0: Backup, Git, Build, Unit Test, dan Lint

- Tanggal: 4 September 2026
- Persetujuan pemilik: Fase 0 disetujui
- Fase/item: persiapan dan baseline
- Perubahan: memperbarui `.gitignore`, membuat backup lokal, menginisialisasi repositori Git lokal, dan menghasilkan artefak build/test/lint; tidak ada perubahan fungsi aplikasi atau database
- File source yang berubah: `.gitignore` dan `ROADMAP.md`
- Backup lokal: `.backups/doahindu-baseline-20260904-105714.zip`
- SHA-256 backup: `0A4F1296BF42D6C73DB9DFFF266FB5E25D2AA6C8DF6F61D324C61D9B33CB13D9`
- Build debug: berhasil; APK berada di `app/build/outputs/apk/debug/app-debug.apk`
- SHA-256 APK debug: `BBFD24382DF4A2ADE5F16DC164552F81377838FAB11013C940F49E886573C0B3`
- Unit test: berhasil, 1 test lulus; test yang tersedia masih berupa test template
- Lint: berhasil tanpa error, dengan 127 warning
- Warning lint utama: `HardcodedText` (18), `RtlHardcoded` (16), `ContentDescription` (10), `DefaultLocale` (10), `Overdraw` (9), `GradleDependency` (9), `SetTextI18n` (8), `NonConstantResourceId` (8), `DiscouragedApi` (4), `LockedOrientationActivity` (4), `IconLocation` (4), dan `UnusedResources` (4)
- Warning build: konfigurasi global `android.defaults.buildfeatures.buildconfig=true` deprecated dan fitur Gradle deprecated perlu ditinjau sebelum Gradle 9
- Temuan aset: `app/src/main/res/font/montserrat_regular.ttf` berukuran 0 byte, sedangkan salinan di assets berukuran 245.708 byte
- Konfigurasi baseline: JDK 17.0.20.1, Gradle 8.13, Android Gradle Plugin 8.13.2, compile/target SDK 35
- Kendala lingkungan: Gradle memerlukan cache lokal proyek dan akses Android SDK; artefak build lama yang berbeda kepemilikan telah dibersihkan
- Pengujian perangkat: APK berhasil dipasang dan proses aplikasi berhasil diluncurkan tanpa crash fatal pada emulator Android 15/API 35; otomasi alur utama tertahan pada dialog izin notifikasi sehingga pengujian fungsional belum dinyatakan selesai
- Temuan emulator: image emulator menggunakan Google Play services yang lebih lama daripada versi yang diminta dependency aplikasi
- Screenshot baseline: screenshot diagnostik tersimpan lokal di `.backups/doahindu-baseline-home.png`, tetapi belum menjadi baseline UI final karena masih menampilkan dialog permission
- Hasil: baseline build dapat dikompilasi; pekerjaan Fase 0 yang memerlukan perangkat masih terbuka
- Risiko tersisa: test otomatis belum mencakup logika aplikasi dan 127 warning lint belum ditangani

## Catatan Rilis

Belum ada perubahan aplikasi yang siap dirilis. Bagian ini akan diisi setelah implementasi dan verifikasi dimulai.
