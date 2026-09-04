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
- [x] Menjalankan aplikasi pada perangkat uji Android 10/API 29.
- [ ] Menjalankan aplikasi pada Android versi terbaru.
- [x] Mencatat screenshot beranda dan alur utama sebagai baseline awal.
- [x] Menguji alur utama secara manual: kategori, pencarian, baca doa, favorit, riwayat, berbagi, dan menu aplikasi.
- [x] Mencatat temuan baseline yang dapat diperiksa dari build dan lint.

Kriteria selesai:

- [x] Source dan data awal memiliki backup lokal yang dapat dipulihkan.
- [x] Build awal berhasil dan perilaku utama telah dicatat.
- [x] Daftar temuan baseline tersedia.

## Fase 1 — Pembaruan SDK dan Build System

Tujuan: membuat aplikasi kompatibel dengan SDK/API terbaru dan persyaratan Google Play.

- [x] Mendapatkan persetujuan untuk memulai Fase 1.
- [x] Memastikan versi API terbaru dan tenggat Google Play saat implementasi dimulai.
- [x] Memperbarui `compileSdk` ke API yang disepakati (API 36).
- [x] Memperbarui `targetSdk` ke API yang disepakati (API 36).
- [x] Meninjau dan mempertahankan `minSdk 23`.
- [x] Memastikan kompatibilitas Android Gradle Plugin, Gradle, dan JDK.
- [x] Memperbarui dependency AndroidX, Material, Firebase, Google Mobile Ads, dan test.
- [ ] Menghapus atau mengganti dependency lama yang sudah tidak diperlukan.
- [x] Menguji build debug dan release dengan R8/minification.
- [ ] Menangani warning dan error yang relevan dari lint/build.
- [ ] Menguji perubahan perilaku Android 16.

Kriteria selesai:

- [x] Debug dan release build berhasil.
- [ ] Aplikasi berjalan pada versi Android minimum dan terbaru yang disepakati.
- [x] Tidak ada crash pada alur utama setelah pembaruan API 36 berdasarkan pengujian manual perangkat fisik.
- [x] Target API memenuhi persyaratan Google Play yang berlaku saat pemeriksaan Fase 1.

## Fase 2 — UI, Edge-to-Edge, dan Aksesibilitas

Tujuan: memastikan tampilan nyaman, adaptif, dan dapat diakses.

- [x] Mendapatkan persetujuan untuk memulai Fase 2.
- [x] Menerapkan edge-to-edge dan penanganan `WindowInsets`.
- [ ] Menguji status bar, navigation bar, dan display cutout.
- [x] Menghapus pemaksaan `fontScale = 1`.
- [x] Menambahkan dukungan ukuran teks pengguna.
- [ ] Menambahkan pengaturan ukuran teks khusus isi doa jika disetujui.
- [x] Memeriksa kontras warna untuk mode terang dan gelap pada perangkat fisik.
- [x] Menambahkan `contentDescription` pada elemen interaktif yang memerlukannya.
- [x] Memastikan ukuran target sentuh memadai.
- [x] Membuat baseline layout adaptif untuk ponsel dan layar `sw600dp`; verifikasi tablet/foldable masih terbuka.
- [x] Meninjau kembali dan menghapus penguncian orientasi portrait.
- [x] Memastikan teks tidak terpotong saat font diperbesar pada perangkat fisik.
- [ ] Menguji dengan TalkBack.

Kriteria selesai:

- [x] Tampilan ponsel API 29 tidak tertutup system bar berdasarkan pemeriksaan visual.
- [x] Aplikasi dapat digunakan dengan ukuran font besar pada perangkat fisik.
- [ ] Alur utama dapat digunakan dengan TalkBack.
- [ ] Layout utama berfungsi pada ponsel dan layar besar.

## Fase 3 — Modernisasi Arsitektur Data Lokal

Tujuan: memisahkan konten aplikasi dari data pribadi pengguna dan membuat migrasi aman.

