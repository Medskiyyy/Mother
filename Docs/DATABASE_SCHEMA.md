# DATABASE_SCHEMA.md (Part 1)

# Mother Database Schema

Dokumen ini menjelaskan struktur database aplikasi Mother.

Database menggunakan Room sebagai penyimpanan lokal.

Seluruh data disimpan secara offline.

---

# Database

Nama

```
mother.db
```

Versi awal

```
1
```

---

# Prinsip

* Semua tabel menggunakan Primary Key berupa UUID.
* Gunakan Foreign Key jika terdapat relasi.
* Gunakan Index pada kolom yang sering dicari.
* Jangan menyimpan data yang bisa dihitung ulang.
* Semua waktu disimpan sebagai Unix Timestamp (UTC).
* Semua operasi menggunakan transaksi jika melibatkan lebih dari satu tabel.
* Soft Delete tidak digunakan.
* Data yang dihapus benar benar dihapus.

---

# Entity Overview

```
Category
│
├── Schedule
├── Task
└── Habit

Habit
│
└── StudySession

Task
│
├── Checklist
└── Attachment

Schedule
│
├── Reminder
└── Attachment

Habit
│
└── Reminder

Achievement

AppSetting

RestoreHistory
```

---

# Enum

## Priority

```
AMAN
WASPADA
MEPET
URGENT
```

---

## StatusTask

```
ACTIVE
COMPLETED
OVERDUE
```

---

## StatusSchedule

```
UPCOMING
RUNNING
COMPLETED
MISSED
CANCELLED
```

---

## RepeatType

```
NONE
DAILY
WEEKLY
MONTHLY
CUSTOM
```

---

## ReminderType

```
ONCE
REPEAT
```

---

## Theme

```
LIGHT
DARK
SYSTEM
```

---

## AttachmentType

```
IMAGE
PDF
```

---

# Category

Digunakan oleh.

* Schedule
* Task
* Habit

---

## Table

| Field | Type |
|--------|------|
| id | String (UUID) |
| name | String |
| icon | String |
| color | String |
| createdAt | Long |
| updatedAt | Long |

---

## Rules

Nama kategori harus unik.

Kategori tidak boleh dihapus jika masih digunakan.

---

# AppSetting

Digunakan menyimpan seluruh konfigurasi aplikasi.

Hanya memiliki satu row.

---

## Table

| Field | Type |
|--------|------|
| id | Int |
| theme | Theme |
| reminderEnabled | Boolean |
| aggressiveReminder | Boolean |
| defaultSnoozeMinute | Int |
| defaultStudyTargetMinute | Int |
| lastBackup | Long? |
| onboardingFinished | Boolean |

---

# Achievement

Achievement yang tersedia.

---

## Table

| Field | Type |
|--------|------|
| id | String |
| title | String |
| description | String |
| icon | String |
| target | Int |
| currentProgress | Int |
| unlocked | Boolean |
| unlockedAt | Long? |

---

## Rules

Achievement tidak boleh dihapus.

Progress dihitung otomatis.

---

# RestoreHistory

Mencatat penggunaan Restore Streak.

---

## Table

| Field | Type |
|--------|------|
| id | String |
| restoreDate | Long |
| reason | String? |

---

## Rules

Digunakan untuk menghitung.

Maksimal 2 restore setiap bulan.

---

# Relationship

Category

```
1
│
├──── Schedule
├──── Task
└──── Habit
```

Habit

```
1
│
└──── StudySession
```

Task

```
1
│
├──── Checklist
└──── Attachment
```

Schedule

```
1
│
├──── Reminder
└──── Attachment
```

Habit

```
1
│
└──── Reminder
```

---

# Index

Category

```
name
```

Achievement

```
unlocked
```

RestoreHistory

```
restoreDate
```

---

# Foreign Key

Schedule

```
categoryId

REFERENCES Category(id)
```

Task

```
categoryId

REFERENCES Category(id)
```

Habit

