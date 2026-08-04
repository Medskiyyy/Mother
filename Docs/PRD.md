# PRD.md (Part 1)

# Mother

> **Personal Operating System untuk membantu pengguna menjalani hari dengan lebih terarah, disiplin, dan konsisten.**

---

# 1. Project Overview

## Ringkasan

Mother adalah aplikasi Android offline yang membantu pengguna mengatur seluruh aktivitas harian dalam satu tempat.

Aplikasi menggabungkan beberapa konsep menjadi satu sistem yang saling terintegrasi.

* Daily Planner
* Schedule Manager
* Task Manager
* Habit Tracker
* Study Tracker
* Focus Timer
* Pomodoro
* Progress Tracker

Mother **bukan** aplikasi kalender biasa atau aplikasi to-do list biasa.

Mother berperan sebagai sistem yang membantu pengguna mengetahui apa yang harus dilakukan hari ini, mengingatkan ketika ada aktivitas yang terlewat, serta membantu menjaga konsistensi dalam jangka panjang.

---

# 2. Tujuan Produk

Mother dibuat untuk membantu pengguna:

* Tidak lupa aktivitas harian.
* Tidak lupa deadline tugas.
* Memiliki jadwal yang jelas setiap hari.
* Konsisten menjalankan kebiasaan.
* Memiliki tracking progres belajar.
* Meningkatkan disiplin melalui streak dan reminder.
* Memiliki statistik perkembangan yang jelas.

---

# 3. Masalah yang Ingin Diselesaikan

Pengguna sering mengalami:

* Lupa mengerjakan tugas.
* Lupa jadwal.
* Tidak konsisten belajar.
* Tidak tahu prioritas hari ini.
* Tidak memiliki sistem untuk mengukur perkembangan.
* Sulit mempertahankan rutinitas.

Mother hadir untuk menjadi sistem eksternal yang membantu pengguna menjalankan hari dengan lebih terarah.

---

# 4. Target User

## Primary User

Pengguna utama adalah individu yang:

* Pelupa.
* Memiliki banyak aktivitas.
* Sedang membangun kebiasaan baru.
* Ingin meningkatkan produktivitas.
* Ingin tracking waktu belajar.
* Lebih suka aplikasi offline.

Versi pertama dibuat khusus untuk kebutuhan developer sendiri.

Namun struktur aplikasi harus cukup fleksibel agar dapat digunakan pengguna lain.

---

# 5. Platform

## Platform

Android

## Teknologi

Native Android

Kotlin

Jetpack Compose

Material 3

Room Database

MVVM Architecture

Offline First

Tidak menggunakan backend.

Tidak memerlukan login.

Seluruh data disimpan secara lokal.

---

# 6. Filosofi Produk

Mother mempunyai filosofi sederhana.

> Ketika pengguna membuka aplikasi, dalam waktu kurang dari 5 detik pengguna harus mengetahui apa yang harus dilakukan hari ini.

Semua fitur harus mendukung filosofi tersebut.

Jika suatu fitur tidak membantu pengguna menyelesaikan aktivitas hari ini, maka fitur tersebut bukan prioritas.

---

# 7. Design Principles

## Simple

UI sederhana.

Tidak ramai.

Tidak membingungkan.

---

## Fast

Semua interaksi harus cepat.

Tidak ada loading yang tidak perlu.

---

## Offline First

Semua fitur dapat digunakan tanpa internet.

---

## Motivating

Aplikasi harus membuat pengguna ingin kembali setiap hari.

Menggunakan:

* Streak
* Progress
* Statistik
* Heatmap
* Achievement

---

## Action Oriented

Fokus aplikasi bukan mencatat.

Fokus aplikasi adalah membantu pengguna benar-benar mengerjakan aktivitas.

---

# 8. Core Features

Mother memiliki beberapa modul utama.

## Dashboard

Menampilkan kondisi hari ini.

## Schedule

Mengatur jadwal aktivitas.

## Task

Mengelola tugas dan deadline.

## Habit

Mengelola kebiasaan.

## Study Tracker

Tracking durasi belajar.

## Focus Session

Mode fokus.

## Pomodoro

Timer belajar.

## Statistics

Melihat perkembangan.

## Calendar

Melihat aktivitas berdasarkan tanggal.

## Achievement

Reward untuk konsistensi.

## Widget

Informasi penting di Home Screen.

## Backup

Backup & Restore database.

---

# 9. Dashboard

## Tujuan

Dashboard menjadi halaman pertama ketika aplikasi dibuka.

