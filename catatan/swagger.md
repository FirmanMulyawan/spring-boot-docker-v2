# Swagger / OpenAPI dalam Spring Boot

## Apa itu Swagger?

Swagger (sekarang disebut **OpenAPI**) adalah tools untuk membuat **dokumentasi API secara otomatis**.

Tanpa Swagger, orang lain yang ingin menggunakan API kamu harus tanya-tanya:
- "Endpoint apa saja yang ada?"
- "Body-nya isi apa?"
- "Response-nya format gimana?"

Dengan Swagger, semua itu langsung **terdokumentasi dan bisa dicoba langsung** di browser.

---

## Tampilan Swagger

Setelah Swagger dipasang, kamu bisa buka di browser:

```
http://localhost:8081/swagger-ui.html
```

Tampilannya berupa halaman web interaktif yang menampilkan semua endpoint API beserta:
- Method (GET, POST, PUT, DELETE)
- URL endpoint
- Parameter yang dibutuhkan
- Contoh request body
- Contoh response
- Tombol **"Try it out"** untuk langsung test API

---

## Library yang Digunakan

Untuk Spring Boot, kita gunakan **SpringDoc OpenAPI**:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.9</version>
</dependency>
```

> **Catatan:** Jangan pakai `springfox` (library lama). Gunakan `springdoc-openapi` yang sudah support Spring Boot 3/4.

---

## Korelasi Versi SpringDoc ↔ Spring Boot

| Spring Boot | SpringDoc OpenAPI | Keterangan |
|---|---|---|
| **2.x** | `springdoc-openapi-ui` **1.x** | Artifact ID berbeda! |
| **3.x** | `springdoc-openapi-starter-webmvc-ui` **2.x** | Wajib pakai versi 2 |
| **4.x** | `springdoc-openapi-starter-webmvc-ui` **2.7+** | Yang kita pakai (2.8.9) |

### Kenapa berbeda?

Spring Boot 3.x ke atas migrasi ke **Jakarta EE**:

```text
Spring Boot 2.x → pakai javax.servlet.*
Spring Boot 3.x → pakai jakarta.servlet.* (beda package!)
Spring Boot 4.x → lanjutan dari 3.x
```

SpringDoc 2.x dibuat ulang untuk mendukung perubahan besar ini.

### Cara cek versi yang tepat

Lihat Spring Boot kamu di `pom.xml`:
```xml
<parent>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.1.1</version>  ← Spring Boot 4.x
</parent>
```

Karena Spring Boot **4.1.1** → pakai SpringDoc **2.8.9** ✅

### Tanda kamu salah pilih versi

```text
❌ Pakai springdoc 1.x di Spring Boot 3/4
   → Error: ClassNotFoundException javax.servlet...

❌ Pakai artifact lama "springdoc-openapi-ui" di Spring Boot 3/4
   → Error tidak kompatibel

✅ Pakai springdoc-openapi-starter-webmvc-ui 2.8.9 di Spring Boot 4.x
   → Kompatibel ✅
```

---

## Konfigurasi Dasar di application.yaml

```yaml
springdoc:
  api-docs:
    path: /api-docs        # URL untuk raw JSON spec
  swagger-ui:
    path: /swagger-ui.html # URL untuk tampilan UI
```

---

## Anotasi Swagger di Controller

### `@Tag` — Memberi nama grup endpoint

```java
@RestController
@RequestMapping("/users")
@Tag(name = "User API", description = "API untuk mengelola data user")
public class UserController {
```

### `@Operation` — Mendeskripsikan satu endpoint

```java
@GetMapping
@Operation(
    summary = "Ambil semua user",
    description = "Mengembalikan daftar semua user yang tersimpan di database"
)
public ResponseEntity<List<UserResponseDTO>> getUsers() {
```

### `@ApiResponse` — Mendokumentasikan response

```java
@PostMapping
@Operation(summary = "Buat user baru")
@ApiResponse(responseCode = "201", description = "User berhasil dibuat")
@ApiResponse(responseCode = "400", description = "Data tidak valid")
public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO dto) {
```

### `@Schema` — Mendokumentasikan field di DTO

```java
public record UserRequestDTO(
    @NotBlank(message = "Name is mandatory")
    @Schema(description = "Nama lengkap user", example = "Firman Mulyawan")
    String name,

    @Email(message = "Email format is invalid")
    @Schema(description = "Alamat email user", example = "firman@gmail.com")
    String email
) {}
```

---

## Konfigurasi Info Aplikasi (Opsional)

Bisa tambahkan informasi umum tentang API di class konfigurasi:

```java
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Belajar Spring Boot API")
                        .version("1.0.0")
                        .description("API dokumentasi untuk project belajar Spring Boot + Docker")
                        .contact(new Contact()
                                .name("Firman Mulyawan")
                                .email("firman@gmail.com")));
    }
}
```

---

## URL Penting Setelah Swagger Dipasang

| URL | Fungsi |
|---|---|
| `http://localhost:8081/swagger-ui.html` | Tampilan UI interaktif |
| `http://localhost:8081/api-docs` | Raw JSON spec (bisa diimport ke Postman) |
| `http://localhost:8081/api-docs.yaml` | Format YAML spec |

---

## Keuntungan Swagger

```text
1. Dokumentasi otomatis → tidak perlu buat dokumen manual
2. Bisa langsung test API di browser → tidak perlu Postman
3. Bisa di-import ke Postman → generate collection otomatis
4. Tim frontend tahu format request/response tanpa tanya backend
5. Standar industri (OpenAPI Specification)
```

---

## Swagger vs Postman

| | Swagger | Postman |
|---|---|---|
| **Fungsi** | Dokumentasi + test | Test API |
| **Lokasi** | Di dalam aplikasi | Aplikasi terpisah |
| **Auto-generate** | Ya, dari kode | Tidak (manual) |
| **Bagikan ke tim** | Buka URL saja | Export collection |
| **Cocok untuk** | Dokumentasi resmi | Testing kompleks |

Keduanya sering dipakai **bersama-sama** di project nyata.

---

## Langkah Implementasi

```text
1. Tambah dependency springdoc-openapi di pom.xml
2. Tambah konfigurasi di application.yaml
3. (Opsional) Buat SwaggerConfig.java
4. Tambah anotasi @Tag, @Operation di Controller
5. Tambah anotasi @Schema di DTO
6. Build & run
7. Buka http://localhost:8081/swagger-ui.html
```

---

## Ringkasan

```text
Swagger  = dokumentasi API otomatis yang bisa dicoba langsung di browser
OpenAPI  = standar/format spesifikasi API (Swagger mengikuti standar ini)
SpringDoc = library untuk generate Swagger di Spring Boot
@Tag     = nama grup endpoint di Swagger
@Operation = deskripsi satu endpoint
@Schema  = deskripsi field di DTO
```
