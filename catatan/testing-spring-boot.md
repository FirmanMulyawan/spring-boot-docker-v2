# Testing di Spring Boot

## 🍼 Bahasa Bayi: Kenapa Harus Testing?

Bayangkan kamu tukang roti.

Setiap kali kamu bikin roti baru, kamu harus **mencicipnya dulu** sebelum dijual ke pelanggan. Kalau tidak dicicipin, bisa saja rotinya gosong atau asin banget dan pelanggan kecewa.

Di dunia pemrograman, **Testing = proses mencicipin kode kamu secara otomatis**, sebelum kode itu dijalankan di server production (tempat aplikasi nyata yang dipakai user beneran).

```text
Kamu nulis kode baru
       ↓
Jalankan Testing otomatis (seperti cicip roti)
       ↓
Test Berhasil (Roti enak) → Deploy ke Production ✅
Test Gagal   (Roti gosong) → Perbaiki dulu ❌
```

---

## 🔑 3 Jenis Testing yang Akan Dipelajari

### Analogi Sederhana:

| Jenis Test | Analogi | Penjelasan |
|---|---|---|
| **Unit Test** | Cicipin adonan roti satu persatu | Menguji **satu method/fungsi** secara terisolasi |
| **Integration Test** | Cicipin roti yang sudah matang utuh | Menguji **beberapa komponen bekerja bersama** |
| **MockMvc Test** | Coba pesan roti lewat kasir | Menguji **endpoint API (Controller)** dari luar |

---

## 🛠️ Tools yang Dipakai

Spring Boot sudah menyiapkan semua tools testing di dalam dependency `spring-boot-starter-webmvc-test` yang ada di `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc-test</artifactId>
    <scope>test</scope>
</dependency>
```

Di dalamnya sudah ada:

| Tool | Fungsi |
|---|---|
| **JUnit 5** | Framework utama untuk menulis dan menjalankan test |
| **Mockito** | Membuat "boneka palsu" dari class yang belum ada / tidak mau dipakai (Repository, Service, dll) |
| **AssertJ** | Membantu kita "memeriksa jawaban" test dengan cara yang mudah dibaca |
| **MockMvc** | Simulasi HTTP request ke Controller tanpa perlu server beneran nyala |

---

## 📍 Letak File Test

Semua file test diletakkan di folder `src/test/java/`, bukan di `src/main/java/`.

```text
src/
├── main/
│   └── java/
│       └── com/example/belajar_spring_docker_v2/
│           ├── controller/   ← Kode asli
│           ├── service/      ← Kode asli
│           └── repository/   ← Kode asli
│
└── test/
    └── java/
        └── com/example/belajar_spring_docker_v2/
            ├── controller/   ← File test Controller
            ├── service/      ← File test Service
            └── repository/   ← File test Repository
```

> **Aturan Penamaan:** Nama file test selalu sama dengan nama class aslinya + diakhiri kata `Test`.
> Contoh: `UserService.java` → `UserServiceTest.java`

---

## 1. UNIT TEST (Menguji Service)

### 🍼 Bahasa Bayi:

Unit Test itu seperti kamu menguji **satu resep masak secara terpisah**.

Misalnya, kamu ingin test fungsi `getAllUsers()` di `UserService`.

Masalahnya: `UserService` butuh `UserRepository` (koneksi ke database). Tapi waktu test, kita **TIDAK MAU** konek ke database beneran. Kenapa?

1. Database mungkin belum nyala.
2. Test bisa mencemari data di database.
3. Test jadi lambat karena harus konek database sungguhan.

Solusinya adalah **Mockito** — yaitu membuat **"boneka/tiruan"** dari `UserRepository`. Boneka ini kita ajari untuk pura-pura memberikan data ketika dipanggil.

```text
UserService (yang mau kita test)
    |
    └── UserRepository  <-- DIGANTI BONEKA PALSU (Mock)
                             (tidak perlu database beneran!)
```

### Anatomi Dasar Unit Test:

```java
@ExtendWith(MockitoExtension.class)  // Aktifkan Mockito di class ini
class UserServiceTest {

    @Mock  // Buat boneka palsu UserRepository
    private UserRepository userRepository;

    @InjectMocks  // Masukkan boneka ke dalam UserService
    private UserService userService;

    @Test  // Ini adalah 1 fungsi test
    void namaFungsiTest() {
        // LANGKAH 1: ARRANGE — Siapkan data dan ajarkan boneka
        // (Apa yang akan terjadi kalau method boneka dipanggil?)

        // LANGKAH 2: ACT — Jalankan method yang ingin ditest

        // LANGKAH 3: ASSERT — Periksa hasilnya sesuai harapan tidak?
    }
}
```