- [x] Mendapatkan persetujuan untuk memulai Fase 3.
- [x] Menentukan tetap memakai SQLite dengan lapisan akses terpisah untuk meminimalkan risiko migrasi.
- [x] Menetapkan `topic_id` sebagai ID konten permanen yang tidak berubah antarversi.
- [x] Memisahkan database konten dari data pengguna.
- [x] Memindahkan favorit ke penyimpanan lokal pengguna yang terpisah.
- [x] Memindahkan riwayat baca ke penyimpanan lokal pengguna yang terpisah.
- [x] Menghindari penyimpanan objek konten lengkap baru di SharedPreferences; pembaca format lama dipertahankan untuk migrasi.
- [x] Menambahkan nomor versi skema database pengguna.
- [x] Menambahkan strategi migrasi satu kali dari struktur lama.
- [x] Menambahkan indeks untuk relasi kategori, pencarian, dan urutan.
- [x] Mengaktifkan serta menguji foreign key pada koneksi database konten.
- [x] Memindahkan operasi database dari main/UI thread.
- [x] Mengganti query pencarian concatenation dengan parameter query.
- [x] Mengubah navigasi detail agar hanya mengirim ID, bukan BLOB dan isi doa melalui Intent.
- [x] Menambahkan migration/integration test untuk lapisan akses database.

Kriteria selesai:

- [x] Favorit dan riwayat lama tetap tersedia setelah migrasi berdasarkan migration test.
- [x] Database konten dapat diganti tanpa menghapus data pengguna berdasarkan integration test.
- [x] Tidak ada operasi database berat pada UI thread.
- [x] Seluruh migration test lulus.

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

- [x] Mendapatkan persetujuan untuk memulai Fase 4.
- [x] Memilih GitHub Pages sebagai sumber resmi.
- [x] Menentukan SQLite sebagai source konten.
- [x] Menentukan struktur `manifest.json`.
- [x] Menambahkan `schemaVersion`, `contentVersion`, `publishedAt`, dan `minimumAppVersion`.
- [x] Menyediakan pola URL paket konten berversi melalui workflow; URL publik menunggu repositori GitHub.
- [x] Menghasilkan dan memverifikasi checksum SHA-256.
- [x] Menentukan ukuran unduhan maksimum 25 MiB dan manifest maksimum 64 KiB.
- [x] Mengunduh hanya melalui HTTPS.
- [x] Menyimpan unduhan ke file sementara.
- [x] Memvalidasi checksum, ukuran, integritas, versi skema, tabel/kolom, ID duplikat, dan relasi kategori SQLite sebelum digunakan.
- [x] Menerapkan penggantian data secara atomik dengan file cadangan.
- [x] Menyediakan rollback otomatis jika pembaruan gagal.
- [x] Menampilkan status pembaruan dengan bahasa yang mudah dipahami.
- [x] Mempertahankan database bawaan APK untuk penggunaan offline pertama.
- [x] Menambahkan pemeriksaan otomatis harian dan pemeriksaan manual melalui menu.
- [x] Menambahkan GitHub Actions untuk validasi dan publikasi paket konten ke GitHub Pages.
- [x] Menguji pembaruan normal melalui GitHub Pages serta gagal unduh/non-HTTPS, checksum salah, skema salah, dan rollback melalui instrumented test.

Kriteria selesai:

- [x] Konten dapat diperbarui tanpa update APK berdasarkan pengujian `contentVersion 1` ke `contentVersion 2` pada emulator.
- [x] Aplikasi tetap dapat dibuka tanpa internet.
- [x] Paket rusak atau tidak sah tidak pernah menggantikan database aktif berdasarkan instrumented test.
- [x] Favorit, riwayat, dan pengaturan pengguna tetap utuh berdasarkan pemisahan database dan instrumented test.
- [x] Versi database sebelumnya dapat dipulihkan jika pembaruan gagal berdasarkan instrumented test.

## Fase 5 — Perbaikan Konten

Tujuan: meningkatkan mutu, konsistensi, dan keterlacakan isi doa.

- [x] Mendapatkan persetujuan untuk memulai audit Fase 5.
- [x] Memeriksa duplikat `Sarascamuscaya 3`; ditemukan pada ID 20 dan 21 dengan isi placeholder identik.
- [ ] Memverifikasi ejaan `Sarascamuscaya` atau `Sarasamuccaya` dengan editor konten.
- [x] Memvalidasi seluruh BLOB gambar berukuran sangat kecil; 52 topik hanya menyimpan signature PNG 8 byte, bukan gambar lengkap.
- [x] Memeriksa doa yang sangat pendek; ID 18–21 berisi placeholder identik 48 karakter, bukan konten final.
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