```
categoryId

REFERENCES Category(id)
```

---

# Cascade Rule

Category

Tidak boleh dihapus apabila masih digunakan.

Pengguna harus memindahkan seluruh data terlebih dahulu.

---

# Data Yang Tidak Disimpan

Jangan menyimpan.

* Total jam belajar.
* Total streak.
* Statistik mingguan.
* Statistik bulanan.
* Heatmap.

Semua dihitung dari data utama.

---

# Catatan

Seluruh statistik berasal dari.

* StudySession
* Task
* Schedule
* Habit

Tidak ada tabel statistik khusus.

---

# Naming Convention

Entity

```
PascalCase
```

Field

```
camelCase
```

Primary Key

```
id
```

Foreign Key

```
entityNameId
```

Timestamp

```
createdAt
updatedAt
completedAt
deletedAt
```

Gunakan suffix yang konsisten di seluruh tabel.

# DATABASE_SCHEMA.md (Part 2)

# Schedule

Digunakan untuk menyimpan seluruh aktivitas yang memiliki waktu pelaksanaan.

Contoh.

* Kuliah
* Gym
* Belajar
* Meeting
* Tidur

---

## Table

| Field | Type |
|--------|------|
| id | String (UUID) |
| title | String |
| description | String? |
| categoryId | String |
| priority | Priority |
| startTime | Long |
| endTime | Long |
| repeatType | RepeatType |
| customRepeatRule | String? |
| location | String? |
| note | String? |
| status | StatusSchedule |
| createdAt | Long |
| updatedAt | Long |

---

## Index

startTime

endTime

categoryId

status

---

## Rules

Jam selesai harus lebih besar dari jam mulai.

Schedule tidak dihapus setelah selesai.

Schedule dapat memiliki banyak Reminder.

Schedule dapat memiliki banyak Attachment.

---

# Task

Digunakan untuk menyimpan seluruh pekerjaan.

---

## Table

| Field | Type |
|--------|------|
| id | String (UUID) |
| title | String |
| description | String? |
| categoryId | String |
| priority | Priority |
| deadline | Long? |
| status | StatusTask |
| note | String? |
| completedAt | Long? |
| createdAt | Long |
| updatedAt | Long |

---

## Index

deadline

status

priority

categoryId

---

## Rules

Task selesai dipindahkan ke halaman Completed.

Task tetap disimpan.

Task dapat memiliki Checklist.

Task dapat memiliki Attachment.

Task dapat memiliki Reminder.

---

# Habit

Digunakan untuk menyimpan seluruh kebiasaan.

---

## Table

| Field | Type |
|--------|------|
| id | String (UUID) |
| categoryId | String |
| title | String |
| targetMinute | Int |
| repeatType | RepeatType |
| customRepeatRule | String? |
| reminderEnabled | Boolean |
| color | String |
| icon | String |
| note | String? |
| archived | Boolean |
| createdAt | Long |
| updatedAt | Long |

---

## Index

title

categoryId

archived

---

## Rules

Target dapat berubah.

Perubahan target tidak mengubah histori.

Progress dihitung dari StudySession.

---

# StudySession

Mencatat seluruh sesi belajar.

---

## Table

| Field | Type |
|--------|------|
| id | String (UUID) |
| habitId | String |
| startTime | Long |
| endTime | Long |
| durationMinute | Int |
| source | String |
| note | String? |
| createdAt | Long |

---

## Source

```
TIMER

MANUAL

POMODORO
```

---

## Index

habitId

startTime

createdAt

---

## Rules

Satu sesi menghasilkan satu row.

Session tidak digabung.

Menghapus session akan mempengaruhi statistik.

---

# Reminder

Digunakan bersama oleh Schedule, Task, dan Habit.

---

## Table

| Field | Type |
|--------|------|
| id | String (UUID) |
| ownerType | String |
| ownerId | String |
| triggerTime | Long |
| snoozeMinute | Int |
| enabled | Boolean |
| createdAt | Long |

