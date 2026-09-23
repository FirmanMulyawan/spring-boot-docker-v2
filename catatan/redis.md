# Redis dalam Spring Boot

## Apa itu Redis?

Redis adalah database yang menyimpan data di **memory (RAM)**, bukan di disk seperti PostgreSQL.

```text
PostgreSQL → data disimpan di disk (permanen, tapi lebih lambat)
Redis      → data disimpan di RAM  (tidak permanen, tapi sangat cepat)
```

Karena di RAM, Redis **jauh lebih cepat** dari PostgreSQL. Cocok untuk data yang sering dibaca berulang kali.

---

## Kenapa Perlu Redis?

Bayangkan kamu punya endpoint `GET /users` yang dipanggil ribuan kali per menit.  
Setiap kali dipanggil, Spring Boot query ke PostgreSQL → lambat dan membebani database.

Dengan Redis (sebagai **cache**):

```text
Request pertama:
Client → Spring Boot → PostgreSQL (ambil data) → simpan ke Redis → kirim ke Client

Request kedua dan seterusnya:
Client → Spring Boot → Redis (ambil dari cache, cepat ⚡) → kirim ke Client
```

PostgreSQL hanya ditanya **sekali**. Sisanya ambil dari Redis.

---

## Kegunaan Redis

| Kegunaan | Penjelasan |
|---|---|
| **Cache** | Simpan hasil query agar tidak tanya DB terus-menerus |
| **Session** | Simpan data sesi login user |
| **OTP** | Simpan kode OTP yang expired dalam X menit |
| **Rate Limiting** | Batasi jumlah request per user per menit |
| **Queue** | Antrian task sederhana |

---

## Redis di Docker Compose

Untuk menjalankan Redis bersama Spring Boot dan PostgreSQL:

```yaml
redis:
  image: redis:7-alpine
  container_name: redis_cache
  ports:
    - "6379:6379"
  networks:
    - app_network
```

- **Port default Redis**: `6379`
- Kiri `6379` = port di laptop (bisa diakses dari luar)
- Kanan `6379` = port di dalam Docker

---

## Dependency di pom.xml

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

---

## Konfigurasi di application.yaml

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: 6379
```

- `${REDIS_HOST:localhost}` artinya: ambil dari environment variable `REDIS_HOST`, kalau tidak ada pakai default `localhost`
- Di Docker, `REDIS_HOST` diisi nama service yaitu `redis`

---

## Aktifkan Caching di Spring Boot

Tambahkan anotasi `@EnableCaching` di class utama aplikasi:

```java
@SpringBootApplication
@EnableCaching  // ← tambahkan ini
public class BelajarSpringDockerV2Application {
    public static void main(String[] args) {
        SpringApplication.run(BelajarSpringDockerV2Application.class, args);
    }
}
```

---

## Anotasi Cache di Service

### `@Cacheable` — Simpan hasil ke cache

```java
@Cacheable("users")
public List<UserResponseDTO> getAllUsers() {
    // Method ini hanya jalan saat data BELUM ada di cache
    // Jika sudah ada di cache, langsung ambil dari Redis
    return userRepository.findAll().stream()
            .map(UserResponseDTO::fromEntity)
            .collect(Collectors.toList());
}
```

### `@CacheEvict` — Hapus cache

```java
@CacheEvict(value = "users", allEntries = true)
public UserResponseDTO createUser(UserRequestDTO requestDTO) {
    // Setelah user baru dibuat, cache "users" dihapus
    // agar data yang ditampilkan selalu fresh
}
```

Kenapa di-evict? Karena kalau user baru ditambahkan, cache lama masih berisi data lama.  
Dengan evict, saat `GET /users` dipanggil lagi, Spring Boot akan ambil ulang dari PostgreSQL.

---

## Alur Kerja Lengkap

```text
GET /users (pertama kali):
  Controller → Service @Cacheable → cek Redis
  → Redis MISS (kosong) → query PostgreSQL
  → simpan hasil ke Redis dengan key "users"
  → return ke client

GET /users (kedua kali dan seterusnya):
  Controller → Service @Cacheable → cek Redis
  → Redis HIT → langsung return data dari Redis ⚡
  → TIDAK query PostgreSQL

POST /users:
  Controller → Service @CacheEvict → hapus cache "users"
  → simpan user baru ke PostgreSQL
  → return user baru ke client
```

---

## Environment Variable di Docker Compose

Tambahkan `REDIS_HOST` ke bagian `environment` service `app`:

```yaml
app:
  environment:
    - DB_HOST=postgres
    - DB_PORT=5432
    - DB_NAME=belajar_db
    - DB_USER=postgres
    - DB_PASS=postgres
    - REDIS_HOST=redis   # ← tambahkan ini
```

Nilai `redis` merujuk ke nama service Redis di Docker Compose.

---

## Cek Redis Berjalan

Setelah `docker compose up`, kamu bisa cek apakah Redis berjalan:

```bash
# Masuk ke dalam container Redis
docker exec -it redis_cache redis-cli

# Cek koneksi
ping
# Output: PONG

# Lihat semua key yang tersimpan
keys *

# Lihat value dari sebuah key
get namaKey
```

---

## Ringkasan

```text
Redis = database super cepat di RAM
Cache = teknik menyimpan hasil query agar tidak tanya DB terus
@Cacheable = simpan hasil method ke Redis
@CacheEvict = hapus cache agar data selalu fresh
```