- [x] Mendapatkan persetujuan untuk memulai Fase 7A.
- [x] Menaikkan database pengguna ke versi 2 dengan migrasi non-destruktif.
- [x] Memigrasikan favorit lama ke koleksi bawaan `Favorit`.
- [x] Menambahkan folder atau koleksi favorit.
- [x] Mendukung satu doa di beberapa koleksi.
- [x] Menambahkan pembuatan, penggantian nama, dan penghapusan koleksi non-bawaan.
- [x] Menambahkan catatan pribadi pada doa.
- [x] Memastikan koleksi dan catatan tetap tersedia setelah database konten diganti.
- [x] Menambahkan instrumented test upgrade database pengguna versi 1 ke versi 2.
- [x] Memverifikasi seluruh alur koleksi dan catatan secara manual pada perangkat.
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

- [x] Target SDK/API final untuk pembaruan ini: API 36.
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

### Entri 3 — Commit Baseline dan Smoke Test Perangkat Fisik

- Tanggal: 4 September 2026
- Persetujuan pemilik: identitas Git diberikan dan penggunaan perangkat fisik diizinkan
- Git lokal: `user.name=arton-soul`, `user.email=arton.79@gmail.com`
- Commit baseline: `c8bdf149ef7ab6f6dcdd9bd61cda1b13c0653524` (`chore: establish Doa Hindu baseline`)
- Perangkat: Xiaomi Redmi Note 8 Pro, Android 10/API 29
- Kondisi awal perangkat: paket `com.dearyoti.doahindu` belum terpasang sehingga tidak ada data aplikasi lama yang ditimpa
- Instalasi: APK debug berhasil dipasang
- Peluncuran: cold start berhasil dan proses aplikasi tetap berjalan tanpa crash fatal
- Tampilan: beranda berhasil tampil dengan banner, kategori, toolbar, dan navigasi bawah sistem
- Screenshot baseline valid: `.backups/doahindu-physical-home.png`
- UI hierarchy baseline: `.backups/doahindu-physical-home.xml`
- Kendala pengujian: MIUI menolak injeksi tap ADB karena izin `INJECT_EVENTS`; pengaturan keamanan perangkat tidak diubah
- Status: smoke test peluncuran dan beranda lulus; pengujian interaksi kategori, pencarian, detail doa, favorit, riwayat, dan berbagi masih terbuka

### Entri 4 — Verifikasi Manual dan Penyelesaian Fase 0

- Tanggal: 4 September 2026
- Penguji: pemilik aplikasi
- Perangkat uji: Xiaomi Redmi Note 8 Pro, Android 10/API 29
- Cakupan: beranda, kategori, daftar dan detail doa, pencarian, favorit, persistensi favorit, riwayat baca, berbagi, menu Tentang, Kebijakan Privasi, navigasi, tombol kembali, dan tampilan umum
- Hasil: seluruh fungsi yang diuji berjalan normal dan tidak ditemukan masalah
- Status Fase 0: selesai untuk baseline yang disepakati
- Catatan: pengujian khusus pada API minimum 23 dan Android/API terbaru tetap dapat ditambahkan pada matriks pengujian rilis di Fase 9

### Entri 5 — Fase 1: SDK/API 36 dan Dependency