### Pola AAA (Arrange - Act - Assert):

Setiap test selalu mengikuti pola ini:

```text
Arrange → Siapkan bahan-bahan (data dummy & atur perilaku mock)
   ↓
Act     → Jalankan fungsi yang ingin ditest
   ↓
Assert  → Cek apakah hasilnya sesuai harapan
```

### Contoh Nyata: Test `getAllUsers()`

```java
@Test
void getAllUsers_ShouldReturnListOfUsers() {

    // ARRANGE: Siapkan data dummy dan ajarkan boneka
    User userPalsu = new User();
    userPalsu.setId(1L);
    userPalsu.setName("Budi");
    userPalsu.setEmail("budi@email.com");

    // Ajarkan boneka: "kalau findAll() dipanggil, kembalikan list berisi userPalsu"
    when(userRepository.findAll()).thenReturn(List.of(userPalsu));

    // ACT: Jalankan method yang mau ditest
    List<UserResponseDTO> result = userService.getAllUsers();

    // ASSERT: Periksa hasilnya
    assertThat(result).hasSize(1);                    // harus ada 1 item
    assertThat(result.get(0).name()).isEqualTo("Budi"); // nama harus "Budi"
}
```

### Annotations Penting Unit Test:

| Annotation | Fungsi |
|---|---|
| `@ExtendWith(MockitoExtension.class)` | Aktifkan fitur Mockito di class test |
| `@Mock` | Buat boneka palsu dari suatu class (tidak konek ke real implementation) |
| `@InjectMocks` | Masukkan semua boneka `@Mock` ke dalam class yang ingin ditest |
| `@Test` | Menandai bahwa fungsi ini adalah sebuah test case |
| `@BeforeEach` | Fungsi yang dijalankan sebelum setiap test (biasanya untuk setup data awal) |

### Perintah Mockito yang Sering Dipakai:

```java
// Ajarkan boneka untuk mengembalikan nilai tertentu
when(userRepository.findAll()).thenReturn(List.of(user));
when(userRepository.findById(1L)).thenReturn(Optional.of(user));

// Ajarkan boneka untuk melempar exception
when(userRepository.findById(99L)).thenThrow(new RuntimeException("Not found"));

// Verifikasi apakah method boneka benar-benar dipanggil
verify(userRepository, times(1)).findAll();
verify(userRepository, never()).deleteById(anyLong());
```

### Perintah AssertJ yang Sering Dipakai:

```java
// Cek nilai
assertThat(result).isEqualTo("Budi");
assertThat(result).isNotNull();
assertThat(result).isNull();

// Cek boolean
assertThat(result).isTrue();
assertThat(result).isFalse();

// Cek list/collection
assertThat(result).hasSize(3);
assertThat(result).isEmpty();
assertThat(result).isNotEmpty();
assertThat(result).contains("Budi");

// Cek exception yang dilempar
assertThatThrownBy(() -> userService.getUserById(99L))
    .isInstanceOf(RuntimeException.class);
```

---

## 2. CONTROLLER TEST dengan MockMvc

### 🍼 Bahasa Bayi:

MockMvc itu seperti **robot yang pura-pura jadi browser atau Postman**.

Robot ini mengirim request HTTP (GET, POST, PUT, DELETE) ke Controller kamu, dan kita bisa periksa apakah response-nya benar.

Bedanya dengan test langsung konek ke URL nyata: MockMvc **tidak perlu server Tomcat beneran nyala**. Semua dilakukan di dalam memori Java saja. Jadi super cepat!

```text
MockMvc (Robot Postman Palsu)
    |
    | Kirim: GET /users
    ↓
UserController  <-- Yang ditest
    |
    └── UserService  <-- DIGANTI BONEKA (Mock)
```

### Anatomi Dasar MockMvc Test:

```java
@WebMvcTest(UserController.class)  // Hanya load Controller, bukan seluruh aplikasi
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;  // Robot Pengirim Request

    @MockBean  // Buat boneka UserService (bukan @Mock biasa!)
    private UserService userService;

    @Test
    void getNamaTest() throws Exception {

        // ARRANGE: Ajarkan boneka UserService
        // ACT & ASSERT: Kirim request dan periksa hasilnya sekaligus
        mockMvc.perform(get("/users"))         // Robot kirim GET /users
               .andExpect(status().isOk())     // Hasilnya harus HTTP 200
               .andExpect(jsonPath("$").isArray()); // Body harus berupa array JSON
    }
}
```

### Contoh Lengkap: Test GET /users