---

## OwnerType

```
TASK

SCHEDULE

HABIT
```

---

## Rules

Satu owner dapat memiliki banyak Reminder.

Reminder tidak memiliki Foreign Key karena owner dapat berasal dari beberapa tabel.

Validasi owner dilakukan pada Repository.

---

# Checklist

Digunakan oleh Task.

---

## Table

| Field | Type |
|--------|------|
| id | String (UUID) |
| taskId | String |
| title | String |
| checked | Boolean |
| orderIndex | Int |

---

## Index

taskId

orderIndex

---

## Rules

Checklist otomatis ikut terhapus ketika Task dihapus.

---

# Attachment

Digunakan oleh Task dan Schedule.

---

## Table

| Field | Type |
|--------|------|
| id | String (UUID) |
| ownerType | String |
| ownerId | String |
| type | AttachmentType |
| fileName | String |
| filePath | String |
| createdAt | Long |

---

## OwnerType

```
TASK

SCHEDULE
```

---

## Rules

Attachment menggunakan penyimpanan lokal.

Yang disimpan di database hanya path file.

---

# Relationship

Category

```
1

↓

Task
```

Category

```
1

↓

Schedule
```

Category

```
1

↓

Habit
```

Habit

```
1

↓

StudySession
```

Task

```
1

↓

Checklist
```

Task

```
1

↓

Reminder
```

Task

```
1

↓

Attachment
```

Schedule

```
1

↓

Reminder
```

Schedule

```
1

↓

Attachment
```

Habit

```
1

↓

Reminder
```

---

# Cascade Rule

Task dihapus

↓

Checklist ikut terhapus.

---

Task dihapus

↓

Attachment ikut terhapus.

---

Schedule dihapus

↓

Attachment ikut terhapus.

---

Habit dihapus

↓

StudySession ikut terhapus.

---

# Validasi

Schedule

Jam selesai wajib lebih besar.

---

Task

Judul wajib.

---

Habit

Target harus lebih dari nol.

---

StudySession

Durasi harus lebih dari nol.

---

Reminder

Owner harus ada.

---

Attachment

File harus tersedia.

---

# Catatan Implementasi

Reminder dan Attachment dibuat sebagai tabel generik agar tidak terjadi duplikasi struktur.

Semua statistik belajar dihitung dari tabel StudySession.

Habit tidak menyimpan progress harian.

Progress selalu dihitung secara dinamis.

# DATABASE_SCHEMA.md (Part 3)

# TaskReminder

Digunakan oleh Task.

---

## Table

| Field | Type |
|------|------|
| id | String (UUID) |
| taskId | String |
| triggerTime | Long |
| snoozeMinute | Int |
| enabled | Boolean |

---

## Foreign Key

taskId

↓

Task.id

---

## Cascade

ON DELETE CASCADE

---

# ScheduleReminder

Digunakan oleh Schedule.

---

## Table

| Field | Type |
|------|------|
| id | String (UUID) |
| scheduleId | String |
| triggerTime | Long |
| snoozeMinute | Int |
| enabled | Boolean |

---

## Foreign Key

scheduleId

↓

Schedule.id

---

## Cascade

ON DELETE CASCADE

---

# HabitReminder

Digunakan oleh Habit.

---

## Table

| Field | Type |
|------|------|
| id | String (UUID) |
| habitId | String |
| triggerTime | Long |
| snoozeMinute | Int |
| enabled | Boolean |

---

## Foreign Key

habitId

↓

Habit.id

---

## Cascade

ON DELETE CASCADE

---

# TaskAttachment

Digunakan oleh Task.

---

## Table

| Field | Type |
|------|------|
| id | String (UUID) |
| taskId | String |
| type | AttachmentType |
| fileName | String |
| filePath | String |
| createdAt | Long |

---

## Foreign Key

taskId

↓

Task.id

---

## Cascade

ON DELETE CASCADE

---

# ScheduleAttachment

Digunakan oleh Schedule.