- Tanggal: 4 September 2026
- Persetujuan pemilik: Fase 1 disetujui
- SDK: `compileSdk` dan `targetSdk` diperbarui dari 35 ke 36; `minSdk 23` dipertahankan
- Toolchain: Java source/target diperbarui dari 8 ke 17; AGP 8.13.2 dan Gradle 8.13 dipertahankan karena kompatibel dengan API 36
- Google Services plugin: 4.4.2 ke 4.5.0
- Firebase: dipindahkan ke Firebase BoM 34.18.0 agar Analytics dan Messaging menggunakan versi yang konsisten
- AndroidX/Material: AppCompat 1.8.0, Material 1.14.0, ConstraintLayout 2.2.2, dan RecyclerView 1.4.0
- Library lain: Gson 2.14.0; Google Mobile Ads tetap 25.4.0 karena sudah versi stabil terbaru saat pemeriksaan
- Test: AndroidX Test JUnit 1.3.0 dan Espresso 3.7.0
- Release optimization: default ProGuard diperbarui ke `proguard-android-optimize.txt`
- BuildConfig: konfigurasi deprecated dipindahkan dari `gradle.properties` ke module DSL
- Build debug: berhasil
- Unit test: 1 test lulus, tanpa failure/error
- Lint: berhasil tanpa error; warning turun dari 127 menjadi 116
- Build release R8/minification: berhasil; menghasilkan APK unsigned dan mapping R8
- SHA-256 APK debug: `E0988AFBA1F51B9A63654FB8809532E4F0A79A9316CE630DD1C14300575441E9`
- SHA-256 APK release unsigned: `A30E6C22972B9075ECFA69484DBFF00095D762E24E920FA94D8AFE8D36226CB9`
- Verifikasi manifest APK: `minSdk 23`, `targetSdk 36`, `compileSdk 36`
- Perangkat fisik: APK API 36 berhasil dipasang pada Redmi Note 8 Pro/Android 10 API 29; cold start berhasil tanpa crash atau ANR
- Peringatan tersisa: command-line tools Android lebih lama daripada format metadata SDK, beberapa API Java lama/deprecated masih digunakan, dan Gradle masih melaporkan fitur deprecated
- Pengujian tersisa: alur utama setelah pembaruan perlu diverifikasi manual; perubahan perilaku khusus Android 16 perlu diuji pada emulator/perangkat API 36
- Referensi: Android 16 SDK `https://developer.android.com/about/versions/16/setup-sdk`, persyaratan target API `https://developer.android.com/google/play/requirements/target-sdk`, Firebase release notes `https://firebase.google.com/support/release-notes/android`, dan Mobile Ads release notes `https://developers.google.com/admob/android/rel-notes`

### Entri 6 — Verifikasi Manual Fase 1

- Tanggal: 4 September 2026
- Penguji: pemilik aplikasi
- Perangkat: Xiaomi Redmi Note 8 Pro, Android 10/API 29
- Build: debug dengan `compileSdk 36` dan `targetSdk 36`
- Cakupan: seluruh alur utama aplikasi setelah pembaruan SDK dan dependency
- Hasil: seluruh fungsi yang diuji berjalan normal dan tidak ditemukan masalah
- Status: verifikasi fungsional Fase 1 pada API 29 selesai
- Catatan: system image/emulator API 36 belum tersedia di lingkungan lokal, sehingga item pengujian perilaku khusus Android 16 tetap terbuka dan tidak dicentang

### Entri 7 — Implementasi Awal Fase 2

- Tanggal: 4 September 2026
- Persetujuan pemilik: Fase 2 disetujui
- Edge-to-edge: menambahkan penanganan system bar dan display cutout melalui `WindowInsets` pada Main, Splash, Topic, dan Stories
- Orientasi: penguncian portrait dihapus dari empat activity utama
- Ukuran teks: pemaksaan `fontScale = 1` dihapus agar aplikasi mengikuti pengaturan sistem
- Aksesibilitas: menambahkan label aksesibilitas pada elemen interaktif/dekoratif yang relevan, label dinamis untuk favorit, dan target sentuh minimum 48dp
- RTL dan resource: teks antarmuka dipindahkan ke string resource dan atribut kiri/kanan yang relevan diganti menjadi start/end
- Layout adaptif: grid ponsel menggunakan 2 kolom, sedangkan resource `sw600dp` menggunakan 4 kolom dan ukuran banner khusus layar besar
- Mode gelap: palette warna malam dasar ditambahkan; pemeriksaan kontras visual manual masih terbuka
- Perbaikan layout: menghilangkan ScrollView berlapis pada halaman Tentang dan Kebijakan Privasi serta memperbaiki overlap header beranda
- Build debug: berhasil
- Unit test: 1 test lulus, tanpa failure/error
- Lint: berhasil tanpa error; warning turun dari 116 menjadi 54
- Sasaran lint yang kini nol: `ContentDescription`, `LockedOrientationActivity`, `DiscouragedApi`, `RtlHardcoded`, `HardcodedText`, `TouchTargetSize`, `RelativeOverlap`, `ObsoleteLayoutParam`, dan `ScrollViewSize`
- Perangkat fisik: APK dipasang dan dibuka pada Redmi Note 8 Pro/Android 10 API 29 dengan skala font sistem 1,17; tidak ditemukan crash/ANR aplikasi
- Pemeriksaan visual: toolbar, header, tombol, grid dua kolom, status bar, dan navigation bar tampil baik pada perangkat fisik
- Screenshot lokal: `.backups/phase2-physical-final.png`
- Pengujian tersisa: rotasi, ukuran font besar, mode gelap, TalkBack, display cutout, multi-window, serta tablet/foldable
- Status: implementasi awal selesai; Fase 2 belum ditutup sampai pengujian manual tersisa dilakukan

