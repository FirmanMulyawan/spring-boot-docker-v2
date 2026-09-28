# CI/CD dengan GitHub Actions

## 🍼 Bahasa Bayi: Apa itu CI/CD?

Bayangkan kamu punya toko roti online.

Setiap kali kamu bikin resep baru, kamu harus:

1. Cicipin dulu (test)
2. Panggang dalam jumlah besar (build)
3. Kirim ke toko (deploy)

**Tanpa CI/CD** — semua dilakukan manual, satu per satu, bisa lupa, bisa salah.

**Dengan CI/CD** — kamu cukup upload resep ke GitHub, sisanya **berjalan otomatis**:

```text
Kamu push kode ke GitHub
        ↓
GitHub Actions otomatis jalan:
  ① Test → apakah kode aman? (seperti cicip roti)
  ② Build → compile dan buat Docker Image (seperti panggang)
  ③ Push → upload image ke Docker Hub (seperti kirim ke gudang)
  ④ Deploy → jalankan di server (seperti pajang di toko) ← opsional
```

---

## 🔑 Istilah Penting

| Istilah | Artinya |
|---|---|
| **CI** (Continuous Integration) | Setiap push kode → otomatis di-test dan di-build |
| **CD** (Continuous Delivery) | Setelah build sukses → otomatis siap di-deploy |
| **Pipeline** | Rangkaian langkah otomatis (test → build → deploy) |
| **Workflow** | File konfigurasi pipeline di GitHub Actions (format YAML) |
| **Job** | Satu kelompok pekerjaan dalam workflow (misal: job "test", job "build") |
| **Step** | Satu langkah dalam sebuah job (misal: step "jalankan mvn test") |
| **Runner** | Komputer virtual yang menjalankan pipeline (disediakan GitHub gratis) |
| **Trigger** | Kejadian yang memicu pipeline berjalan (misal: saat push ke branch main) |

---

## 🗂️ Letak File Workflow

GitHub Actions membaca konfigurasi pipeline dari folder khusus:

```text
project-kamu/
├── .github/
│   └── workflows/
│       └── ci.yml   ← File pipeline kita
├── src/
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

> **Aturan:** File harus ada di `.github/workflows/` dan berekstensi `.yml`

---

## 📋 Anatomi File Workflow

```yaml
name: Nama Pipeline             # Nama yang tampil di GitHub

on:                             # TRIGGER: kapan pipeline ini jalan?
  push:
    branches: [ main ]          # Jalan saat ada push ke branch "main"

jobs:                           # Daftar pekerjaan
  nama-job:                     # Nama job (bebas)
    runs-on: ubuntu-latest      # Pakai komputer virtual Ubuntu

    steps:                      # Daftar langkah dalam job ini
      - name: Nama Langkah      # Nama langkah (untuk tampilan di GitHub)
        uses: action/nama@v4    # Pakai action siap pakai dari marketplace
        # atau
        run: perintah-terminal  # Jalankan perintah langsung
```

---

## 🔧 Workflow CI/CD untuk Project Kita

Berikut alur lengkap yang akan kita buat:

```text
Push ke GitHub
      ↓
┌─────────────────────────────────────────┐
│  Job 1: test                            │
│  ① Checkout kode dari GitHub            │
│  ② Setup Java 21                        │
│  ③ Jalankan: mvnw test                  │
│     └── UserServiceTest (9 test)        │
│     └── UserControllerTest (11 test)    │
└─────────────────────────────────────────┘
      ↓ (hanya lanjut jika test LULUS)
┌─────────────────────────────────────────┐
│  Job 2: build-and-push                  │
│  ① Checkout kode                        │
│  ② Login ke Docker Hub                  │
│  ③ Build Docker Image                   │
│  ④ Push image ke Docker Hub             │
└─────────────────────────────────────────┘
```

---

## 🔐 GitHub Secrets

Saat pipeline perlu login ke Docker Hub, kita **tidak boleh** tulis username/password langsung di file `.yml` (bahaya! publik!).

Solusinya: **GitHub Secrets** — tempat menyimpan data sensitif yang terenkripsi.

```yaml
# ❌ JANGAN PERNAH BEGINI
- run: docker login -u "firman" -p "password123"