---

## Table

| Field | Type |
|------|------|
| id | String (UUID) |
| scheduleId | String |
| type | AttachmentType |
| fileName | String |
| filePath | String |
| createdAt | Long |

---

## Foreign Key

scheduleId

↓

Schedule.id

---

## Cascade

ON DELETE CASCADE

---

# DAO

Minimal DAO.

CategoryDao

TaskDao

ScheduleDao

HabitDao

StudySessionDao

AchievementDao

SettingDao

ReminderDao

AttachmentDao

---

# Query Penting

## Dashboard

Ambil.

* Aktivitas hari ini.
* Deadline terdekat.
* Habit hari ini.
* Target belajar hari ini.

---

## Study

Hitung.

Total belajar hari ini.

Total belajar minggu ini.

Total belajar bulan ini.

---

## Habit

Hitung.

Progress hari ini.

Status selesai.

Streak.

Streak tertinggi.

---

## Task

Ambil.

Task aktif.

Task completed.

Task overdue.

---

## Schedule

Ambil.

Schedule hari ini.

Schedule minggu ini.

Schedule berikutnya.

---

# Statistik

Jangan simpan statistik.

Hitung dari.

StudySession.

Task.

Schedule.

Habit.

---

# Heatmap

Hitung dari.

StudySession.

Kelompokkan berdasarkan tanggal.

---

# Backup

Backup mencakup seluruh tabel.

Tidak termasuk.

Cache.

Temporary State.

---

# Restore

Restore mengganti seluruh isi database.

Gunakan transaksi.

Jika gagal.

Rollback seluruh perubahan.

---

# Transaction

Gunakan transaksi pada.

Restore.

Delete Habit.

Delete Task.

Delete Schedule.

Import Backup.

---

# Migration

Semua perubahan schema wajib memiliki Migration.

Jangan menggunakan fallbackToDestructiveMigration pada production.

---

# Index

Tambahkan Index pada.

Task.deadline

Task.status

Task.categoryId

Schedule.startTime

Schedule.endTime

Schedule.status

Schedule.categoryId

Habit.categoryId

StudySession.habitId

StudySession.startTime

StudySession.createdAt

Checklist.taskId

TaskReminder.taskId

ScheduleReminder.scheduleId

HabitReminder.habitId

TaskAttachment.taskId

ScheduleAttachment.scheduleId

---

# Data Flow

Schedule

↓

Reminder

↓

Notification

↓

User Action

↓

Status Update

---

Habit

↓

Timer

↓

Study Session

↓

Progress

↓

Streak

↓

Statistics

↓

Heatmap

---

Task

↓

Checklist

↓

Completed

↓

Statistics

---

# Data Yang Tidak Disimpan

Jangan simpan.

Progress Habit.

Progress Harian.

Total Belajar.

Statistik Mingguan.

Statistik Bulanan.

Heatmap.

Streak.

Streak Tertinggi.

Semua dihitung dari data utama.

---

# Soft Delete

Tidak digunakan.

Delete berarti menghapus permanen.

---

# File Storage

Attachment disimpan di penyimpanan lokal.

Database hanya menyimpan path file.

---

# Backup Format

Gunakan satu file.

Contoh.

mother-backup-YYYY-MM-DD.zip

Isi.

Database.

Attachment.

Metadata.

---

# Konsistensi Data

Setiap Foreign Key wajib valid.

Tidak boleh ada orphan data.

Tidak boleh ada duplicate UUID.

Semua operasi database harus menjaga konsistensi.

---

# Definition of Done

Database dianggap selesai apabila.

* Semua Entity memiliki DAO.
* Semua relasi memiliki Foreign Key.
* Semua Index sudah dibuat.
* Semua Migration tersedia.
* Tidak ada duplikasi data.
* Backup berhasil.
* Restore berhasil.
* Query Dashboard cepat.
* Query Statistics benar.
* Seluruh requirement pada PRD dapat dipenuhi.
