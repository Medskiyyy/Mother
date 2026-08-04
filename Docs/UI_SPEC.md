# UI_SPEC.md (Part 1)

# Mother UI Specification

Dokumen ini menjelaskan struktur setiap halaman aplikasi.

Dokumen ini tidak menentukan ukuran, warna, maupun detail visual.

Fokus dokumen ini adalah fungsi setiap halaman.

---

# Navigation

Bottom Navigation terdiri dari lima menu.

* Dashboard
* Calendar
* Tasks
* Progress
* Settings

Floating Action Button digunakan untuk membuat data baru.

FAB menampilkan pilihan.

* Schedule
* Task
* Habit

---

# Dashboard

## Tujuan

Memberikan ringkasan kondisi hari ini.

Pengguna harus mengetahui apa yang harus dilakukan dalam beberapa detik.

---

## Komponen

Greeting

Tanggal

Streak

Progress Target Hari Ini

Aktivitas Berikutnya

Deadline Terdekat

Jadwal Hari Ini

Quick Action

---

## Greeting

Berubah otomatis.

Contoh.

Selamat Pagi

Selamat Siang

Selamat Malam

---

## Progress Target

Menampilkan.

Progress Bar

Durasi Saat Ini

Target Hari Ini

Contoh.

1j 20m / 2j

---

## Aktivitas Berikutnya

Menampilkan.

Nama

Jam

Countdown

Tombol Mulai

Jika tidak ada aktivitas.

Tampilkan Empty State.

---

## Deadline

Menampilkan maksimal tiga deadline.

Urut berdasarkan waktu terdekat.

Menekan item membuka Detail Task.

---

## Jadwal Hari Ini

Menampilkan seluruh aktivitas.

Setiap item menampilkan.

Icon

Warna

Judul

Jam

Status

---

## Quick Action

Shortcut.

Tambah Task

Tambah Schedule

Start Timer

---

## Interaksi

Menekan Activity.

Membuka Detail Schedule.

Menekan Task.

Membuka Detail Task.

Menekan Progress.

Membuka halaman Progress.

---

## Empty State

Belum ada aktivitas hari ini.

Belum ada deadline.

Belum ada target belajar.

---

# Calendar

## Tujuan

Melihat seluruh aktivitas berdasarkan tanggal.

---

## Komponen

Header

Filter

Calendar

Daftar Aktivitas

---

## Header

Menampilkan.

Bulan

Tahun

Tombol Hari Ini

---

## Filter

Pilihan.

Semua

Schedule

Task

Habit

Study Session

---

## Calendar

Mendukung.

Month

Week

Day

---

## Daftar Aktivitas

Menampilkan aktivitas sesuai tanggal yang dipilih.

Urutan.

Jam paling awal.

---

## Interaksi

Swipe berpindah bulan.

Klik tanggal.

Menampilkan aktivitas.

Klik aktivitas.

Membuka Detail.

---

## Empty State

Tidak ada aktivitas.

---

# Tasks

## Tujuan

Mengelola seluruh pekerjaan.

---

## Komponen

Search

Filter

Sorting

Daftar Task

FAB

---

## Search

Cari berdasarkan.

Judul

Deskripsi

Kategori

---

## Filter

Status

Priority

Kategori

---

## Sorting

Deadline

Priority

Tanggal Dibuat

Nama

---

## Daftar

Menampilkan.

Judul

Deadline

Priority

Checklist Progress

Status

---

## FAB

Tambah Task.

---

## Swipe Action

Geser kanan.

Complete.

Geser kiri.

Delete.

---

## Undo

Setelah Complete.

Snackbar muncul selama 3 detik.

---

## Empty State

Belum ada task.

---

# Detail Task

## Komponen

Judul

Priority

Deadline

Checklist

Catatan

Lampiran

Reminder

---

## Tombol

Edit

Delete

Complete

---

## Completed

Jika selesai.

Pindah ke halaman Completed.

---

# Create Task

## Input

Judul

Deskripsi

Priority

Deadline

Kategori

Checklist

Reminder

Lampiran

---

## Validasi

Judul wajib.

Deadline opsional.

---

# Schedule

## Daftar

Mirip Task.

Namun menggunakan jam mulai dan selesai.

---

## Detail

Menampilkan.

Jam

Lokasi

Repeat

Reminder

Lampiran

Catatan

---

## Create Schedule

Input.

Judul

Jam Mulai

Jam Selesai

Repeat

Reminder

Priority

Kategori

Lokasi

Catatan

Lampiran

---

## Validasi

Jam selesai harus lebih besar.

Jika bentrok.

Tampilkan dialog konfirmasi.

---

# Habit

## Tujuan

Mengelola kebiasaan.

---

## Daftar

Setiap Habit menampilkan.

Nama

Progress

Target

Streak

---

## Detail

Nama

Target