### Entri 8 — Verifikasi Manual Fase 2 pada Ponsel

- Tanggal: 4 September 2026
- Penguji: pemilik aplikasi
- Perangkat: Xiaomi Redmi Note 8 Pro, Android 10/API 29
- Cakupan yang diminta: rotasi portrait/landscape, ukuran font sistem besar, mode gelap, alur utama, serta pemeriksaan system bar dan layout
- Hasil: seluruh bagian yang dicoba berjalan normal dan tidak ditemukan masalah
- Status: implementasi dan verifikasi Fase 2 pada ponsel selesai
- Pengujian tambahan yang tetap terbuka: TalkBack, display cutout, multi-window, serta tablet/foldable
- Catatan: pengaturan ukuran teks khusus isi doa belum dibuat karena merupakan fitur opsional yang memerlukan persetujuan tersendiri

### Entri 9 — Fase 3: Pemisahan Database Konten dan Data Pengguna

- Tanggal: 4 September 2026
- Persetujuan pemilik: Fase 3 disetujui
- Keputusan arsitektur: tetap menggunakan SQLite agar migrasi aplikasi lama lebih kecil dan database konten mudah diganti pada Fase 4
- Database konten: `doahindu1.sqlite` dipertahankan sebagai sumber doa offline
- Database pengguna: menambahkan `doahindu_user.sqlite` versi 1 dengan tabel `user_favorite` dan `user_recent`
- Migrasi: saat startup pertama, favorit dan riwayat dibaca dari kolom database konten serta SharedPreferences lama, kemudian disalin satu kali ke database pengguna
- Kompatibilitas: pembaca SharedPreferences lama dipertahankan hanya untuk migrasi; aplikasi tidak lagi menulis objek doa lengkap ke SharedPreferences
- ID permanen: `topic_id` menjadi kontrak penghubung antara konten, favorit, dan riwayat
- Indeks: menambahkan indeks kategori topik, nama topik, latest story, dan urutan riwayat
- Query: pencarian dan lookup ID memakai selection arguments
- Navigasi detail: Intent hanya membawa `topic_id` dan flag sumber; judul, isi, kategori, dan gambar dibaca kembali dari database
- Perbaikan tambahan: status favorit pada adapter kini memakai ID item yang sedang di-bind, bukan field ID bersama
- Build debug: berhasil
- Unit test: 1 test template lulus, tanpa failure/error
- Lint: berhasil dengan 0 error dan 54 warning, sama dengan hasil akhir Fase 2
- Uji perangkat fisik: upgrade APK ditolak Android karena tanda tangan APK terpasang berbeda; aplikasi/data lama tidak dihapus
- Uji emulator: setelah mendapat izin pemilik, paket lama dihapus hanya dari emulator API 35 dan APK baru berhasil dipasang secara bersih
- Verifikasi database emulator: `doahindu1.sqlite` dan `doahindu_user.sqlite` terbentuk sebagai file terpisah; penulisan favorit/riwayat memperbesar WAL database pengguna tanpa mengubah file konten
- Verifikasi alur emulator: beranda, kategori, dan detail “Kata Pengantar” berhasil dibuka melalui navigasi berbasis ID; favorit berhasil ditambahkan dan label berubah menjadi “Hapus dari favorit”
- Stabilitas emulator: proses aplikasi tetap hidup dan tidak ditemukan crash, ANR aplikasi, atau exception SQLite pada log
- Pengujian tersisa: migrasi dengan signing key yang sama, pengujian repository/migrasi otomatis, dan pemindahan operasi database dari UI thread
- Status: fondasi pemisahan data selesai; Fase 3 masih berjalan

### Entri 10 — Penyelesaian Teknis Fase 3