Dashboard harus menjawab pertanyaan:

> "Apa yang harus saya lakukan hari ini?"

Dashboard tidak boleh dipenuhi grafik yang tidak penting.

Dashboard harus fokus pada informasi yang dapat ditindaklanjuti.

---

## Komponen Dashboard

### Greeting

Contoh

Selamat Pagi.

Selamat Siang.

Selamat Malam.

Greeting berubah otomatis berdasarkan waktu.

---

### Hari & Tanggal

Contoh

Selasa

4 Agustus 2026

---

### Streak

Menampilkan streak aktif.

Contoh

🔥 24 Hari

---

### Target Hari Ini

Progress target belajar.

Contoh

1 jam 20 menit / 2 jam

Menggunakan progress bar.

---

### Aktivitas Berikutnya

Menampilkan aktivitas yang akan datang.

Informasi:

* Judul
* Jam
* Countdown
* Tombol Mulai

Jika tidak ada aktivitas.

Tampilkan:

"Tidak ada aktivitas berikutnya."

---

### Deadline Terdekat

Menampilkan maksimal tiga deadline terdekat.

Urut berdasarkan waktu.

---

### Jadwal Hari Ini

Menampilkan seluruh aktivitas hari ini.

Setiap aktivitas memiliki:

* Icon
* Warna kategori
* Jam
* Status
* Nama aktivitas

---

### Quick Action

Berisi shortcut.

Minimal:

* Tambah Task
* Tambah Schedule
* Start Timer

---

# 10. Dashboard Rules

Dashboard selalu membuka tanggal hari ini.

Dashboard otomatis refresh ketika hari berganti.

Progress belajar diperbarui secara realtime.

Countdown diperbarui otomatis.

Task selesai tidak muncul di Dashboard.

Activity selesai tetap muncul dengan status selesai.

---

# 11. Dashboard Edge Cases

## Tidak ada aktivitas

Tampilkan empty state.

## Tidak ada task

Tampilkan pesan bahwa semua tugas selesai.

## Semua target selesai

Progress berubah menjadi penuh.

Berikan animasi sederhana.

## Tidak ada streak

Tampilkan:

🔥 0 Hari

## Hari baru

Semua habit otomatis reset.

Task tidak reset.

Schedule mengikuti tanggal.

---

# 12. Schedule Module

## Tujuan

Mengatur seluruh aktivitas berdasarkan waktu.

Contoh:

* Kuliah
* Gym
* Belajar
* Meeting
* Tidur
* Ibadah

---

## Data Schedule

Setiap schedule memiliki:

* Judul
* Deskripsi
* Kategori
* Warna
* Icon
* Lokasi (opsional)
* Catatan
* Lampiran
* Jam mulai
* Jam selesai
* Reminder
* Repeat Rule
* Priority
* Status

---

## Priority

Empat level.

* Aman
* Waspada
* Mepet
* Urgent

Priority dapat diubah kapan saja.

---

## Repeat

Schedule dapat berulang.

Contoh:

* Tidak berulang
* Harian
* Mingguan
* Bulanan
* Custom

---

## Reminder

Reminder dapat memiliki lebih dari satu waktu.

Contoh

30 menit sebelumnya.

10 menit sebelumnya.

Saat mulai.

---

## Reminder Behavior

Ketika reminder berbunyi.

User wajib memilih salah satu:

* Mulai
* Snooze
* Lewati

Reminder tidak boleh hilang begitu saja.

---

## Snooze

Minimal tersedia.

* 5 menit
* 10 menit
* 15 menit

---

## Schedule Conflict

Jika ada jadwal bertabrakan.

Tampilkan dialog.

```
Jadwal bertabrakan.

Lanjutkan?

[Ya]

[Tidak]
```

User tetap boleh menyimpan jadwal.

---

## Focus Session

Jika schedule memiliki timer.

User dapat menekan:

Start

Timer mulai berjalan.

Hanya satu timer yang boleh aktif dalam satu waktu.

Jika mencoba memulai timer lain.

Muncul dialog:

"Selesaikan timer sebelumnya terlebih dahulu."

---

## Study Schedule

Schedule belajar memiliki kemampuan tambahan.

Memiliki target harian.

Dapat dihubungkan dengan Habit.

Dapat menghasilkan Study Session.

Tidak langsung menghitung progres.

Progress dihitung berdasarkan total Study Session.

---

## Schedule Status

Setiap schedule memiliki status.

Belum Dimulai

Sedang Berjalan

Selesai