Reminder

History

Session

Statistik

---

## Create Habit

Nama

Target

Reminder

Repeat

Icon

Warna

---

## Empty State

Belum ada Habit.

# UI_SPEC.md (Part 2)

# Progress

## Tujuan

Menampilkan seluruh perkembangan pengguna dalam satu halaman.

Progress menjadi pusat untuk melihat konsistensi, bukan mengelola aktivitas.

---

## Tab

Halaman Progress terdiri dari beberapa tab.

* Habit
* Study
* Statistics
* Heatmap
* Achievement

Posisi tab dapat di-scroll jika tidak muat.

---

# Habit

## Tujuan

Menampilkan seluruh habit beserta progres hari ini.

---

## Komponen

Daftar Habit.

Setiap Habit menampilkan.

* Icon
* Nama
* Progress
* Target
* Streak
* Status

---

## Status

Belum Dimulai

Sedang Berjalan

Selesai

---

## Interaksi

Klik Habit.

Membuka Detail Habit.

---

## Detail Habit

### Komponen

Nama

Target

Progress Hari Ini

Streak

Streak Tertinggi

Reminder

History

Study Session

Catatan

---

### Tombol

Start

Tambah Session Manual

Edit

Delete

---

## Empty State

Belum ada Habit.

---

# Study Tracker

## Tujuan

Melihat seluruh aktivitas belajar.

---

## Komponen

Ringkasan

History

Filter

FAB

---

## Ringkasan

Menampilkan.

Total Jam

Jumlah Session

Rata-rata Durasi

Target Hari Ini

Progress Hari Ini

---

## History

Daftar seluruh Study Session.

Setiap Session menampilkan.

Nama Habit

Tanggal

Jam Mulai

Jam Selesai

Durasi

Catatan

---

## Filter

Hari

Minggu

Bulan

Tahun

Custom Range

---

## FAB

Tambah Session Manual.

---

## Interaksi

Klik Session.

Membuka Detail Session.

---

## Empty State

Belum ada Study Session.

---

# Detail Study Session

## Komponen

Habit

Tanggal

Jam Mulai

Jam Selesai

Durasi

Catatan

---

## Tombol

Edit

Delete

---

# Tambah Session Manual

## Input

Habit

Tanggal

Jam Mulai

Jam Selesai

Durasi

Catatan

---

## Validasi

Durasi harus lebih dari nol.

Jam selesai harus lebih besar dari jam mulai.

---

# Focus Mode

## Tujuan

Memberikan tampilan yang bebas distraksi.

---

## Komponen

Nama Aktivitas

Nama Habit

Timer

Progress

---

## Tombol

Pause

Resume

Stop

---

## Rules

Tidak ada Bottom Navigation.

Tidak ada FAB.

Tidak ada menu lain.

---

## Exit

Keluar dari Focus Mode.

Timer tetap berjalan.

---

# Timer

## Tujuan

Menghitung durasi aktivitas.

---

## Komponen

Nama Aktivitas

Durasi

Status

---

## Tombol

Start

Pause

Resume

Stop

---

## Rules

Hanya satu timer aktif.

---

## Interaksi

Start.

Masuk Focus Mode.

---

# Pomodoro

## Tujuan

Membantu pengguna belajar dengan interval fokus.

---

## Komponen

Durasi Fokus

Durasi Istirahat

Jumlah Sesi

Status

---

## Tombol

Start

Pause

Resume

Stop

Reset

---

## Saat Fokus

Menampilkan.

Countdown

Sesi Saat Ini

Progress

---

## Saat Istirahat

Menampilkan.

Countdown

Sisa Waktu

---

## Selesai

Menampilkan ringkasan.

Total Fokus

Total Istirahat

Jumlah Sesi

---

# Statistics

## Tujuan

Menampilkan statistik perkembangan.

---

## Komponen

Filter

Card Statistik

---

## Card Belajar

Total Jam

Target

Rata-rata

---

## Card Habit

Habit Selesai

Habit Gagal

Streak

---

## Card Task

Task Selesai

Task Terlambat

Task Aktif

---

## Card Schedule

Schedule Selesai

Schedule Terlewat

Schedule Dibatalkan

---

## Filter

Hari

Minggu

Bulan

Tahun

Custom

---

## Interaksi

Klik Card.

Membuka statistik detail.

---

## Empty State

Belum ada data.

---

# Heatmap

## Tujuan

Memberikan gambaran konsistensi belajar.

---

## Komponen

Heatmap

Legenda

Ringkasan

---

## Heatmap

Satu kotak mewakili satu hari.

Semakin tinggi durasi.

Semakin gelap warna.

---

## Legenda

Tidak Belajar

Sedikit

Sedang

Banyak

Sangat Banyak

---

## Ringkasan

Total Hari Aktif

Hari Terbaik

Durasi Terlama

---

