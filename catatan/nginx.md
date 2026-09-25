# Nginx dalam Ekosistem Backend dan Docker

## 1. Apa itu Nginx?

**Nginx** (dibaca *"Engine-X"*) adalah web server berkinerja sangat tinggi yang juga berfungsi sebagai **Reverse Proxy**, **Load Balancer**, **HTTP Cache**, dan **API Gateway**.

Dalam arsitektur backend modern, aplikasi Java/Spring Boot **hampir tidak pernah** dipublikasikan secara langsung ke internet publik. Sebagai gantinya, Nginx ditempatkan di paling depan sebagai pintu gerbang utama (*front-line / gateway*).

---

## 🍼 Nginx dalam "Bahasa Bayi" (Analogi Satpam Restoran)

Bayangkan aplikasi backend kamu adalah sebuah **Restoran Fast Food**:

* **Koki Dapur (Spring Boot / Port 8081):**
  * Koki tugasnya fokus memasak logika & data (CRUD).
  * Koki berada di ruang dalam (dapur). Dia **tidak ramah** kalau diajak ngobrol langsung oleh pembeli dari luar, dan dia tidak mengerti cara menjaga keamanan pintu depan.

* **Satpam & Pelayan Depan Pintu (Nginx / Port 80):**
  * Nginx adalah Satpam ganteng yang berdiri tegak di pintu depan restoran.
  * **Siapa saja yang datang dari luar internet (HP/Browser) HANYA BISA bicara dengan Nginx di pintu depan (Port 80).**

### Apa Saja Tugas Satpam Nginx Ini?

1. **Reverse Proxy (Perantara Pesanan):**
   * Pembeli: *"Mas Nginx, saya minta daftar user dong!"*
   * Nginx: *"Oke tunggu sebentar."* *(Nginx lari ke dapur Spring Boot di port 8081, ambil datanya, lalu kasih balik ke Pembeli)*.
   * **Hasilnya:** Pembeli tidak pernah tahu dapur Koki ada di mana atau pakai port berapa!

2. **Load Balancer (Bagi-bagi Tugas):**
   * Kalau restoran sangat ramai, kita tambah 3 Koki (3 Container Spring Boot: `app1`, `app2`, `app3`).
   * Nginx bakal bagi antrean: Pesanan ke-1 dikasih ke Koki 1, pesanan ke-2 ke Koki 2, pesanan ke-3 ke Koki 3. Supaya tidak ada Koki yang pingsan karena keberatan beban.

3. **Keamanan (Security & SSL):**
   * Kalau ada penjahat mau merusak restoran, Nginx mencegatnya di pintu depan.
   * Nginx juga yang memasang stempel aman HTTPS (SSL certificate) sebelum surat pesanan masuk ke Dapur.

---

## 2. Mengapa Perlu Nginx di Depan Spring Boot?

Secara default, Spring Boot memiliki embedded server bernama **Tomcat** (port default `8080` atau `8081`). 

### Perbandingan Arsitektur:

#### Tanpa Nginx (Kurang Aman & Tidak Standar):
```text
Browser / Mobile App (Internet)
       │
       │ (Port 8081 terbuka langsung ke luar)
       ▼
Spring Boot (Embedded Tomcat)
       │
       ▼
PostgreSQL & Redis
```

#### Dengan Nginx (Standar Industri / Production Ready):
```text
Browser / Mobile App (Internet)
       │
       │ (Hanya Port HTTP 80 / HTTPS 443 yang dibuka)
       ▼
  [ Nginx Proxy ]  <--- (Menangani Security, SSL/TLS, Caching)
       │
       │ (Jaringan Internal Docker / Private Network)
       ▼
Spring Boot Container (Port 8081)
       │
       ▼
PostgreSQL & Redis
```

### Keunggulan Utama Nginx:
| Fitur | Penjelasan |
|---|---|
| **Reverse Proxy** | Sembunyikan port asli aplikasi (`8081`). User cukup mengakses port standar HTTP (`80`) atau HTTPS (`443`). |
| **SSL/TLS Termination** | Nginx menangani sertifikat HTTPS (SSL). Spring Boot tidak perlu sibuk mengurus enkripsi SSL. |
| **Load Balancing** | Membagi beban request jika kita menjalankan multiple instance/container Spring Boot. |
| **Security & Hardening** | Menepis serangan DDoS, membatasi rate limit request, dan menyembunyikan identitas server internal. |
| **Static File Serving** | Jika ada file static (seperti foto profil, gambar, dokumen HTML), Nginx menyajikannya jauh lebih cepat daripada Spring Boot. |

---

## 3. Konsep Penting: Forward Proxy vs Reverse Proxy

Untuk memahami Nginx, penting membedakan **Forward Proxy** dan **Reverse Proxy**:

```text
1. Forward Proxy (Melindungi Client):
   Client (Anda) ──► [ Forward Proxy ] ──► Internet / Server Lain
   *Tujuan: Menyembunyikan identitas Client (contoh: VPN).

2. Reverse Proxy (Melindungi Server):
   Client (Public) ──► [ Reverse Proxy / Nginx ] ──► Backend Server (Spring Boot)
   *Tujuan: Menyembunyikan identitas & struktur internal Server.
```

---

## 4. Anatomi File Konfigurasi Nginx (`nginx.conf`)

Struktur file konfigurasi Nginx terbagi menjadi blok-blok hirarkis (*directives*):

```nginx
# 1. Main Context: Konfigurasi global worker process
worker_processes auto;

events {
    worker_connections 1024; # Jumlah koneksi simultan per worker
}

http {
    include       mime.types;
    default_type  application/octet-stream;

    # 2. Server Block: Deklarasi virtual host / domain / port listener
    server {
        listen 80; # Nginx mendengarkan port 80 (HTTP)
        server_name localhost;

        # 3. Location Block: Aturan penanganan URL path tertentu
        location / {
            proxy_pass http://app:8081; # Meneruskan ke container Spring Boot
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
}
```

### Penjelasan Directives Penting:

- **`listen 80;`**: Menentukan port tempat Nginx menerima request dari client luar.
- **`proxy_pass http://app:8081;`**: Inti dari Reverse Proxy. Perintah ini menginstruksikan Nginx untuk **meneruskan request** ke service `app` (nama container Spring Boot di Docker) pada port `8081`.
- **`proxy_set_header Host $host;`**: Memastikan header `Host` asli dari client tetap diteruskan ke Spring Boot.
- **`proxy_set_header X-Real-IP $remote_addr;`**: Agar Spring Boot bisa mengetahui IP Address asli dari pembeli/user, bukan IP dari Nginx.
- **`proxy_set_header X-Forwarded-For`**: Menyimpan jejak IP jika request melewati beberapa proxy.

---

## 5. Implementasi Nginx + Spring Boot di Docker Compose

### Step 1: Buat File `nginx.conf` di Folder Proyek

Buat file bernama `nginx.conf` di dalam proyek Spring Boot kamu:

```nginx
events {
    worker_connections 1024;
}

http {
    # Meneruskan request ke container Spring Boot
    upstream spring_backend {
        server app:8081; # 'app' adalah nama service di docker-compose.yml
    }

    server {
        listen 80;
        server_name localhost;

        location / {
            proxy_pass http://spring_backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
}
```

---

### Step 2: Tambahkan Service Nginx di `docker-compose.yml`

Buka file `docker-compose.yml` dan tambahkan service `nginx`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: postgres_db
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: belajar_db
    networks:
      - app_network

  redis:
    image: redis:7-alpine
    container_name: redis_cache
    networks:
      - app_network

  app:
    build: .
    container_name: spring_app
    # Port 8081 TIDAK PERLU diekspos ke luar laptop lagi,
    # karena cukup diakses oleh Nginx dari dalam jaringan internal Docker!
    environment:
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=belajar_db
      - DB_USER=postgres
      - DB_PASS=postgres
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - app_network

  nginx:
    image: nginx:alpine
    container_name: nginx_proxy
    ports:
      - "80:80" # Port 80 laptop dipetakan ke port 80 Nginx
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro # Mounting file konfigurasi (read-only)
    depends_on:
      - app # Nginx jalan setelah container app siap
    networks:
      - app_network

networks:
  app_network:
    driver: bridge
```

---

## 6. Konsep Load Balancing dengan Nginx

Jika aplikasi kamu mendapat traffic yang sangat besar, kamu bisa menjalankan **banyak container Spring Boot sekaligus** (*Scaling*).

Di `nginx.conf`, kita cukup mendaftarkan multiple instance pada bagian `upstream`:

```nginx
upstream spring_backend {
    # Nginx akan membagi traffic secara bergiliran (Round Robin)
    server app1:8081;
    server app2:8081;
    server app3:8081;
}
```

Atau dengan perintah Docker Compose untuk membesar-besarkan container:
```bash
docker compose up -d --scale app=3
```
Nginx secara otomatis akan merutekan beban secara seimbang ke 3 container Spring Boot tersebut!

---

## 7. Perintah-Perintah Penting Nginx di Docker

| Perintah | Fungsi |
|---|---|
| `docker exec -it nginx_proxy nginx -t` | Menguji apakah sintaks file `nginx.conf` sudah benar / ada error syntax. |
| `docker exec -it nginx_proxy nginx -s reload` | Me-reload konfigurasi baru tanpa perlu mematikan/restart container Nginx. |
| `docker logs -f nginx_proxy` | Melihat log akses (access log) dan error log realtime dari Nginx. |

---

## 8. Ringkasan Level 7 (Nginx)

```text
1. Client hanya memanggil http://localhost/ (Port 80 standar).
2. Nginx menerima request -> meneruskannya ke http://app:8081 di jaringan internal Docker.
3. Spring Boot memproses logika -> query DB Postgres / Redis -> mengembalikan response ke Nginx.
4. Nginx menyampaikan response akhir ke Client.
```