- Tanggal: 4 September 2026
- Persetujuan pemilik: pekerjaan Fase 3 dilanjutkan
- Threading: menambahkan `DatabaseExecutor` dengan worker pool dan callback main thread yang kompatibel mulai API 23
- Cakupan asynchronous: startup/copy database, beranda, kategori, daftar doa, pencarian, detail, favorit, dan riwayat tidak lagi menjalankan operasi database pada UI thread
- Keamanan concurrent: koneksi database tidak lagi ditutup oleh setiap query; `DatabaseHelper.close()` menutup database konten dan pengguna bersama-sama
- Test runner: konfigurasi `AndroidJUnitRunner` ditambahkan
- Migration/integration test: membuat state favorit/riwayat format lama, menjalankan migrasi, memverifikasi foreign key aktif, menguji query pencarian berparameter, mengganti database konten, lalu memastikan data pengguna tetap tersedia
- Temuan test: percobaan awal menemukan koneksi WAL pengguna belum ikut ditutup; kontrak `close()` diperbaiki sebelum test dinyatakan lulus
- Hasil connected test: 1 test lulus pada emulator Android 15/API 35, 0 failure dan 0 error
- Build debug dan unit test: berhasil
- Lint: berhasil dengan 0 error dan 50 warning
- Catatan lingkungan: emulator sempat tidak responsif dan harus dihidupkan ulang; test final setelah restart lulus
- Status: seluruh pekerjaan teknis dan kriteria otomatis Fase 3 selesai
- Verifikasi rilis yang tetap diperlukan: upgrade pada aplikasi produksi harus menggunakan signing key yang sama agar migrasi data pengguna nyata dapat diuji tanpa uninstall

### Entri 11 — Fondasi Sistem Pembaruan Konten dari GitHub

- Tanggal: 4 September 2026
- Persetujuan pemilik: Fase 4 disetujui
- Arsitektur: GitHub Pages menyajikan `manifest.json` dan database SQLite berversi; database bawaan APK tetap menjadi fallback offline
- Aplikasi: menambahkan pemeriksaan otomatis harian dan pemeriksaan manual dari menu navigasi
- Keamanan unduhan: hanya HTTPS, redirect diperiksa, timeout jaringan diterapkan, manifest dibatasi 64 KiB, dan database dibatasi 25 MiB
- Validasi paket: ukuran dan SHA-256 harus cocok; SQLite harus lulus `integrity_check`, `user_version`, kontrak tabel/kolom, pemeriksaan ID duplikat, dan relasi kategori
- Aktivasi: database baru diunduh ke file sementara, database aktif dipindah ke backup, dan rollback dijalankan jika aktivasi gagal
- Data pengguna: favorit dan riwayat tetap berada di `doahindu_user.sqlite`, sehingga penggantian database konten tidak menimpanya
- Otomasi: menambahkan workflow GitHub Actions untuk memvalidasi source SQLite, membuat artefak berversi dan manifest, lalu menerbitkannya ke GitHub Pages
- Dokumentasi: menambahkan petunjuk pembaruan konten dan konfigurasi endpoint pada `content/README.md`
- Build debug, unit test, dan lint: berhasil; lint menghasilkan 0 error dan 52 warning
- Instrumented test: 3 test lulus pada emulator Android 15/API 35, termasuk migrasi Fase 3, validasi database/checksum/schema, aktivasi, backup/rollback, preservasi favorit, endpoint kosong, dan penolakan HTTP
- Status: fondasi lokal selesai dan tervalidasi; endpoint produksi masih kosong sehingga aplikasi aman menampilkan status belum dikonfigurasi
- Pekerjaan tersisa: menyediakan repositori GitHub dan URL Pages, mengisi `CONTENT_MANIFEST_URL`, menjalankan workflow pertama, lalu menguji unduhan nyata dan pembaruan tanpa APK

### Entri 12 — Konfigurasi Repositori Produksi Fase 4

- Tanggal: 4 September 2026
- Persetujuan pemilik: menghubungkan dan mengunggah proyek ke GitHub disetujui
- Repositori: `https://github.com/arton-soul/doahindu`
- Branch produksi: `main`
- Endpoint manifest: `https://arton-soul.github.io/doahindu/manifest.json`
- Perubahan: mengisi `CONTENT_MANIFEST_URL` dan menyesuaikan dokumentasi publikasi
- Upload: seluruh riwayat proyek berhasil diunggah ke branch `main`; remote lokal `origin` melacak `origin/main`
- Verifikasi GitHub: repositori publik dengan default branch `main` dan workflow publikasi sudah dikenali GitHub
- GitHub Pages: aktif; workflow publikasi pertama selesai sukses dan endpoint manifest dapat diakses

