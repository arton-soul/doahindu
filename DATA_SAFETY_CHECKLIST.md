# Checklist Data Safety Google Play — Doa Hindu

Dokumen ini disusun berdasarkan kode aplikasi dan dokumentasi SDK pada
5 September 2026. Periksa kembali tampilan serta pertanyaan terbaru di Play
Console sebelum menekan **Kirim/Submit**. Pengembang tetap bertanggung jawab
atas deklarasi final.

## Informasi dasar

- Nama aplikasi: **Doa Hindu**
- Package: `com.dearyoti.doahindu`
- Kebijakan privasi:
  `https://dearyoti.blogspot.com/p/kebijakan-privasi-doa-hindu-terakhir.html`
- Aplikasi berisi iklan: **Ya**
- Aplikasi menyediakan pembuatan akun: **Tidak**
- Data pengguna dikumpulkan atau dibagikan oleh aplikasi/SDK: **Ya**
- Semua data yang dikirim melalui jaringan dienkripsi saat transit: **Ya**
- Mekanisme permintaan penghapusan data server milik pengembang: **Tidak ada**;
  aplikasi tidak memiliki akun atau server data pengguna. Data lokal dapat
  dihapus dengan menghapus data aplikasi atau uninstall.

## Data yang disarankan untuk dideklarasikan

### Lokasi — Perkiraan lokasi

- Dikumpulkan: **Ya**
- Dibagikan: **Ya**
- Pemrosesan sementara/ephemeral saja: **Tidak**
- Wajib atau opsional: **Wajib** untuk penggunaan aplikasi yang berisi iklan
- Tujuan: **Iklan atau pemasaran**, **Analitik**, serta
  **Pencegahan penipuan, keamanan, dan kepatuhan**
- Dasar: Google Mobile Ads memproses alamat IP yang dapat digunakan untuk
  memperkirakan lokasi umum; Firebase Analytics juga memperoleh lokasi kasar
  dari IP yang disamarkan.

### Aktivitas aplikasi — Interaksi aplikasi

- Dikumpulkan: **Ya**
- Dibagikan: **Ya**
- Pemrosesan sementara/ephemeral saja: **Tidak**
- Wajib atau opsional: **Wajib**
- Tujuan: **Iklan atau pemasaran**, **Analitik**, serta
  **Pencegahan penipuan, keamanan, dan kepatuhan**
- Contoh: peluncuran aplikasi, tampilan layar/sesi, ketukan, tayangan video
  iklan, dan interaksi notifikasi.

### Info dan performa aplikasi — Diagnostik

- Dikumpulkan: **Ya**
- Dibagikan: **Ya**
- Pemrosesan sementara/ephemeral saja: **Tidak**
- Wajib atau opsional: **Wajib**
- Tujuan: **Analitik**, **Iklan atau pemasaran**, serta
  **Pencegahan penipuan, keamanan, dan kepatuhan**
- Contoh: waktu peluncuran, hang rate, penggunaan energi, versi aplikasi, dan
  informasi teknis SDK.

### ID perangkat atau ID lainnya

- Dikumpulkan: **Ya**
- Dibagikan: **Ya**
- Pemrosesan sementara/ephemeral saja: **Tidak**
- Wajib atau opsional: **Wajib**
- Tujuan: **Fungsi aplikasi**, **Iklan atau pemasaran**, **Analitik**, serta
  **Pencegahan penipuan, keamanan, dan kepatuhan**
- Contoh: advertising ID, app set ID, app-instance ID, dan Firebase
  installation ID. FCM menggunakan identitas instalasi untuk pengiriman
  notifikasi.

## Data yang tidak perlu dideklarasikan sebagai dikumpulkan

Data berikut hanya tersimpan dan diproses lokal, tidak dikirim ke server
Dearyoti, sehingga tidak termasuk data yang dikumpulkan menurut definisi Data
Safety saat ini:

- koleksi favorit;
- catatan pribadi;
- riwayat dan posisi baca;
- pengaturan Mode Baca;
- database konten yang tersimpan offline.

Cloud backup aplikasi dinonaktifkan. Menghapus data aplikasi atau uninstall
akan menghapus data lokal tersebut.

## Pemeriksaan konfigurasi Play Console

- [ ] Masukkan URL kebijakan privasi publik.
- [ ] Nyatakan bahwa aplikasi berisi iklan.
- [ ] Pilih **Ya** untuk pengumpulan atau pembagian data.
- [ ] Nyatakan data dienkripsi saat transit.
- [ ] Nyatakan aplikasi tidak menyediakan akun pengguna.
- [ ] Deklarasikan empat kelompok data di atas.
- [ ] Pastikan tujuan dan status collected/shared cocok dengan formulir.
- [ ] Tinjau preview Data Safety agar konsisten dengan kebijakan privasi.
- [ ] Simpan screenshot atau ekspor CSV deklarasi final untuk arsip rilis.
- [ ] Kirim formulir dan tunggu status diterima Play Console.

## Sumber resmi

- Google Play Data Safety:
  `https://support.google.com/googleplay/android-developer/answer/10787469`
- Google Mobile Ads data disclosure:
  `https://developers.google.com/admob/android/privacy/play-data-disclosure`
- Firebase Android data disclosure:
  `https://firebase.google.com/docs/android/play-data-disclosure`
- Google Analytics for Firebase data disclosure:
  `https://support.google.com/analytics/answer/11582702`

