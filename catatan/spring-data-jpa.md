# Spring Data JPA

Spring Data JPA adalah salah satu komponen terpenting di ekosistem Spring Boot yang wajib dipahami. Ia menjembatani kode Java dengan *database* relasional tanpa perlu banyak menulis SQL murni.

---

## Apa itu Spring Data JPA?

Spring Data JPA adalah modul dari Spring Framework yang mengimplementasikan konsep **ORM (Object Relational Mapping)**. Di balik layar, ia menggunakan **Hibernate** sebagai implementasi JPA (*Java Persistence API*).

**Intinya**: Developer cukup bekerja dengan *object* Java, dan Spring Data JPA yang akan menerjemahkannya menjadi perintah SQL ke database secara otomatis.

---

## Komponen Utama

### 1. `@Entity` — Mendefinisikan Tabel

Class Java yang diberi anotasi `@Entity` akan dianggap sebagai representasi sebuah tabel di database.

```java
import jakarta.persistence.*;

@Entity
@Table(name = "products") // nama tabel di database
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Double price;

    // getter & setter...
}
```

| Anotasi | Kegunaan |
|---|---|
| `@Entity` | Menandai class ini sebagai tabel database |
| `@Table(name = "...")` | Menentukan nama tabel secara eksplisit |
| `@Id` | Menandai field ini sebagai *Primary Key* |
| `@GeneratedValue` | Mengatur strategi auto-increment ID |
| `@Column` | Konfigurasi tambahan pada kolom (nullable, unique, dll) |

---

### 2. `Repository` — Antarmuka ke Database

Repository adalah *interface* yang mengekstensi `JpaRepository`. Spring Data JPA akan secara **otomatis** menghasilkan implementasinya di balik layar.

```java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Tidak perlu kode apapun untuk fungsi CRUD dasar!
}
```

`JpaRepository<Product, Long>` menerima dua parameter:
- `Product` → Tipe *Entity* yang dikelola
- `Long` → Tipe data *Primary Key*

#### Fungsi Bawaan (Built-in)

Dengan mengekstensi `JpaRepository`, kamu sudah mendapatkan fungsi-fungsi ini **secara gratis**:

| Method | Kegunaan |
|---|---|
| `save(entity)` | Menyimpan atau memperbarui data |
| `findAll()` | Mengambil semua data |
| `findById(id)` | Mencari data berdasarkan ID, mengembalikan `Optional<T>` |
| `deleteById(id)` | Menghapus data berdasarkan ID |
| `existsById(id)` | Mengecek apakah data dengan ID tersebut ada |
| `count()` | Menghitung jumlah total data |

---

### 3. Query Method — Query dari Nama Fungsi

Spring Data JPA dapat membuat query SQL otomatis hanya dari **penamaan method** di repository.

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // SELECT * FROM products WHERE name = ?
    List<Product> findByName(String name);

    // SELECT * FROM products WHERE price < ?
    List<Product> findByPriceLessThan(Double price);

    // SELECT * FROM products WHERE name LIKE ?
    List<Product> findByNameContaining(String keyword);
}
```

---

### 4. `@Query` — Query Manual dengan JPQL

Untuk query yang lebih kompleks, gunakan anotasi `@Query` dengan JPQL (*Java Persistence Query Language*). JPQL mirip SQL, tapi menggunakan nama *Entity* dan *field* Java, bukan nama tabel dan kolom di database.

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :min AND :max")
    List<Product> findByPriceRange(@Param("min") Double min, @Param("max") Double max);
}
```

---

## Konfigurasi di `application.properties`

Berikut adalah konfigurasi dasar yang dibutuhkan untuk menghubungkan Spring Boot ke PostgreSQL:

```properties
# Koneksi Database
spring.datasource.url=jdbc:postgresql://localhost:5432/nama_database
spring.datasource.username=postgres
spring.datasource.password=password_kamu
spring.datasource.driver-class-name=org.postgresql.Driver

# Hibernate / JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### Penjelasan `ddl-auto`:

| Nilai | Perilaku |
|---|---|
| `create` | Menghapus & membuat ulang tabel setiap kali aplikasi *restart* |
| `create-drop` | Membuat tabel saat *start*, menghapusnya saat *stop* |
| `update` | Memperbarui skema tabel tanpa menghapus data yang ada (**direkomendasikan untuk development**) |
| `validate` | Hanya memvalidasi, tidak mengubah apapun (**direkomendasikan untuk production**) |
| `none` | Tidak melakukan apa-apa |

---

## Dependency di `pom.xml`

Pastikan kedua dependency berikut ada di `pom.xml`:

```xml
<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Driver PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## Cara Pakai di Service

Repository selalu dipanggil dari lapisan `service`, bukan dari `controller`.

```java
@Service
public class ProductService {

    private final ProductRepository productRepository;

    // Constructor Injection (cara terbaik)
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product tidak ditemukan dengan id: " + id));
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
```

---

## Ringkasan Alur Data

```
Client (HTTP Request)
    ↓
Controller  →  menerima request, meneruskan ke Service
    ↓
Service     →  berisi logika bisnis, memanggil Repository
    ↓
Repository  →  berkomunikasi dengan Database via Spring Data JPA
    ↓
Database (PostgreSQL)
```
