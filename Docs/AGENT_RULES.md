# AGENT_RULES.md

# Mother AI Coding Rules

Dokumen ini berisi aturan yang wajib dipatuhi selama pengembangan.

Selalu baca `PRD.md` sebelum mengerjakan tugas.

Jika ada konflik, `PRD.md` adalah sumber kebenaran utama.

---

# 1. Aturan Umum

* Jangan menambah fitur yang tidak ada di PRD.
* Jangan menghapus fitur tanpa instruksi.
* Jangan mengubah perilaku fitur yang sudah sesuai PRD.
* Jangan membuat asumsi jika requirement tidak jelas.
* Jika ragu, berhenti dan minta klarifikasi.

---

# 2. Prioritas

Urutan prioritas.

1. PRD
2. Stabilitas aplikasi
3. Kualitas kode
4. Tampilan

---

# 3. Teknologi

Gunakan.

* Kotlin
* Jetpack Compose
* Material 3
* Room
* MVVM

Jangan menambahkan library jika fitur dapat dibuat menggunakan library yang sudah ada.

---

# 4. Arsitektur

Pisahkan.

* UI
* ViewModel
* Repository
* Database

Jangan meletakkan business logic di UI.

ViewModel tidak boleh mengetahui implementasi database secara langsung.

---

# 5. UI

UI hanya menampilkan state.

UI tidak boleh berisi business logic.

Semua ukuran menggunakan dp atau sp.

Jangan hardcode string.

Gunakan Material Theme.

Gunakan warna dari Theme.

---

# 6. State

Gunakan satu sumber state.

Jangan membuat state yang sama di beberapa tempat.

State harus tetap konsisten setelah rotasi layar.

---

# 7. Navigation

Gunakan Navigation Compose.

Jangan menggunakan Intent untuk perpindahan antar screen kecuali memang diperlukan.

---

# 8. Database

Semua data disimpan menggunakan Room.

Jangan menghapus tabel.

Jangan mengubah struktur tabel tanpa migration.

Jangan menghapus data pengguna secara otomatis.

---

# 9. Data

Gunakan ID yang stabil.

Gunakan timestamp UTC untuk penyimpanan waktu.

Jangan menyimpan data yang dapat dihitung ulang.

---

# 10. Error Handling

Jangan membiarkan aplikasi crash.

Tangani semua kemungkinan error.

Tampilkan pesan yang mudah dipahami.

---

# 11. Timer

Hanya satu timer boleh aktif.

Jangan membuat dua timer berjalan bersamaan.

Durasi harus tetap benar setelah aplikasi ditutup atau perangkat dikunci.

---

# 12. Reminder

Reminder harus tetap bekerja setelah perangkat restart.

Reminder tidak boleh hilang tanpa tindakan pengguna.

---

# 13. Habit

Habit reset setiap hari.

Streak dihitung otomatis.

Jangan menambah streak secara manual.

Progress dihitung dari Study Session.

---

# 14. Study Session

Satu sesi belajar menghasilkan satu Study Session.

Jangan menggabungkan beberapa session menjadi satu.

Session manual memiliki perilaku yang sama dengan session timer.

---

# 15. Task

Task selesai dipindahkan ke Completed.

Task Completed tidak muncul di Dashboard.

Task tetap masuk statistik.

---

# 16. Schedule

Schedule selesai tetap disimpan.

Schedule tidak dipindahkan ke Completed.

Jangan menghapus riwayat Schedule.

---

# 17. Statistik

Semua statistik berasal dari data asli.

Jangan menyimpan hasil perhitungan di database jika bisa dihitung saat dibutuhkan.

---

# 18. Performa

Hindari query database yang tidak diperlukan.

Jangan melakukan pekerjaan berat di Main Thread.

Gunakan lazy loading jika daftar mulai besar.

---

# 19. Kode

Tulis kode yang sederhana.

Hindari duplikasi.

Gunakan nama yang jelas.

Jangan membuat fungsi yang terlalu panjang.

Pisahkan fungsi berdasarkan tanggung jawab.

---

# 20. Komentar

Jangan menulis komentar yang menjelaskan hal yang sudah jelas.

Tambahkan komentar hanya jika logika cukup rumit.

---

# 21. Dokumentasi

Jika mengubah perilaku fitur.

Perbarui PRD jika diperlukan.

Jika mengubah database.

Perbarui TECH_SPEC.

---

# 22. Pengujian

Sebelum menyelesaikan tugas.

Pastikan.

* Build berhasil.
* Tidak ada error.
* Tidak ada warning penting.
* Fitur berjalan sesuai PRD.
* Tidak merusak fitur lain.

---

# 23. Sebelum Mengirim Hasil

Pastikan.

* Requirement sudah terpenuhi.
* Kode dapat dikompilasi.
* Tidak ada TODO yang tertinggal.
* Tidak ada kode yang tidak digunakan.
* Tidak ada import yang tidak digunakan.

---

# 24. Yang Tidak Boleh Dilakukan

Jangan.

* Mengubah PRD tanpa instruksi.
* Menambah fitur sendiri.
* Menghapus data pengguna.
* Menggunakan library tanpa alasan.
* Menyimpan data sementara sebagai solusi permanen.
* Menonaktifkan validasi agar error hilang.
* Menyembunyikan error dengan try catch kosong.
* Menggunakan hardcode untuk mengatasi bug.

---

# 25. Prinsip

Setiap perubahan harus membuat aplikasi.

* Lebih benar.
* Lebih sederhana.
* Lebih mudah dipelihara.
* Tetap sesuai PRD.

Jika perubahan tidak memenuhi tujuan tersebut, jangan lakukan.