### Entri 13 — Uji Pembaruan Konten Nyata melalui GitHub Pages

- Tanggal: 4 September 2026
- Persetujuan pemilik: pengujian Fase 4 dilanjutkan
- Workflow: run #2 selesai sukses dan menerbitkan `contentVersion 2`
- Manifest publik: HTTP 200, `schemaVersion 1`, `contentVersion 2`, dan `minimumAppVersion 12`
- Paket publik: `content-v2.sqlite`; ukuran file dan SHA-256 hasil unduhan cocok dengan manifest
- Perangkat uji: emulator Android 15/API 35; perangkat fisik tidak disentuh
- Hasil aplikasi: pembaruan otomatis melalui internet berhasil memasang versi konten 2 tanpa update APK
- Bukti aktivasi: preferensi `content_version=2`, database konten aktif tersedia, dan file `.backup` versi sebelumnya terbentuk
- Data pengguna: `doahindu_user.sqlite` tetap terpisah dan tersedia setelah pembaruan
- Stabilitas: proses aplikasi tetap berjalan dan tidak ditemukan fatal exception atau crash updater
- Catatan teknis: log SQLite mencatat `file renamed while open` ketika aktivasi; relaunch berhasil, tetapi koneksi database perlu ditutup secara eksplisit sebelum penggantian pada perbaikan berikutnya
- Status Fase 4: seluruh fungsi dan kriteria utama selesai; perbaikan warning koneksi terbuka direkomendasikan sebelum rilis produksi

### Entri 14 — Perbaikan Koneksi Terbuka saat Aktivasi Konten

- Tanggal: 4 September 2026
- Persetujuan pemilik: perbaikan warning aktivasi Fase 4 dilanjutkan
- Akar masalah: `MainActivity` hanya menutup helper miliknya setelah replacement, sedangkan helper milik fragment/adapter masih dapat menahan koneksi database konten
- Perbaikan: seluruh instance `DatabaseHelper` diregistrasikan secara lemah dan ditutup sebelum file database aktif diganti
- Sidecar SQLite: file journal, WAL, dan SHM milik database konten aktif/cadangan dibersihkan setelah koneksi ditutup dan sebelum replacement
- Test tambahan: instrumented test membuka koneksi konten, menjalankan replacement, lalu memastikan koneksi lama sudah tertutup
- Build debug dan unit test: berhasil
- Lint: berhasil dengan 0 error dan 52 warning
- Instrumented test: 3 test lulus pada emulator Android 15/API 35
- Uji regresi internet: `contentVersion 2` kembali berhasil terpasang, file backup terbentuk, dan database pengguna tetap tersedia
- Log regresi: tidak ditemukan lagi `file renamed while open`, `SQLiteException`, atau fatal exception
- Status: warning koneksi terbuka Fase 4 selesai diperbaiki dan tervalidasi

### Entri 15 — Fase 7A: Koleksi Favorit dan Catatan Pribadi

- Tanggal: 4 September 2026
- Persetujuan pemilik: lompat ke Fase 7 hanya untuk koleksi favorit dan catatan pribadi disetujui
- Database pengguna: versi dinaikkan dari 1 ke 2 dengan tabel koleksi, relasi koleksi-doa, dan catatan
- Migrasi: seluruh `user_favorite` lama disalin ke koleksi bawaan `Favorit` tanpa menghapus tabel/data lama
- Koleksi: pengguna dapat membuat koleksi, mengganti nama, menghapus koleksi non-bawaan, dan memasukkan satu doa ke beberapa koleksi
- Layar favorit: menambahkan pemilih koleksi, tombol koleksi baru, dan menu pengelolaan; penghapusan item hanya berlaku pada koleksi aktif
- Halaman doa: ikon favorit membuka pemilih multi-koleksi dan menyediakan pembuatan koleksi baru
- Catatan pribadi: menu pada halaman doa membuka editor; teks kosong menghapus catatan
- Keamanan data: koleksi dan catatan berada di `doahindu_user.sqlite` sehingga tidak ikut diganti oleh pembaruan konten GitHub
- Build debug dan unit test: berhasil
- Lint: berhasil dengan 0 error dan 55 warning
- Instrumented test: 4 test lulus pada emulator Android 15/API 35, termasuk upgrade user database v1→v2, multi-koleksi, catatan, penghapusan koleksi, dan replacement database konten
- Pengujian tersisa: verifikasi UI manual pada perangkat oleh pemilik aplikasi