```java
@Test
void getUsers_ShouldReturn200AndListOfUsers() throws Exception {

    // ARRANGE
    UserResponseDTO userDto = new UserResponseDTO(1L, "Budi", "budi@email.com");
    when(userService.getAllUsers()).thenReturn(List.of(userDto));

    // ACT & ASSERT
    mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].name").value("Budi"))
           .andExpect(jsonPath("$[0].email").value("budi@email.com"));
}
```

### Contoh Lengkap: Test POST /users

```java
@Test
void createUser_ShouldReturn201() throws Exception {

    // ARRANGE
    UserRequestDTO request = new UserRequestDTO("Budi", "budi@email.com");
    UserResponseDTO response = new UserResponseDTO(1L, "Budi", "budi@email.com");
    when(userService.createUser(any())).thenReturn(response);

    // ACT & ASSERT
    mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Budi",
                      "email": "budi@email.com"
                    }
                """))
           .andExpect(status().isCreated())       // HTTP 201
           .andExpect(jsonPath("$.id").value(1))
           .andExpect(jsonPath("$.name").value("Budi"));
}
```

### Contoh Lengkap: Test GET /users/{id} — User Tidak Ditemukan

```java
@Test
void getUserById_WhenNotFound_ShouldReturn404() throws Exception {

    // ARRANGE: Ajarkan boneka bahwa user dengan ID 99 tidak ada
    when(userService.getUserById(99L)).thenReturn(Optional.empty());

    // ACT & ASSERT
    mockMvc.perform(get("/users/99"))
           .andExpect(status().isNotFound());  // HTTP 404
}
```

### Perintah MockMvc yang Sering Dipakai:

| Perintah | Fungsi |
|---|---|
| `mockMvc.perform(get("/users"))` | Kirim GET request ke `/users` |
| `mockMvc.perform(post("/users").content(...))` | Kirim POST request dengan body JSON |
| `.andExpect(status().isOk())` | Periksa status HTTP 200 |
| `.andExpect(status().isCreated())` | Periksa status HTTP 201 |
| `.andExpect(status().isNotFound())` | Periksa status HTTP 404 |
| `.andExpect(jsonPath("$.name").value("Budi"))` | Periksa field `name` di JSON response |
| `.andExpect(jsonPath("$").isArray())` | Periksa bahwa response JSON berupa array |

### Annotations Penting MockMvc Test:

| Annotation | Fungsi |
|---|---|
| `@WebMvcTest(Controller.class)` | Load hanya layer Controller (lebih ringan dari `@SpringBootTest`) |
| `@MockBean` | Buat boneka Spring Bean (berbeda dengan `@Mock` dari Mockito murni) |
| `@Autowired MockMvc mockMvc` | Inject robot pengirim request HTTP |

---

## 3. Menjalankan Test

### Cara 1: Semua Test Sekaligus (lewat Maven)

```bash
./mvnw test
```

atau di Windows:

```powershell
.\mvnw.cmd test
```

### Cara 2: Test Satu File Saja

```bash
./mvnw test -Dtest=UserServiceTest
```

### Cara 3: Test Satu Method Saja

```bash
./mvnw test -Dtest=UserServiceTest#getAllUsers_ShouldReturnListOfUsers
```

### Cara 4: Lewat IDE (IntelliJ / VS Code)

Klik tombol ▶ hijau di sebelah kiri baris `@Test` atau nama class test. Hasil test akan langsung terlihat di tab "Test Results".

---

## 4. Membaca Hasil Test

Ketika test dijalankan, hasilnya akan seperti ini:

### Test Berhasil (PASSED) ✅:

```text
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Test Gagal (FAILED) ❌:

```text
[ERROR] UserServiceTest.getAllUsers_ShouldReturnListOfUsers FAILED
        expected: <"Budi">
         but was: <"Andi">
[ERROR] Tests run: 5, Failures: 1, Errors: 0, Skipped: 0
[ERROR] BUILD FAILURE
```

Test gagal artinya kodenya ada yang salah. **Ini bagus!** Kita tahu masalahnya sebelum deploy ke production.

---

## 5. Ringkasan Cepat

```text
Unit Test (Service)
  └── Pakai: @ExtendWith(MockitoExtension.class), @Mock, @InjectMocks
  └── Tujuan: Test logika bisnis terisolasi tanpa database
  └── when().thenReturn() untuk ajarkan boneka
  └── assertThat() untuk periksa hasil

MockMvc Test (Controller)
  └── Pakai: @WebMvcTest, @MockBean, @Autowired MockMvc
  └── Tujuan: Test endpoint HTTP tanpa server nyala
  └── mockMvc.perform() untuk kirim request
  └── .andExpect() untuk periksa response

Jalankan Test:
  └── .\mvnw.cmd test         → Semua test
  └── .\mvnw.cmd test -Dtest=NamaTest  → Satu class test saja
```
