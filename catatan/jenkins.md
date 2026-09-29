# CI/CD dengan Jenkins

## Apa itu Jenkins?

Jenkins adalah server CI/CD yang kamu **jalankan sendiri** (self-hosted).

Bedanya dengan GitHub Actions:

| | GitHub Actions | Jenkins |
|---|---|---|
| **Hosting** | Di server GitHub (cloud) | Di server/laptop kamu sendiri |
| **Bayar?** | Gratis untuk public repo | Gratis (open source) |
| **Konfigurasi** | File `.github/workflows/*.yml` | File `Jenkinsfile` di root project |
| **Populer di** | Project open source, startup | Perusahaan enterprise |
| **Kontrol** | Terbatas pada fitur GitHub | Bebas, bisa kustomisasi apapun |

---

## Cara Kerja Jenkins

```text
Developer push kode ke GitHub
        ↓
Jenkins deteksi perubahan (via webhook atau polling)
        ↓
Jenkins baca Jenkinsfile
        ↓
Jalankan stage per stage:
  Stage 1: Checkout   → ambil kode
  Stage 2: Test       → jalankan mvnw test
  Stage 3: Build JAR  → mvnw package
  Stage 4: Build Image→ docker build
  Stage 5: Push       → docker push ke Docker Hub
        ↓
Notifikasi: SUKSES / GAGAL
```

---

## Istilah Jenkins

| Istilah | Artinya |
|---|---|
| **Pipeline** | Alur kerja otomatis yang didefinisikan di Jenkinsfile |
| **Stage** | Satu tahap dalam pipeline (misal: Test, Build, Deploy) |
| **Step** | Satu perintah dalam stage |
| **Agent** | Mesin yang menjalankan pipeline |
| **Credentials** | Tempat simpan username/password secara aman di Jenkins |
| **Webhook** | Notifikasi otomatis dari GitHub ke Jenkins saat ada push |

---

## Jalankan Jenkins via Docker

Cara termudah menjalankan Jenkins adalah via Docker:

```bash
# Jalankan Jenkins
docker compose -f docker-compose.jenkins.yml up -d

# Lihat log awal Jenkins
docker logs jenkins

# Buka di browser
# http://localhost:8080
```

---

## Setup Jenkins Pertama Kali

### 1. Ambil initial password

```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Masukkan password ini saat pertama buka `http://localhost:8080`.

### 2. Install plugin yang dibutuhkan

Di halaman setup awal, pilih:
> **Install suggested plugins**

Lalu tambahkan plugin ini secara manual:
```
Jenkins → Manage Jenkins → Plugins → Available plugins

Cari dan install:
- Docker Pipeline
- Docker Commons
```

### 3. Konfigurasi JDK dan Maven

```
Jenkins → Manage Jenkins → Tools

JDK:
  Name: JDK-21
  Centang: Install automatically → AdoptOpenJDK 21

Maven:
  Name: Maven-3
  Centang: Install automatically → versi terbaru
```

### 4. Simpan Credentials Docker Hub

```
Jenkins → Manage Jenkins → Credentials → System → Global credentials
→ Add Credentials

Kind     : Username with password
Username : (username Docker Hub kamu)
Password : (password Docker Hub kamu)
ID       : dockerhub-credentials   ← harus sama dengan di Jenkinsfile
```

---

## Buat Pipeline Job di Jenkins

```
Jenkins → New Item

Nama   : belajar-spring-docker
Type   : Pipeline
→ OK

Di halaman konfigurasi:
  Pipeline → Definition: Pipeline script from SCM
  SCM: Git
  Repository URL: (URL GitHub repo kamu)
  Branch: */main
  Script Path: belajar-spring-docker-v2/Jenkinsfile
→ Save
```

---

## Anatomi Jenkinsfile

```groovy
pipeline {
    agent any          // Jalankan di agent manapun

    environment {
        IMAGE_NAME = "username/nama-image"
    }

    tools {
        jdk   'JDK-21'    // Nama harus sama dengan konfigurasi di Jenkins Tools
        maven 'Maven-3'
    }

    stages {
        stage('Nama Stage') {
            steps {
                echo 'Ini adalah langkah pertama'
                sh './mvnw test'    // Jalankan perintah shell
            }
        }
    }

    post {
        success { echo 'Berhasil!' }
        failure { echo 'Gagal!'    }
    }
}
```

---

## Perbandingan Jenkinsfile vs GitHub Actions

```text
GitHub Actions:            Jenkins:
─────────────────          ──────────────────
on: push                   triggers (webhook)
  branches: [main]           branch: main
                           
jobs:                      stages:
  test:                      stage(Test):
    steps:                     steps:
      - run: ./mvnw test         sh './mvnw test'
                           
  build:                     stage(Build):
    needs: test                (otomatis setelah Test)
    steps:                     steps:
      - run: docker build        sh 'docker build ...'
```

---

## Ringkasan

```text
Jenkins    = server CI/CD yang kamu hosting sendiri
Jenkinsfile= file konfigurasi pipeline (di root project)
Stage      = tahap dalam pipeline
Credentials= tempat simpan password aman di Jenkins
Agent      = mesin yang menjalankan pipeline
```