## Interaksi

Klik satu hari.

Membuka Detail Hari.

---

## Empty State

Belum ada data.

---

# Detail Hari

## Menampilkan

Tanggal

Durasi Belajar

Study Session

Task

Schedule

Habit

---

# Achievement

## Tujuan

Memberikan motivasi.

---

## Komponen

Progress

Achievement List

---

## Achievement

Setiap item menampilkan.

Icon

Nama

Deskripsi

Status

Progress

---

## Status

Locked

Unlocked

---

## Interaksi

Klik Achievement.

Membuka Detail.

---

# Detail Achievement

## Menampilkan

Nama

Deskripsi

Cara Membuka

Tanggal Dibuka

Progress

---

# Search

## Tujuan

Mencari data dari seluruh aplikasi.

---

## Komponen

Search Bar

Filter

Result

---

## Filter

Semua

Task

Schedule

Habit

Study Session

---

## Result

Dikelompokkan berdasarkan kategori.

---

## Empty State

Data tidak ditemukan.

---

# Global FAB

## Tujuan

Menambah data dengan cepat.

---

## Saat Ditekan

Menampilkan Bottom Sheet.

Pilihan.

Tambah Schedule

Tambah Task

Tambah Habit

Tambah Study Session Manual

---

# Snackbar

Digunakan untuk.

Undo Complete

Undo Delete

Informasi Singkat

---

# Dialog

Digunakan untuk.

Konfirmasi Delete

Konfirmasi Restore

Konfirmasi Conflict Schedule

Konfirmasi Reset Data

Konfirmasi Stop Timer

---

# Loading

Gunakan Loading hanya jika benar-benar diperlukan.

Jangan menampilkan Loading untuk operasi yang sangat cepat.

---

# Empty State

Semua halaman harus memiliki.

Icon

Judul

Deskripsi singkat

Tombol aksi jika diperlukan.

---

# Error State

Semua halaman harus memiliki.

Judul Error

Deskripsi

Tombol Coba Lagi

# UI_SPEC.md (Part 3)

# Settings

## Tujuan

Mengatur preferensi aplikasi.

---

## Daftar Menu

* Appearance
* Reminder
* Habit
* Backup & Restore
* About

---

# Appearance

## Komponen

Theme

---

## Pilihan

* Light
* Dark
* System

Perubahan diterapkan secara langsung.

---

# Reminder

## Tujuan

Mengatur perilaku reminder.

---

## Komponen

Default Snooze

Suara

Getaran

Reminder Agresif

---

## Default Snooze

Pilihan.

* 5 Menit
* 10 Menit
* 15 Menit

---

## Reminder Agresif

Jika aktif.

Reminder akan semakin sering muncul ketika diabaikan.

---

# Habit Settings

## Komponen

Default Target Belajar

Default Reminder

Restore Streak

---

## Restore Streak

Menampilkan.

Sisa Restore Bulan Ini

Contoh.

2 / 2

---

# Backup & Restore

## Tujuan

Melindungi data pengguna.

---

## Komponen

Export Data

Import Data

Informasi Backup Terakhir

---

## Export

Menampilkan.

Lokasi penyimpanan.

Ukuran file.

Tanggal export.

---

## Import

Sebelum import.

Tampilkan dialog konfirmasi.

---

## Dialog

Judul.

Import Data

Isi.

Seluruh data saat ini akan diganti dengan data dari file backup.

Aksi.

Batal

Import

---

# About

## Menampilkan

Nama Aplikasi

Mother

Versi

Developer

Versi Database

Lisensi Open Source

---

# Notification Permission

## Tujuan

Meminta izin notifikasi.

---

## Flow

Jika izin belum diberikan.

Tampilkan halaman penjelasan.

Setelah itu.

Minta permission Android.

---

## Jika Ditolak

Tampilkan informasi.

Reminder tidak akan bekerja tanpa izin notifikasi.

---

# Alarm Permission

Jika Android memerlukan izin alarm.

Tampilkan penjelasan.

Arahkan pengguna ke halaman pengaturan.

---

# Splash Screen

## Tujuan

Menampilkan identitas aplikasi saat startup.

---

## Komponen

Logo

Nama Aplikasi

Versi

---

## Rules

Tidak lebih dari beberapa detik.

Tidak ada tombol.

---

# Onboarding

Versi pertama hanya ditampilkan satu kali.

---

## Halaman 1

Apa itu Mother.

---

## Halaman 2

Fitur utama.

* Schedule
* Task
* Habit
* Study Tracker

---

## Halaman 3

Penjelasan Reminder.

---

## Halaman 4

Meminta izin.

* Notification
* Alarm

---

## Tombol

Lewati

Lanjut

Mulai

---

# Detail Schedule

## Komponen

Judul

Kategori

Priority

Tanggal

Jam

Lokasi

Repeat Rule

