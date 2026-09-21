# Spring Boot Dasar

Catatan ini berisi materi Spring Boot dasar yang perlu dikuasai.
Pastikan sudah memahami Java dasar sebelum masuk ke sini.

---

## 1. Apa itu Spring Boot

Spring Boot adalah framework Java yang mempermudah pembuatan aplikasi backend.

```text
Tanpa Spring Boot:
- Konfigurasi manual banyak
- Setup server manual
- Dependency management rumit

Dengan Spring Boot:
- Auto configuration
- Embedded server (Tomcat)
- Starter dependency tinggal pilih
```

### Alur Kerja Aplikasi Spring Boot

```text
Client (Browser / Postman)
         ↓
    Controller   ← menerima request
         ↓
      Service    ← proses logic bisnis
         ↓
    Repository   ← akses database
         ↓
     Database    ← simpan data
```

---

## 2. Membuat Project Spring Boot

### Cara Membuat Project

Gunakan Spring Initializr: https://start.spring.io

Pilih:

```text
Project    : Maven
Language   : Java
Spring Boot: 3.x.x (versi terbaru)
Group      : com.example
Artifact   : demo
Packaging  : Jar
Java       : 17 atau 21
```

### Dependency Awal yang Dibutuhkan

```text
Spring Web           → untuk membuat REST API
Spring Data JPA      → untuk akses database
PostgreSQL Driver    → driver koneksi PostgreSQL
Spring Boot DevTools → auto restart saat development
Lombok               → mengurangi boilerplate code
```

### Struktur Project

```text
demo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/demo/
│   │   │       ├── DemoApplication.java        ← entry point
│   │   │       ├── controller/                  ← terima request
│   │   │       ├── service/                     ← logic bisnis
│   │   │       ├── repository/                  ← akses database
│   │   │       ├── entity/                      ← model database
│   │   │       └── dto/                         ← data transfer object
│   │   └── resources/
│   │       ├── application.yml                  ← konfigurasi
│   │       └── static/                          ← file statis
│   └── test/
│       └── java/
│           └── com/example/demo/
│               └── DemoApplicationTests.java
├── pom.xml                                      ← dependency Maven
└── mvnw                                         ← Maven wrapper
```

---

## 3. Entry Point Aplikasi

```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

`@SpringBootApplication` menggabungkan tiga annotation:

```text
@SpringBootApplication
├── @Configuration       → class ini berisi konfigurasi
├── @EnableAutoConfiguration → Spring auto konfigurasi
└── @ComponentScan       → scan semua class di package ini
```

---

## 4. Controller

Controller bertugas menerima HTTP request dan mengembalikan response.

### Controller Sederhana

```java
package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Halo, Spring Boot!";
    }
}
```

Akses: `GET http://localhost:8080/hello`

### Annotation Penting di Controller

```text
@RestController     → menandai class sebagai REST controller
@RequestMapping     → base path untuk semua endpoint di class ini
@GetMapping         → handle HTTP GET
@PostMapping        → handle HTTP POST
@PutMapping         → handle HTTP PUT
@DeleteMapping      → handle HTTP DELETE
@PathVariable       → ambil nilai dari URL path
@RequestParam       → ambil nilai dari query parameter
@RequestBody        → ambil data dari body request (JSON)
```

### Controller dengan Base Path

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping
    public String getAll() {
        return "Semua user";
    }

    @GetMapping("/{id}")
    public String getById(@PathVariable Long id) {
        return "User dengan ID: " + id;
    }

    @PostMapping
    public String create(@RequestBody String nama) {
        return "User dibuat: " + nama;
    }
}
```

Endpoint yang dihasilkan:

```text
GET    /api/users       → getAll()
GET    /api/users/1     → getById(1)
POST   /api/users       → create(body)
```

---

## 5. Service

Service berisi logic bisnis. Controller tidak boleh langsung akses database.

### Membuat Service

```java
package com.example.demo.service;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    public String getGreeting(String nama) {
        if (nama == null || nama.isEmpty()) {
            return "Halo, Guest!";
        }
        return "Halo, " + nama + "!";
    }
}
```

### Controller Memanggil Service

```java
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    // constructor injection (direkomendasikan)
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/greeting")
    public String greeting(@RequestParam(defaultValue = "") String nama) {
        return userService.getGreeting(nama);
    }
}
```

### Alur Request

```text
GET /api/greeting?nama=Budi
         ↓