# ✅ Yang benar: pakai Secrets
- run: docker login -u ${{ secrets.DOCKER_USERNAME }} -p ${{ secrets.DOCKER_PASSWORD }}
```

### Cara set Secrets di GitHub:

```text
Repository GitHub kamu
  → Settings
    → Secrets and variables
      → Actions
        → New repository secret
```

Buat 2 secrets:
- `DOCKER_USERNAME` → username Docker Hub kamu
- `DOCKER_PASSWORD` → password Docker Hub kamu

---

## 📄 Contoh File ci.yml Lengkap

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:

  # =============================================
  # JOB 1: Jalankan semua test
  # =============================================
  test:
    name: Run Tests
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Cache Maven packages
        uses: actions/cache@v4
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}

      - name: Run tests
        run: ./mvnw test

  # =============================================
  # JOB 2: Build dan push Docker image
  # =============================================
  build-and-push:
    name: Build & Push Docker Image
    runs-on: ubuntu-latest
    needs: test

    if: github.event_name == 'push' && github.ref == 'refs/heads/main'

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Login to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}

      - name: Build and push Docker image
        uses: docker/build-push-action@v6
        with:
          context: .
          push: true
          tags: ${{ secrets.DOCKER_USERNAME }}/belajar-spring-docker:latest
```

---

## 🔍 Penjelasan Kunci

### Kenapa ada `needs: test`?

```yaml
build-and-push:
  needs: test    # Job ini HANYA jalan jika job "test" berhasil
```

Tanpa `needs`, job build bisa jalan bersamaan dengan test → Docker image bisa jadi walau testnya gagal. Dengan `needs: test`, build hanya jalan jika test **LULUS** dulu.

### Kenapa ada Cache Maven?

Setiap kali pipeline jalan, runner adalah komputer **fresh** (bersih). Maven harus download semua dependency dari internet — bisa 2-3 menit. Dengan cache, dependency disimpan dan dipakai ulang → pipeline lebih cepat.

---

## 📊 Tampilan di GitHub

```text
✅ CI/CD Pipeline
   └── ✅ test             (9 + 11 + 1 = 21 test lulus)
   └── ✅ build-and-push   (image berhasil di-push ke Docker Hub)
```

Kalau test gagal:
```text
❌ CI/CD Pipeline
   └── ❌ test             (ada test yang gagal)
   └── ⏭️ build-and-push   (di-skip otomatis)
```

---

## ❓ CI/CD vs Manual

| | Tanpa CI/CD | Dengan CI/CD |
|---|---|---|
| **Test** | Harus ingat jalankan manual | Otomatis setiap push |
| **Build image** | Manual: `docker build` | Otomatis |
| **Push image** | Manual: `docker push` | Otomatis |
| **Risiko lupa test** | Tinggi | Tidak mungkin |
| **Kerja tim** | Kacau | Terstruktur |

---

## 📝 Langkah Implementasi

```text
1. Pastikan repository sudah ada di GitHub
2. Buat folder .github/workflows/ di project
3. Buat file ci.yml
4. Set GitHub Secrets (DOCKER_USERNAME, DOCKER_PASSWORD)
5. Push ke GitHub → lihat tab Actions → pipeline jalan otomatis
```

---

## 🔑 Ringkasan

```text
CI/CD          = otomatisasi: test → build → push → deploy
GitHub Actions = tool CI/CD bawaan GitHub (gratis untuk public repo)
Workflow       = file YAML konfigurasi pipeline (.github/workflows/ci.yml)
Job            = kelompok pekerjaan (test, build, deploy)
Step           = satu langkah dalam job
Runner         = komputer virtual Ubuntu yang menjalankan pipeline
Secrets        = tempat simpan password/token secara aman di GitHub
needs          = membuat job menunggu job lain selesai dulu
```