Reminder

Lampiran

Catatan

Status

---

## Tombol

Mulai

Edit

Delete

---

# Edit Schedule

Input sama dengan Create Schedule.

Data lama harus otomatis terisi.

---

# Detail Habit

## Komponen

Nama

Target

Progress Hari Ini

Streak

Streak Tertinggi

Reminder

Repeat Rule

History

Study Session

Catatan

---

## Tombol

Start

Tambah Session Manual

Edit

Delete

---

# Detail Statistics

## Tujuan

Menampilkan informasi lebih rinci.

---

## Komponen

Ringkasan

Grafik

Daftar Aktivitas

---

## Filter

Hari

Minggu

Bulan

Tahun

Custom

---

# Detail Heatmap

## Komponen

Tanggal

Durasi

Daftar Session

Daftar Task

Daftar Schedule

---

# Completed Task

## Tujuan

Menampilkan seluruh task yang selesai.

---

## Komponen

Search

Filter

Daftar Task

---

## Interaksi

Klik Task.

Membuka Detail.

---

# Archived Schedule

## Tujuan

Menampilkan riwayat Schedule.

---

## Komponen

Filter

Search

Daftar Schedule

---

# Archived Study Session

## Tujuan

Menampilkan seluruh riwayat belajar.

---

## Komponen

Search

Filter

Daftar Session

---

# Bottom Sheet

Digunakan untuk.

Quick Add

Filter

Sorting

Pemilihan Kategori

Pemilihan Icon

---

# Date Picker

Digunakan untuk.

Task

Schedule

Session Manual

---

# Time Picker

Digunakan untuk.

Schedule

Session Manual

Reminder

Pomodoro

---

# File Picker

Digunakan untuk.

Image

PDF

Backup File

---

# Navigation Flow

## Dashboard

↓

Task Detail

↓

Edit

↓

Dashboard

---

## Dashboard

↓

Schedule Detail

↓

Edit

↓

Dashboard

---

## Progress

↓

Habit Detail

↓

Start Timer

↓

Focus Mode

↓

Progress

---

## Progress

↓

Study Tracker

↓

Session Detail

↓

Edit

↓

Study Tracker

---

## Calendar

↓

Detail Activity

↓

Edit

↓

Calendar

---

# FAB Flow

FAB

↓

Bottom Sheet

↓

Pilih Jenis

↓

Form

↓

Simpan

↓

Kembali ke halaman sebelumnya

---

# Search Flow

Search

↓

Hasil

↓

Detail

↓

Edit

↓

Kembali ke hasil pencarian

---

# Import Flow

Settings

↓

Backup

↓

Import

↓

Konfirmasi

↓

Restore

↓

Restart Aplikasi

---

# Export Flow

Settings

↓

Backup

↓

Export

↓

Pilih Lokasi

↓

Selesai

---

# State yang Harus Ditangani

Setiap halaman minimal memiliki state berikut.

Loading

Success

Empty

Error

---

# Animasi

Animasi digunakan seperlunya.

Contoh.

Perpindahan halaman.

Progress.

Complete.

Snackbar.

Bottom Sheet.

Dialog.

Jangan menggunakan animasi yang mengganggu.

---

# Responsive

UI harus tetap nyaman digunakan.

Portrait.

Landscape.

Layar kecil.

Layar besar.

Tablet.

---

# Prinsip UI

Setiap halaman harus menjawab tiga pertanyaan.

Apa yang harus dilakukan pengguna?

Apa informasi yang paling penting?

Apa tindakan berikutnya?

Jika sebuah komponen tidak membantu menjawab salah satu pertanyaan tersebut, pertimbangkan untuk menghapusnya.

---

# Konsistensi UI

Semua halaman harus memiliki pola yang sama.

* Header konsisten.
* Tombol utama berada di posisi yang sama.
* FAB memiliki perilaku yang sama.
* Dialog menggunakan gaya yang sama.
* Bottom Sheet memiliki struktur yang sama.
* Empty State memiliki format yang sama.
* Error State memiliki format yang sama.

---

# Checklist Sebelum Screen Dianggap Selesai

Pastikan setiap screen memiliki.

* Tujuan yang jelas.
* Navigasi yang benar.
* Empty State.
* Error State.
* Loading State.
* Validasi input.
* Konfirmasi aksi berisiko.
* Mendukung Light Theme.
* Mendukung Dark Theme.
* Mengikuti filosofi Mother.

---

# Filosofi UI Mother

UI Mother harus membuat pengguna dapat memahami kondisi hari ini dalam beberapa detik.

Setiap elemen di layar harus memiliki tujuan yang jelas.

Hindari layar yang penuh informasi, menu yang berlebihan, dan tindakan yang membingungkan.

Fokus utama UI adalah membantu pengguna segera mengambil tindakan, bukan sekadar menampilkan data.