Terlewat

Dibatalkan

Status berubah otomatis berdasarkan kondisi.

---

## Schedule Completion

User dapat menandai selesai.

Jika tidak sengaja.

Undo tersedia selama 3 detik.

---

## Schedule Archive

Schedule lama tetap disimpan.

Dapat dihapus manual oleh user.

---

## Edge Cases

User mengubah jam ketika timer berjalan.

User menghapus schedule yang sedang berjalan.

User mengubah repeat rule.

User mengubah tanggal.

User mengubah timezone.

User memindahkan aktivitas ke hari lain.

Semua kondisi tersebut harus ditangani tanpa menyebabkan data Study Session atau riwayat menjadi rusak.

---
# PRD.md (Part 2)

# 13. Task Module

## Tujuan

Task digunakan untuk mencatat seluruh pekerjaan yang harus diselesaikan.

Task berbeda dengan Schedule.

Schedule memiliki waktu pelaksanaan.

Task memiliki deadline.

Contoh:

• Mengerjakan Laporan
• Membuat Presentasi
• Mengumpulkan Tugas
• Membayar UKT

---

## Data Task

Setiap task memiliki:

* ID
* Judul
* Deskripsi
* Deadline
* Priority
* Kategori
* Status
* Checklist
* Catatan
* Lampiran
* Created At
* Updated At
* Completed At

---

## Priority

Empat level.

* Aman
* Waspada
* Mepet
* Urgent

---

## Status

* Active
* Completed
* Overdue
* Archived

---

## Checklist

Task dapat memiliki checklist.

Contoh

☐ Membuat UI

☐ Membuat Database

☑ Testing

Checklist dapat ditambah, diubah, dan dihapus.

---

## Lampiran

Task mendukung:

* Gambar
* PDF

---

## Completed

Ketika task selesai.

Task berpindah ke halaman Completed.

Task tidak lagi muncul di Dashboard.

Tetap masuk ke statistik.

---

## Undo

Setelah Complete.

Undo tersedia selama 3 detik.

---

## Deadline

Semakin dekat deadline.

Semakin tinggi tingkat peringatannya.

Contoh

7 hari

Normal.

3 hari

Waspada.

1 hari

Mepet.

Hari ini

Urgent.

---

## Reminder

Reminder dapat memiliki beberapa waktu.

Contoh

1 hari sebelumnya

1 jam sebelumnya

15 menit sebelumnya

Saat deadline

---

## Search

Task dapat dicari berdasarkan.

* Judul
* Deskripsi
* Kategori

---

## Sorting

Task dapat diurutkan berdasarkan.

* Deadline
* Priority
* Nama
* Tanggal dibuat

---

## Filter

Filter berdasarkan.

* Status
* Priority
* Kategori

---

## Edge Cases

Deadline diubah.

Task dipindahkan ke tanggal lain.

Task selesai sebelum deadline.

Task selesai setelah deadline.

Task tanpa checklist.

Task tanpa lampiran.

Task tanpa reminder.

---

# 14. Habit Module

## Tujuan

Habit digunakan untuk membangun kebiasaan.

Habit adalah inti dari aplikasi Mother.

---

## Contoh Habit

Belajar Cyber Security

Belajar Bahasa Inggris

Gym

Membaca Buku

Meditasi

---

## Data Habit

Setiap habit memiliki.

* Nama
* Icon
* Warna
* Target
* Repeat Rule
* Reminder
* Status
* Streak
* Restore Streak
* Catatan

---

## Repeat

Habit mendukung.

* Harian

* Mingguan

* Bulanan

* Custom

---

## Target

Target dapat berupa.

Durasi

Contoh

2 jam.

atau

Jumlah.

Contoh

Minum Air

8 kali.

Versi pertama fokus pada target durasi.

---

## Daily Reset

Habit otomatis reset setiap hari.

User harus menyelesaikan kembali.

---

## Completion Rule

Habit dianggap selesai apabila.

Total Study Session >= Target

Contoh.

Target

2 jam.

Hari ini.

45 menit

30 menit

50 menit

Total

2 jam 5 menit.

Habit otomatis selesai.

---

## Target Belum Tercapai

Jika target belum terpenuhi.

Habit tetap belum selesai.

Streak tidak bertambah.

---

## Override Target

Target dapat diubah untuk tanggal tertentu.

Tidak mengubah histori.

---

## Streak

Streak bertambah otomatis.

Tidak perlu tombol manual.

---

## Restore Streak

User memiliki.

2 kali restore setiap bulan.

