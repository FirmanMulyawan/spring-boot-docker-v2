# Docker Compose

Docker Compose adalah alat untuk **mendefinisikan dan menjalankan banyak container Docker sekaligus** hanya dengan satu perintah. Semua konfigurasi ditulis dalam satu file bernama `docker-compose.yml`.

---

## Masalah yang Diselesaikan Docker Compose

Bayangkan aplikasimu butuh dua komponen:
- **Spring Boot** (aplikasi)
- **PostgreSQL** (database)

Tanpa Docker Compose, kamu harus menjalankan container satu-satu secara manual:

```bash
# Langkah 1: jalankan PostgreSQL dulu
docker run -d \
  --name postgres_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=belajar_db \
  -p 5433:5432 \
  postgres:15-alpine

# Langkah 2: buat network
docker network create app_network

# Langkah 3: hubungkan postgres ke network
docker network connect app_network postgres_db

# Langkah 4: baru jalankan Spring Boot
docker run -d \
  --name spring_app \
  -e DB_HOST=postgres \
  -p 8081:8081 \
  --network app_network \
  my-spring-image
```

😩 Sangat panjang dan rawan salah.

**Dengan Docker Compose**, semua itu cukup dengan:

```bash
docker compose up
```

---

## Anatomi File `docker-compose.yml`

Berikut adalah file Docker Compose dari project kamu sendiri beserta penjelasan tiap barisnya:

```yaml
version: '3.8'           # Versi syntax Docker Compose yang digunakan
```

> ⚠️ Pada Docker Compose v2+ (yang modern), baris `version` sudah tidak diperlukan lagi. Tapi tidak masalah kalau tetap ditulis.

---

### `services` — Daftar Container

`services` adalah bagian utama. Di sinilah kamu mendefinisikan setiap container yang ingin dijalankan.

```yaml
services:
  postgres:              # Nama service (bebas, ini jadi "hostname" antar container)
  app:                   # Nama service kedua (Spring Boot)
```

---

### Service `postgres` — Container Database

```yaml
  postgres:
    image: postgres:15-alpine
```
- `image`: Docker akan mengunduh image `postgres` versi `15-alpine` dari Docker Hub.
- `alpine` = versi Linux yang sangat ringan, cocok untuk production.

```yaml
    container_name: postgres_db
```
- `container_name`: Nama container yang akan muncul saat kamu ketik `docker ps`.

```yaml
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: belajar_db
```
- `environment`: Variabel lingkungan yang dikirim ke dalam container.
- Image PostgreSQL secara otomatis membaca variabel ini untuk membuat user, password, dan database pertama kali.

```yaml
    ports:
      - "5433:5432"
```
- `ports`: Format → `"HOST:CONTAINER"`
- Artinya: port **5433 di laptopmu** → diteruskan ke port **5432 di dalam container** (port default PostgreSQL).
- Kamu akses dari laptop pakai port **5433**, tapi di dalam jaringan Docker tetap **5432**.

```yaml
    volumes:
      - postgres_data:/var/lib/postgresql/data
```
- `volumes`: Menyimpan data database ke **volume Docker** agar tidak hilang saat container dihapus/restart.
- `postgres_data` = nama volume (dideklarasikan di bagian bawah file).
- `/var/lib/postgresql/data` = lokasi di dalam container tempat PostgreSQL menyimpan datanya.

```yaml
    networks:
      - app_network
```
- Mendaftarkan container ini ke dalam jaringan bernama `app_network`.

---

### Service `app` — Container Spring Boot

```yaml
  app:
    build: .
```
- `build: .` → Docker akan membangun image Spring Boot dari **Dockerfile yang ada di folder saat ini** (titik = current directory).
- Berbeda dengan `image:` yang mengunduh dari Docker Hub, `build:` membuat image sendiri.

```yaml
    container_name: spring_app
    ports:
      - "8081:8081"
```
- Port 8081 di laptop → port 8081 di dalam container.

```yaml
    environment:
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=belajar_db
      - DB_USER=postgres
      - DB_PASS=postgres
```
- Nilai `DB_HOST=postgres` → ini **nama service** PostgreSQL di atas, bukan `localhost`!
- Di dalam jaringan Docker, container bisa saling menemukan satu sama lain **menggunakan nama service** sebagai hostname.
- Itulah mengapa di `application.yaml` kamu ada `${DB_HOST:localhost}` — nilai defaultnya `localhost` untuk development lokal, dan akan diganti `postgres` saat jalan di Docker.

