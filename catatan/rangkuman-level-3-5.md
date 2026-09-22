# Rangkuman Belajar Spring Boot: Level 3, 4, dan 5

Dokumen ini adalah rangkuman dari tahapan belajar pembuatan REST API, integrasi Database PostgreSQL, hingga pembuatan Project CRUD dengan struktur arsitektur yang standar di ekosistem Spring Boot.

## 3. REST API
Pada tahap ini, fokus utamanya adalah membuat endpoint API (Application Programming Interface) agar aplikasi backend dapat berkomunikasi dengan aplikasi lain (seperti frontend web atau mobile app) menggunakan protokol HTTP.
- **Konsep Utama**: Memahami metode standar HTTP seperti `GET` (mengambil data), `POST` (menyimpan data baru), `PUT` (mengubah data yang ada), dan `DELETE` (menghapus data).
- **Menerima Data**: Memahami cara aplikasi menangkap data dari *client* melalui URL maupun *body* menggunakan anotasi seperti `@PathVariable`, `@RequestParam`, dan `@RequestBody`.
- **Mengirim Balasan**: Mengembalikan data dalam format standar (seperti JSON) dan mengirimkan HTTP Status Code yang tepat (misal: 200 OK untuk sukses, 201 Created untuk data berhasil dibuat, atau 404 Not Found jika data tidak ada).

## 4. PostgreSQL + Spring Data JPA
Setelah bisa membuat REST API, langkah krusial berikutnya adalah menyimpan data ke dalam *database* sungguhan agar data tidak hilang (persistent) ketika server dimatikan.
- **PostgreSQL**: Sistem manajemen database relasional (RDBMS) yang digunakan untuk menyimpan data dalam bentuk tabel.
- **Spring Data JPA & Hibernate**: Alat bantu di Spring Boot berkonsep ORM (*Object Relational Mapping*). Ini memungkinkan developer berinteraksi dengan database cukup melalui kode Java, tanpa harus banyak menulis sintaks SQL murni secara manual.
- **Entity**: Membuat representasi tabel database ke dalam bentuk Class Java (menggunakan anotasi `@Entity`).
- **Repository**: Antarmuka (*interface*) Spring Data JPA yang secara otomatis menyediakan fungsi CRUD bawaan (seperti `save()`, `findAll()`, `findById()`, dan `deleteById()`).

## 5. Buat 1 Project CRUD & Struktur Arsitektur
Tahap ini adalah menggabungkan ilmu REST API dan Database ke dalam satu proyek utuh (Create, Read, Update, Delete). Di tahap ini sangat disarankan untuk mulai mengorganisasi kode ke dalam folder (package) berdasarkan tanggung jawabnya masing-masing.

Berikut adalah penjelasan fungsi struktur folder standar yang digunakan dalam project:

```text
src/main/java
└── com.example.demo
    ├── controller
    ├── service
    ├── repository
    ├── entity
    └── dto
```

### Penjelasan dan Kegunaan Setiap Folder:

1. **`controller`**
   - **Tugas**: Sebagai "resepsionis" atau pintu masuk aplikasi. Menerima *request* HTTP (GET, POST, dll) dari luar dan mengembalikan *response*.
   - **Aturan**: *Controller* harus dibuat seringkas mungkin dan **tidak boleh** berisi aturan/logika bisnis yang rumit. Tugas utamanya hanya meneruskan data ke `service`, lalu membungkus hasilnya (misal menggunakan `ResponseEntity`) untuk dikembalikan ke *user*.

2. **`service`**
   - **Tugas**: Berisi **Logika Bisnis (*Business Logic*)** atau aturan main aplikasi.
   - **Aturan**: Di sinilah "otak" aplikasi berada. Misalnya: proses verifikasi password, pengecekan apakah email sudah terdaftar, kalkulasi diskon harga, dsb. *Service* dipanggil oleh *Controller*, dan *Service* jugalah yang berhak memanggil *Repository*.

3. **`repository`**
   - **Tugas**: Lapisan yang berkomunikasi langsung dengan Database.
   - **Aturan**: Berisi antarmuka (*interface*) yang mengekstensi `JpaRepository`. Tempat disimpannya fungsi untuk melakukan *query* ke database. Hanya boleh dipanggil oleh lapisan `service`.

4. **`entity` (atau `model`)**
   - **Tugas**: Class Java yang merupakan "cerminan" langsung dari struktur tabel yang ada di dalam database.
   - **Aturan**: Setiap variabel/properti di class ini mewakili satu kolom di tabel database. Class ini sebaiknya tidak digunakan sebagai nilai kembalian (*response*) secara langsung ke klien untuk alasan keamanan dan fleksibilitas pengembangan.

5. **`dto` (Data Transfer Object)**
   - **Tugas**: Objek khusus yang bertugas membungkus atau "membawa data" antara *client* (pengguna) dan *server* (aplikasi kita).
   - **Aturan**: 
     - Sering dipisah menjadi `RequestDTO` (struktur data yang kita inginkan saat *user* mengirim form/data) dan `ResponseDTO` (struktur data yang siap ditampilkan ke *user*).
     - **Keuntungan**: DTO menyembunyikan informasi sensitif yang ada di `entity` (misalnya menyembunyikan field *password*). Selain itu, DTO mencegah aplikasi error jika sewaktu-waktu struktur tabel database kita berubah, karena format API (*contract*) yang tampil di luar akan tetap dijaga konsistensinya oleh DTO.