### Entri 16 — Build dan Smoke Test Fase 7A pada Perangkat Fisik

- Tanggal: 4 September 2026
- Persetujuan pemilik: build dan instalasi pada perangkat diminta
- Perangkat: Xiaomi Redmi Note 8 Pro, Android 10/API 29
- Instalasi: APK debug terbaru berhasil dipasang sebagai upgrade (`-r`); aplikasi lama tidak di-uninstall dan data dipertahankan
- Versi terpasang: `3.4.1` (`versionCode 12`, target API 36)
- Peluncuran: aplikasi berhasil dibuka dan proses tetap aktif
- Log: tidak ditemukan fatal exception, ANR aplikasi, atau `SQLiteException`
- Status: smoke test instalasi/peluncuran lulus; alur koleksi favorit dan catatan kemudian dinyatakan normal oleh pemilik

### Entri 17 — Perbaikan Warna Teks Input Fase 7A

- Tanggal: 4 September 2026
- Persetujuan pemilik: perbaikan warna teks disetujui
- Akar masalah: tema memaksa `android:editTextColor` menjadi putih sehingga teks tidak terlihat pada dialog berlatar terang
- Perbaikan: warna input menggunakan `text_color` dan warna hint menggunakan `light_text_color`; keduanya memiliki varian mode terang dan gelap
- Build debug dan lint: berhasil; lint 0 error dan 55 warning
- Perangkat: APK berhasil dipasang sebagai upgrade pada Redmi Note 8 Pro tanpa menghapus data
- Smoke test: aplikasi berhasil dibuka, proses aktif, dan tidak ditemukan crash, ANR, atau `SQLiteException`
- Verifikasi pemilik: teks nama koleksi dan catatan pribadi sudah terbaca; hasil pengujian dinyatakan baik
- Status Fase 7A: implementasi, pengujian otomatis, dan verifikasi manual perangkat selesai

### Entri 18 — Audit Read-only Konten Fase 5

- Tanggal: 4 September 2026
- Persetujuan pemilik: audit Fase 5 disetujui; tidak ada izin perubahan materi keagamaan
- Database: `app/src/main/assets/doahindu1.sqlite`, dibuka read-only; SHA-256 source `298A075F16BA8D5A6E747EEE6A2FAEB799E1B8EF4D425C537D19B89E9F091376`
- Integritas/skema: `integrity_check=ok`, `user_version=1`, 7 kategori, 59 topik, dan 3 cerita terbaru; tidak ada judul/isi kosong atau relasi kategori yatim
- Duplikat: `Sarascamuscaya 3` muncul pada `topic_id` 20 dan 21
- Placeholder: ID 18–21 (`Sarascamuscaya 1`, `2`, `3`, `3`) memiliki isi identik 48 karakter: `Masih dalam pengerjaan, tunggu update berikutnya`
- Ejaan: database konsisten memakai `Sarascamuscaya`; keputusan perubahan menjadi `Sarasamuccaya` memerlukan editor/pemilik materi
- Gambar topik: 52 dari 59 BLOB hanya 8 byte berupa signature PNG `89504E470D0A1A0A` dan bukan file gambar lengkap
- Gambar lengkap: 7 topik memiliki PNG lengkap, yaitu ID 2, 5, 9, 15, 18, 23, dan 65; masing-masing mewakili gambar kategori terkait
- Konsistensi nama: ID 67 memakai spasi ganda pada `Pupuh  Sinom`
- Cerita terbaru: tiga entri merupakan tabel/konten mandiri; ID-nya tidak harus merujuk `tbl_topics`, sehingga `Kutukan Ekalaya` tidak langsung dinyatakan rusak
- Perubahan konten/database: tidak ada
- Keputusan tersisa: tindakan pada duplikat ID 21, ejaan judul/kategori, placeholder ID 18–21, strategi gambar kosong, dan spasi ganda menunggu persetujuan editorial

## Catatan Rilis

Belum ada perubahan aplikasi yang siap dirilis. Bagian ini akan diisi setelah implementasi dan verifikasi dimulai.