UserController.greeting("Budi")
         ↓
UserService.getGreeting("Budi")
         ↓
return "Halo, Budi!"
```

---

## 6. Dependency Injection

Spring Boot otomatis membuat dan mengelola object (bean). Kita tidak perlu `new` manual.

### Cara Kerja

```text
Spring Container
├── Scan semua class dengan annotation
│   ├── @Component
│   ├── @Service
│   ├── @Repository
│   ├── @Controller
│   └── @RestController
├── Buat instance (bean) dari class tersebut
└── Inject (masukkan) ke class yang membutuhkan
```

### Contoh Injection

```java
@Service
public class UserService {
    // logic...
}

@RestController
public class UserController {

    // Spring otomatis inject UserService di sini
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
}
```

### Tiga Cara Injection

```java
// 1. Constructor Injection (DIREKOMENDASIKAN)
@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
}

// 2. Field Injection (tidak direkomendasikan)
@RestController
public class UserController {
    @Autowired
    private UserService userService;
}

// 3. Setter Injection (jarang dipakai)
@RestController
public class UserController {
    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
```

Selalu gunakan **Constructor Injection** karena:

- Field bisa `final` (immutable)
- Mudah di-test
- Dependency jelas terlihat

---

## 7. Konfigurasi (application.yml)

### application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: demo

  datasource:
    url: jdbc:postgresql://localhost:5432/demodb
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### Penjelasan Konfigurasi

```text
server.port                  → port aplikasi (default 8080)
spring.datasource.url        → URL koneksi database
spring.datasource.username   → username database
spring.datasource.password   → password database
spring.jpa.hibernate.ddl-auto → cara handle schema database
spring.jpa.show-sql          → tampilkan query SQL di log
```

### Opsi ddl-auto

```text
none     → tidak lakukan apa-apa (untuk production)
update   → update schema tanpa hapus data (untuk development)
create   → buat ulang schema setiap startup (hapus data!)
create-drop → buat schema saat start, hapus saat stop
validate → cek schema cocok, error kalau tidak
```

Untuk development gunakan `update`. Untuk production gunakan `none` atau `validate`.

### application.properties (Alternatif)

```properties
server.port=8080
spring.datasource.url=jdbc:postgresql://localhost:5432/demodb
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Keduanya fungsinya sama. YAML lebih rapi untuk konfigurasi yang bertingkat.

---

## 8. Entity

Entity adalah class Java yang merepresentasikan tabel di database.

### Membuat Entity

```java
package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nama;

    @Column(unique = true, nullable = false)
    private String email;

    private int umur;

    // constructor kosong (wajib untuk JPA)
    public User() {
    }

    public User(String nama, String email, int umur) {
        this.nama = nama;
        this.email = email;
        this.umur = umur;
    }

    // getter dan setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getUmur() {
        return umur;
    }

    public void setUmur(int umur) {
        this.umur = umur;
    }
}
```

### Entity dengan Lombok (Lebih Singkat)

```java
package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nama;

    @Column(unique = true, nullable = false)
    private String email;

    private int umur;
}
```

Lombok otomatis generate getter, setter, constructor, toString, equals, dan hashCode.

### Annotation Entity

```text
@Entity                → tandai class sebagai entity JPA
@Table(name = "...")   → nama tabel di database
@Id                    → primary key
@GeneratedValue        → auto generate ID
@Column                → konfigurasi kolom
  - nullable           → boleh null atau tidak
  - unique             → harus unik
  - length             → panjang maksimal
  - name               → nama kolom di database
```

### Tabel yang Dihasilkan

```sql
CREATE TABLE users (
    id    BIGSERIAL PRIMARY KEY,
    nama  VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    umur  INTEGER
);
```

---

## 9. Repository

Repository adalah interface untuk mengakses database. Spring Data JPA sudah menyediakan method CRUD otomatis.

### Membuat Repository

```java
package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring otomatis buatkan query berdasarkan nama method
    Optional<User> findByEmail(String email);

    List<User> findByNama(String nama);

    List<User> findByUmurGreaterThan(int umur);

    boolean existsByEmail(String email);
}
```

### Method Bawaan JpaRepository

```text
save(entity)          → simpan atau update entity
findById(id)          → cari berdasarkan ID (return Optional)
findAll()             → ambil semua data
deleteById(id)        → hapus berdasarkan ID
delete(entity)        → hapus entity
count()               → hitung jumlah data
existsById(id)        → cek apakah data ada
```

### Query Method Naming

Spring Data JPA bisa membuat query otomatis dari nama method:

```text
findByNama(String nama)
→ SELECT * FROM users WHERE nama = ?

findByEmail(String email)
→ SELECT * FROM users WHERE email = ?

findByUmurGreaterThan(int umur)
→ SELECT * FROM users WHERE umur > ?

findByNamaAndEmail(String nama, String email)
→ SELECT * FROM users WHERE nama = ? AND email = ?

findByNamaContaining(String keyword)
→ SELECT * FROM users WHERE nama LIKE '%keyword%'

findByNamaOrderByUmurDesc(String nama)
→ SELECT * FROM users WHERE nama = ? ORDER BY umur DESC
```

---

## 10. DTO (Data Transfer Object)

DTO dipakai untuk mengatur data yang diterima dari request dan dikirim ke response. Jangan langsung expose entity ke client.

### Kenapa Pakai DTO

```text
Tanpa DTO:
Client → Entity langsung → Database
- Field yang tidak perlu ikut terkirim
- Perubahan entity langsung mempengaruhi API

Dengan DTO:
Client → DTO → Service → Entity → Database
- Kontrol field mana yang diterima/dikirim
- Entity dan API terpisah
```

### Request DTO

```java
package com.example.demo.dto;

public class CreateUserRequest {
    private String nama;
    private String email;
    private int umur;

    // constructor, getter, setter
    public CreateUserRequest() {
    }

    public CreateUserRequest(String nama, String email, int umur) {
        this.nama = nama;
        this.email = email;
        this.umur = umur;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getUmur() {
        return umur;
    }

    public void setUmur(int umur) {
        this.umur = umur;
    }
}
```

### Response DTO

```java
package com.example.demo.dto;

public class UserResponse {
    private Long id;
    private String nama;
    private String email;
    private int umur;

    public UserResponse(Long id, String nama, String email, int umur) {
        this.id = id;
        this.nama = nama;
        this.email = email;
        this.umur = umur;
    }

    // getter saja (response tidak perlu setter)
    public Long getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public String getEmail() {
        return email;
    }

    public int getUmur() {
        return umur;
    }
}
```

---

## 11. CRUD Lengkap

Menggabungkan semuanya menjadi CRUD API lengkap.

### Service

```java
package com.example.demo.service;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // CREATE
    public UserResponse create(CreateUserRequest request) {
        User user = new User();
        user.setNama(request.getNama());
        user.setEmail(request.getEmail());
        user.setUmur(request.getUmur());

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    // READ ALL
    public List<UserResponse> getAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // READ BY ID
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        return toResponse(user);
    }

    // UPDATE
    public UserResponse update(Long id, CreateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        user.setNama(request.getNama());
        user.setEmail(request.getEmail());
        user.setUmur(request.getUmur());

        User updated = userRepository.save(user);
        return toResponse(updated);
    }

    // DELETE
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User tidak ditemukan");
        }
        userRepository.deleteById(id);
    }

    // helper: convert entity ke response DTO
    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getNama(),
                user.getEmail(),
                user.getUmur()
        );
    }
}
```

### Controller

```java
package com.example.demo.controller;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // POST /api/users
    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody CreateUserRequest request) {
        UserResponse response = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/users
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    // GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    // PUT /api/users/{id}
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    // DELETE /api/users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

### Endpoint yang Tersedia

```text
Method  URL              Body (JSON)                          Response
──────────────────────────────────────────────────────────────────────────
POST    /api/users       {"nama":"Budi","email":"..","umur":25}  201 Created
GET     /api/users       -                                       200 OK
GET     /api/users/1     -                                       200 OK
PUT     /api/users/1     {"nama":"Budi","email":"..","umur":26}  200 OK
DELETE  /api/users/1     -                                       204 No Content
```

### Contoh Request POST

```json
POST /api/users
Content-Type: application/json

{
    "nama": "Budi",
    "email": "budi@email.com",
    "umur": 25
}
```

### Contoh Response

```json
{
    "id": 1,
    "nama": "Budi",
    "email": "budi@email.com",
    "umur": 25
}
```

---

## 12. ResponseEntity dan HTTP Status

### HTTP Status yang Sering Dipakai

```text
200 OK             → request berhasil
201 Created        → data berhasil dibuat
204 No Content     → berhasil tapi tidak ada data yang dikembalikan
400 Bad Request    → request tidak valid
404 Not Found      → data tidak ditemukan
500 Internal Error → error di server
```

### Menggunakan ResponseEntity

```java
// 200 OK dengan body
return ResponseEntity.ok(data);

// 201 Created dengan body
return ResponseEntity.status(HttpStatus.CREATED).body(data);

// 204 No Content
return ResponseEntity.noContent().build();

// 404 Not Found
return ResponseEntity.notFound().build();

// 400 Bad Request dengan pesan error
return ResponseEntity.badRequest().body("Email sudah terdaftar");
```

---

## 13. Validasi Request

### Tambah Dependency

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### Validasi di DTO

```java
import jakarta.validation.constraints.*;

public class CreateUserRequest {