```yaml
    depends_on:
      - postgres
```
- `depends_on`: Memastikan container `postgres` **dijalankan lebih dulu** sebelum container `app` dimulai.

---

### `networks` — Jaringan Antar Container

```yaml
networks:
  app_network:
    driver: bridge
```
- `bridge` adalah tipe network default Docker — container dalam satu network yang sama bisa saling berkomunikasi.
- Tanpa network yang sama, container `app` tidak bisa menemukan container `postgres`.

---

### `volumes` — Deklarasi Volume

```yaml
volumes:
  postgres_data:
```
- Ini adalah **deklarasi** bahwa ada volume bernama `postgres_data` yang dikelola oleh Docker.
- Data di volume ini akan tetap ada meskipun container dihapus dengan `docker compose down`.

---

## Perintah-Perintah Penting Docker Compose

### Menjalankan

```bash
# Menjalankan semua service (di foreground, log terlihat)
docker compose up

# Menjalankan di background (detached mode)
docker compose up -d

# Build ulang image sebelum menjalankan (wajib setelah ubah kode Java)
docker compose up --build

# Build ulang dan jalankan di background
docker compose up --build -d
```

### Menghentikan

```bash
# Menghentikan semua container (tapi tidak menghapusnya)
docker compose stop

# Menghentikan DAN menghapus container + network
docker compose down

# Menghentikan, hapus container + network + VOLUME (data database ikut terhapus!)
docker compose down -v
```

> ⚠️ Hati-hati dengan `down -v`! Data PostgreSQL kamu akan ikut terhapus.

### Monitoring

```bash
# Melihat status semua container
docker compose ps

# Melihat log semua service
docker compose logs

# Melihat log satu service saja (misal hanya Spring Boot)
docker compose logs app

# Melihat log secara realtime (follow mode)
docker compose logs -f app
```

### Lainnya

```bash
# Masuk ke dalam container (seperti SSH)
docker compose exec postgres bash
docker compose exec app sh

# Restart satu service saja
docker compose restart app
```

---

## Alur Kerja: Dari Kode ke Docker Compose

Ini adalah urutan langkah yang harus dilakukan setiap kali kamu mengubah kode Java:

```
1. Ubah kode Java
        ↓
2. Build JAR dulu
   ./mvnw package -DskipTests
        ↓
3. Jalankan Docker Compose
   docker compose up --build
        ↓
4. Test endpoint
   curl http://localhost:8081/users
```

> **Kenapa perlu `--build`?** Karena Dockerfile kamu menyalin file `.jar` yang sudah jadi ke dalam image. Kalau kodenya berubah tapi JAR-nya belum di-build ulang, container tetap pakai kode lama.

---

## Ilustrasi Jaringan Docker

Ini gambaran apa yang terjadi saat `docker compose up` dijalankan:

```
Laptop kamu
│
├── port 8081 ─────────────────────────────┐
│                                          ↓
│                              ┌─── Docker Network: app_network ───┐
│                              │                                   │
│                              │  Container: spring_app            │
│                              │  (port 8081)                      │
│                              │       │                           │
│                              │       │ koneksi ke "postgres:5432"│
│                              │       ↓                           │
│                              │  Container: postgres_db           │
│                              │  (port 5432 dalam network)        │
│                              └───────────────────────────────────┘
│
└── port 5433 ────────────────→ Container postgres_db (untuk akses dari laptop)
```

---

## Perbedaan Penting: `localhost` vs Nama Service

Ini adalah konsep yang **sering membingungkan pemula**:

| Konteks | Yang dipakai | Penjelasan |
|---|---|---|
| Aplikasi jalan **di laptop** (development) | `localhost:5433` | PostgreSQL di laptop / port yang di-expose |
| Aplikasi jalan **di dalam Docker** | `postgres:5432` | Container berkomunikasi via nama service di jaringan Docker internal |

Itulah kenapa `application.yaml` kamu ditulis seperti ini — **sangat cerdas!**

```yaml
url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5433}/${DB_NAME:belajar_db}
```

- Saat development (run biasa): `DB_HOST` tidak di-set → pakai default `localhost`, port `5433`
- Saat di Docker Compose: `DB_HOST=postgres` di-inject → pakai `postgres`, port `5432`
