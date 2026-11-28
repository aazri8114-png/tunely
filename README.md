# Tunely v1.0.0 — pemutar musik ciptaan Azri

TikTok: @realajriinii · Telegram: @nobledevelopment

## Fitur
- Tampilan ala iOS, tab bar bawah **liquid glass** (Beranda, Cari, Koleksi, Tentang).
- Beranda otomatis: Lagi populer, Pop, Hip-Hop/Rap, Electronic, Lo-Fi.
- Cari lagu + kategori cepat. Mini player + layar Sedang diputar (artwork, geser posisi).
- Acak, ulangi (semua / satu lagu), antrean, favorit, playlist sendiri, riwayat, bagikan, sleep timer.
- **Musik tetap jalan** saat aplikasi ditutup atau layar mati (foreground service + notifikasi dengan tombol sebelumnya, jeda, berikutnya).
- Tombol kembali tidak menghentikan musik, cuma menyembunyikan aplikasi.
- Halaman **Tentang**: nama pembuat (bisa diganti), versi Tunely, TikTok, Telegram.

## Sumber musik
Audius (API publik tanpa key). YouTube Music tidak dipakai karena tidak punya API resmi dan
tidak bisa diputar di latar belakang secara native tanpa library tambahan.

## Pasang lewat Termux + GitHub
```bash
pkg install unzip git -y
cd ~ && unzip -o /sdcard/Download/Tunely.zip && cd Tunely
git init && git add . && git commit -m "Tunely v1.0.0"
git branch -M main
git remote add origin https://github.com/USERNAME/Tunely.git
git push -u https://USERNAME:TOKEN@github.com/USERNAME/Tunely.git main
```
Build jalan otomatis di tab **Actions** (sekitar 3-5 menit). Unduh APK di bagian **Artifacts → Tunely-APK**
(unzip dulu, lalu pasang app-debug.apk). Update berikutnya: ulangi `unzip`, `git add .`, `git commit`, `git push`.

## Setelah dipasang
Izinkan Notifikasi saat diminta (wajib di Android 13+ supaya kontrol musik muncul).