    @NotBlank(message = "Nama tidak boleh kosong")
    private String nama;

    @NotBlank(message = "Email tidak boleh kosong")
    @Email(message = "Format email tidak valid")
    private String email;

    @Min(value = 1, message = "Umur minimal 1")
    @Max(value = 150, message = "Umur maksimal 150")
    private int umur;

    // constructor, getter, setter
}
```

### Aktifkan Validasi di Controller

```java
@PostMapping
public ResponseEntity<UserResponse> create(
        @Valid @RequestBody CreateUserRequest request) {
    UserResponse response = userService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

Tambahkan `@Valid` sebelum `@RequestBody`.

### Annotation Validasi

```text
@NotNull      → tidak boleh null
@NotBlank     → tidak boleh null, kosong, atau spasi saja (untuk String)
@NotEmpty     → tidak boleh null atau kosong (untuk String, Collection)
@Email        → harus format email valid
@Min(value)   → nilai minimal
@Max(value)   → nilai maksimal
@Size(min, max) → panjang string minimal dan maksimal
@Pattern(regexp) → harus sesuai regex
@Positive     → harus positif
```

---

## 14. Menjalankan Aplikasi

### Via IDE

Klik tombol Run di class `DemoApplication.java`.

### Via Terminal

```bash
# dengan Maven wrapper
./mvnw spring-boot:run

# atau di Windows
mvnw.cmd spring-boot:run
```

### Build JAR

```bash
# build
./mvnw clean package

# jalankan JAR
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### Log Startup yang Normal

```text
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.x.x)

Started DemoApplication in 2.345 seconds
Tomcat started on port 8080
```

Kalau muncul seperti ini artinya aplikasi berhasil jalan.

---

## Rangkuman Annotation

```text
Annotation            Fungsi
─────────────────────────────────────────────────
@SpringBootApplication  Entry point aplikasi
@RestController         REST controller (return JSON)
@RequestMapping         Base path endpoint
@GetMapping             Handle GET request
@PostMapping            Handle POST request
@PutMapping             Handle PUT request
@DeleteMapping          Handle DELETE request
@PathVariable           Ambil nilai dari URL
@RequestParam           Ambil nilai dari query string
@RequestBody            Ambil data dari body (JSON)
@Valid                  Aktifkan validasi
@Service                Tandai class sebagai service
@Repository             Tandai class sebagai repository
@Entity                 Tandai class sebagai entity JPA
@Table                  Nama tabel di database
@Id                     Primary key
@GeneratedValue         Auto generate ID
@Column                 Konfigurasi kolom
@NotBlank               Validasi tidak boleh kosong
@Email                  Validasi format email
@Min / @Max             Validasi nilai minimal/maksimal
```

## Rangkuman Alur

```text
1. Buat project di start.spring.io
2. Setup application.yml (database, port)
3. Buat Entity (tabel database)
4. Buat Repository (akses database)
5. Buat DTO (request/response)
6. Buat Service (logic bisnis)
7. Buat Controller (endpoint API)
8. Tambah validasi
9. Test pakai Postman / curl
```