Restore digunakan apabila streak putus.

Restore tidak dapat ditumpuk.

Sisa restore akan reset setiap awal bulan.

---

## Statistik

Habit menyimpan.

Jumlah hari selesai.

Jumlah hari gagal.

Total durasi.

Streak tertinggi.

Streak saat ini.

---

## Edge Cases

Target berubah.

Repeat berubah.

Habit dihapus.

Habit diarsipkan.

Target nol.

Hari libur.

Restore digunakan.

---

# 15. Study Tracker

## Tujuan

Mencatat seluruh aktivitas belajar.

Tracking dilakukan menggunakan timer.

---

## Study Session

Setiap sesi belajar menghasilkan satu Study Session.

Session tidak boleh digabung.

---

## Data Session

Setiap session memiliki.

* Habit
* Tanggal
* Jam Mulai
* Jam Selesai
* Durasi
* Catatan

---

## Manual Session

User boleh menambah session secara manual.

Digunakan apabila lupa menyalakan timer.

---

## Edit Session

Session dapat diedit.

---

## Delete Session

Session dapat dihapus.

Penghapusan mempengaruhi statistik.

---

## Rules

Semua statistik belajar dihitung dari Study Session.

Bukan dari Schedule.

Bukan dari Habit.

---

## Session History

Semua session disimpan.

Tidak dihapus otomatis.

---

## Filter

Filter berdasarkan.

Hari

Minggu

Bulan

Custom Range

---

## Insight

Study Tracker dapat menampilkan.

Total jam belajar.

Jumlah session.

Rata-rata durasi.

Hari paling produktif.

Jam paling produktif.

---

# 16. Timer

## Tujuan

Menghitung durasi aktivitas.

---

## Rules

Hanya satu timer aktif.

---

## Flow

Start

↓

Running

↓

Pause

↓

Resume

↓

Stop

↓

Simpan Session

---

## Auto Reminder

Jika timer berjalan terlalu lama.

Contoh.

3 jam.

Tampilkan notifikasi.

"Apakah Anda masih belajar?"

---

## Pause

Durasi pause tidak dihitung.

---

## Resume

Melanjutkan timer.

---

## Stop

Timer selesai.

Study Session dibuat.

---

## Edge Cases

HP restart.

Aplikasi ditutup.

Layar mati.

Timer tetap berjalan.

---

# 17. Pomodoro

## Tujuan

Membantu fokus.

---

## Rules

Durasi dapat diatur.

Contoh.

25 menit.

5 menit.

atau bebas.

---

## Flow

Focus

↓

Break

↓

Focus

↓

Break

↓

Selesai

---

## Session

Pomodoro dapat terhubung dengan Study Session.

Jika user memilih.

---

## Statistik

Jumlah pomodoro.

Total focus time.

Total break.

---

## Notification

Notifikasi muncul ketika.

Focus selesai.

Break selesai.

---

# 18. Focus Mode

## Tujuan

Mengurangi distraksi.

---

## Tampilan

Saat Focus Mode aktif.

Semua menu lain disembunyikan.

Yang tampil hanya.

Nama aktivitas.

Timer.

Pause.

Stop.

---

## Rules

User tidak dapat menjalankan timer lain.

---

## Exit

Keluar dari Focus Mode.

Timer tetap berjalan.

---

# 19. Search

Search dapat mencari.

Task.

Schedule.

Habit.

Study Session.

Catatan.

---

## Hasil

Hasil dikelompokkan berdasarkan kategori.

---

# 20. Achievement

## Tujuan

Memberikan motivasi.

---

## Contoh Achievement

Belajar 10 jam.

Belajar 100 jam.

Streak 7 hari.

Streak 30 hari.

Streak 100 hari.

100 Task selesai.

500 Task selesai.

100 Activity selesai.

---

## Status

Achievement.

Locked.

Unlocked.

---

## Tampilan

Achievement yang belum terbuka tetap terlihat.

Namun dalam kondisi terkunci.

---

# 21. Business Rules

Semua progress belajar berasal dari Study Session.

Habit tidak pernah di-complete secara manual.

Task selesai otomatis masuk Completed.

Schedule selesai tetap berada di riwayat.

Undo tersedia selama 3 detik.

Hanya satu timer aktif.

Habit reset setiap hari.

Target dapat dioverride per tanggal.

Restore streak maksimal dua kali setiap bulan.

Reminder wajib direspons.

Dashboard selalu membuka hari ini.

Semua fitur dapat digunakan tanpa internet.

