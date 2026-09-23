# Rangkuman Belajar: Level 6 (Docker Dasar)

Docker adalah teknologi yang memungkinkan kita untuk mengemas aplikasi beserta seluruh kebutuhannya (sistem operasi dasar, *library*, dependensi, konfigurasi) ke dalam satu wadah (*container*). Hal ini memecahkan masalah klasik para *programmer*: **"Tapi di laptop saya jalan kok!"**, karena lingkungan yang digunakan saat pengembangan di laptop akan 100% identik dengan yang ada di *server*.

Berikut adalah istilah dan konsep utama di ekosistem Docker yang wajib kamu pahami:

## 1. Dockerfile
- **Apa itu?**: `Dockerfile` adalah sebuah file teks (tanpa ekstensi) yang berisi sekumpulan instruksi (seperti resep masakan) untuk membuat sebuah **Docker Image**.
- **Contoh untuk Spring Boot**:
  ```dockerfile
  # Mengambil sistem operasi dasar yang sudah ada Java 21-nya
  FROM eclipse-temurin:21-jre
  
  # Menentukan folder kerja di dalam sistem container
  WORKDIR /app
  
  # Menyalin file .jar dari laptop kamu ke dalam container
  COPY target/*.jar app.jar
  
  # Memberi informasi port berapa yang digunakan aplikasi
  EXPOSE 8081
  
  # Perintah yang otomatis dijalankan ketika container dihidupkan
  ENTRYPOINT ["java", "-jar", "app.jar"]
  ```

## 2. Image
- **Apa itu?**: *Image* adalah cetakan (template) bersifat *read-only* yang dihasilkan setelah kita melakukan build dari `Dockerfile`. Ibarat sebuah file *installer* (`.exe`) yang siap dijalankan, tapi belum berjalan.
- *Image* ini berisi *source code* aplikasi kita, *runtime* (misal Java), dan konfigurasi sistem.
- Kamu bisa membuat *image* dengan perintah terminal: `docker build -t nama-aplikasi:versi .`

## 3. Container
- **Apa itu?**: *Container* adalah instansi yang hidup atau bentuk fisik yang berjalan dari sebuah *Image*. Ibarat sebuah aplikasi game yang *installer*-nya sudah diinstal dan sedang dimainkan.
- Kamu bisa menjalankan banyak *container* secara bersamaan dari satu *image* yang sama.
- Perintah menjalankannya: `docker run -d --name container-ku nama-aplikasi:versi`

## 4. Port (Port Mapping)
- **Apa itu?**: Agar aplikasi di dalam *container* bisa diakses oleh *browser* di laptopmu, kamu harus memetakan "pintu" (Port) laptopmu ke "pintu" *container*.
- Konsep pemetaannya adalah `Port_Laptop : Port_Container`.
- **Contoh**: Jika kamu menjalankan `docker run -p 8080:8081 nama-aplikasi`, artinya kamu bisa mengakses `localhost:8080` di browser laptop, lalu Docker akan otomatis meneruskan jaringan tersebut ke port `8081` milik *container* Spring Boot.

## 5. Environment Variable
- **Apa itu?**: Ini adalah variabel khusus (seperti kredensial) yang disuntikkan dari luar ke dalam *container* tanpa perlu membongkar atau mengubah kode program Java kamu.
- Sangat berguna dan **wajib digunakan** untuk mengatur hal-hal sensitif seperti *username* database, *password*, atau *URL Database* (seperti PostgreSQL) agar lebih dinamis (bisa diganti-ganti per *environment*).
- **Contoh pemakaian**: `docker run -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/appdb nama-aplikasi`

## 6. Network
- **Apa itu?**: Jaringan internal buatan Docker yang memungkinkan satu *container* bisa saling "mengobrol" dengan *container* lainnya secara aman.
- Secara bawaan (*default*), *container* Spring Boot **tidak bisa** berkomunikasi dengan *container* PostgreSQL, kecuali keduanya dimasukkan ke dalam satu *Network* buatan yang sama.
- Di dalam *Network* Docker, antar *container* **tidak lagi menggunakan `localhost`** untuk saling memanggil, melainkan saling panggil menggunakan **nama container-nya** (misal Spring Boot memanggil: `jdbc:postgresql://container_database_postgres:5432/db_name`).

## 7. Volume
- **Apa itu?**: Secara kodratnya, *container* itu bersifat sementara (*stateless/ephemeral*). Jika *container* PostgreSQL terhapus atau dimatikan, semua data tabel yang sudah disimpan di dalamnya **akan musnah selamanya**.
- **Volume** adalah cara Docker memetakan (menyambungkan) sebuah folder di dalam *container* (misal folder data *postgres*) ke folder fisik secara permanen di dalam *hardisk* laptop/server kamu.
- Dengan *Volume*, meskipun *container* database dihancurkan dan dibuat ulang, datanya akan tetap utuh karena fisiknya tersimpan di luar *container*.