Seluruh data disimpan secara lokal menggunakan Room.
# PRD.md (Part 3)

# 22. Calendar Module

## Tujuan

Calendar digunakan untuk melihat seluruh aktivitas berdasarkan tanggal.

Calendar bukan halaman utama, tetapi alat untuk melihat jadwal secara keseluruhan.

---

## View

Mendukung tiga tampilan.

* Month View
* Week View
* Day View

User dapat berpindah kapan saja.

---

## Filter

Calendar dapat difilter berdasarkan.

* Semua
* Schedule
* Task
* Habit
* Study Session

User dapat memilih lebih dari satu filter.

---

## Indikator

Tanggal yang memiliki aktivitas menampilkan indikator.

Jika terdapat beberapa kategori, indikator menggunakan warna kategori masing masing.

---

## Interaksi

Menekan tanggal akan membuka daftar aktivitas pada tanggal tersebut.

Menekan aktivitas akan membuka halaman detail.

---

## Business Rules

Calendar tidak menampilkan Task Completed secara default.

Calendar tetap menampilkan riwayat Schedule dan Study Session.

---

# 23. Statistics Module

## Tujuan

Memberikan gambaran perkembangan pengguna.

Statistik harus sederhana dan mudah dipahami.

Tidak boleh terlalu banyak grafik.

---

## Statistik Belajar

Menampilkan.

* Total jam belajar
* Total Study Session
* Rata rata durasi Session
* Target tercapai
* Hari gagal
* Hari berhasil
* Streak aktif
* Streak tertinggi

---

## Statistik Task

Menampilkan.

* Total Task
* Task selesai
* Task terlambat
* Task aktif

---

## Statistik Schedule

Menampilkan.

* Schedule selesai
* Schedule terlewat
* Schedule dibatalkan

---

## Filter

Semua statistik mendukung filter.

* Hari
* Minggu
* Bulan
* Tahun
* Custom Range

---

# 24. Heatmap

## Tujuan

Memberikan visualisasi konsistensi pengguna.

---

## Rules

Setiap kotak mewakili satu hari.

Semakin tinggi durasi belajar.

Semakin gelap warnanya.

---

## Data

Heatmap menggunakan total Study Session.

Bukan Habit.

---

## Interaksi

Menekan satu kotak akan membuka detail aktivitas hari tersebut.

---

# 25. Widget

## Tujuan

Menampilkan informasi penting tanpa membuka aplikasi.

---

## Small Widget

Menampilkan.

* Streak

---

## Medium Widget

Menampilkan.

* Progress target hari ini
* Durasi belajar

---

## Large Widget

Menampilkan.

* Jadwal hari ini
* Aktivitas berikutnya
* Progress belajar

---

## Rules

Widget diperbarui otomatis.

Widget tidak memiliki interaksi pada versi pertama.

---

# 26. Backup & Restore

## Tujuan

Melindungi data pengguna.

---

## Backup

Backup dilakukan secara manual.

Semua data diekspor menjadi satu file.

---

## Restore

User dapat mengembalikan data dari file backup.

Restore akan menggantikan seluruh data saat ini.

Sebelum restore tampilkan konfirmasi.

---

## Rules

Backup bersifat offline.

Tidak menggunakan cloud.

---

# 27. Notification System

## Reminder

Reminder digunakan untuk.

* Schedule
* Task
* Habit
* Pomodoro

---

## Behavior

Saat notifikasi muncul.

User wajib memilih salah satu.

* Mulai
* Snooze
* Lewati

---

## Snooze

Pilihan.

* 5 menit
* 10 menit
* 15 menit

---

## Smart Reminder

Jika reminder terus diabaikan.

Reminder menjadi lebih agresif.

Contoh.

Notifikasi kedua.

Getaran lebih lama.

Suara lebih kuat.

Frekuensi meningkat.

---

# 28. Search

Search bersifat global.

Mencari.

* Schedule
* Task
* Habit
* Study Session
* Catatan

---

# 29. Settings

## Theme

Pilihan.

* Light
* Dark
* System

---

## Reminder

User dapat mengatur.

* Suara
* Getaran
* Volume
* Snooze Default

---

## Habit

User dapat mengatur.

* Default Target
* Default Reminder

---

## Backup

Halaman untuk.

* Export Data
* Import Data

---

## About

Berisi.

* Nama aplikasi
* Versi
* Changelog
* Lisensi

---

# 30. Navigation

Menggunakan Bottom Navigation.

## Dashboard

Ringkasan hari ini.

---

## Calendar

Jadwal.

---

## Tasks

Task aktif dan Completed.

---

## Progress

Habit, Study Tracker, Statistics, Heatmap, Achievement.

---

## Settings

Pengaturan aplikasi.

---

# 31. Warna

Kategori memiliki warna yang dapat diubah user.

Contoh.

Kuliah

Biru

Belajar

Ungu

Gym

Hijau

Deadline

Merah

Ibadah

Kuning

---

# 32. Icon

Setiap kategori memiliki icon.

User dapat memilih icon sendiri.

---

# 33. Lampiran

Task dan Schedule mendukung.

* Image
* PDF

Versi pertama tidak mendukung video.

---

# 34. Empty State

Semua halaman wajib memiliki Empty State.

Contoh.

Belum ada Task.

Belum ada Habit.

Belum ada Study Session.

Belum ada Schedule.

---

# 35. Error Handling

Aplikasi tidak boleh crash.

Jika terjadi error.

Tampilkan pesan yang mudah dipahami.

Seluruh operasi database menggunakan try catch.

---

# 36. Performance

Target.

Startup < 2 detik.

Perpindahan halaman < 300 ms.

Pencarian < 300 ms.

Scroll tetap 60 FPS.

---

# 37. Accessibility

Ukuran teks mengikuti sistem Android.

Mendukung TalkBack.

Kontras warna cukup tinggi.

Area sentuh minimal 48dp.

---

# 38. Non Functional Requirements

Android Native.

Kotlin.

Jetpack Compose.

Material 3.

Room Database.

MVVM.

Offline First.

Tidak membutuhkan akun.

Tidak membutuhkan internet.

Seluruh data berada di perangkat.

---

# 39. MVP Scope

Versi pertama wajib memiliki.

## Dashboard

Semua komponen utama.

---

## Schedule

CRUD.

Reminder.

Repeat.

Conflict Warning.

---

## Task

CRUD.

Checklist.

Reminder.

Completed.

---

## Habit

CRUD.

Target.

Streak.

Restore Streak.

---

## Study Tracker

Timer.

Manual Session.

History.

---

## Focus Mode

Minimal.

---

## Pomodoro

Durasi Custom.

---

## Calendar

Month.

Week.

Day.

---

## Statistics

Total Jam.

Streak.

Task.

Schedule.

---

## Heatmap

Minimal.

---

## Widget

Small.

Medium.

Large.

---

## Backup

Export.

Import.

---

## Search

Global Search.

---

## Settings

Theme.

Reminder.

Backup.

---

# 40. Future Scope

Fitur yang tidak masuk MVP.

Google Calendar Sync.

Wear OS.

Cloud Sync.

Multi Device.

AI Assistant.

OCR Task Scanner.

Voice Input.

Desktop Version.

iOS Version.

Collaboration.

Shared Schedule.

Shared Habit.

Shared Task.

---

# 41. Out of Scope

Tidak ada login.

Tidak ada backend.

Tidak ada cloud.

Tidak ada AI.

Tidak ada fitur sosial.

Tidak ada chat.

Tidak ada iklan.

Tidak ada subscription.

---

# 42. Definisi Selesai (Definition of Done)

Sebuah fitur dianggap selesai apabila.

* Seluruh requirement pada PRD terpenuhi.
* Tidak ada crash.
* UI sesuai spesifikasi.
* Mendukung Dark Mode.
* Seluruh data tersimpan dengan benar.
* Reminder bekerja.
* State tetap benar setelah aplikasi ditutup.
* Statistik diperbarui otomatis.
* Tidak merusak fitur lain.

---

# 43. Prinsip Pengembangan

Setiap fitur baru harus menjawab pertanyaan berikut.

"Apakah fitur ini membantu pengguna mengetahui apa yang harus dilakukan hari ini?"

Jika tidak.

Fitur tersebut bukan prioritas.

---

# 44. Ringkasan Filosofi Mother

Mother bukan sekadar aplikasi produktivitas.

Mother adalah sistem pendamping pribadi yang membantu pengguna menjalani hari dengan lebih terarah.

Setiap fitur harus mendukung empat tujuan utama.

* Mengingatkan pengguna.
* Membantu pengguna mengambil tindakan.
* Membangun konsistensi.
* Menunjukkan perkembangan secara nyata.

Keputusan desain, implementasi, maupun penambahan fitur harus selalu mengacu pada filosofi ini agar aplikasi tetap sederhana, fokus, dan benar benar bermanfaat.