# Java Dasar untuk Spring Boot

Catatan ini berisi materi Java dasar yang perlu dikuasai sebelum masuk ke Spring Boot.
Tidak perlu menjadi ahli Java. Fokus pada konsep-konsep yang akan sering dipakai di Spring Boot.

---

## 1. Variable dan Tipe Data

### Tipe Data Primitif

```java
int umur = 25;
double harga = 99.99;
boolean aktif = true;
char huruf = 'A';
long jumlahBesar = 1000000000L;
float desimal = 3.14f;
```

### Tipe Data Referensi (Object)

```java
String nama = "Budi";
Integer angka = 100;       // wrapper class dari int
Double nilaiDouble = 99.9; // wrapper class dari double
Boolean status = false;    // wrapper class dari boolean
```

### Perbedaan Primitif dan Wrapper

```text
int     → Integer
double  → Double
boolean → Boolean
char    → Character
long    → Long
float   → Float
```

Wrapper class dipakai saat:

- Menyimpan data ke Collection (`List<Integer>`, bukan `List<int>`)
- Bisa bernilai `null` (primitif tidak bisa)
- Dibutuhkan oleh framework seperti Spring Boot

### Deklarasi Variabel

```java
// deklarasi dan inisialisasi langsung
String kota = "Jakarta";

// deklarasi dulu, isi nanti
String provinsi;
provinsi = "Jawa Barat";

// konstanta (tidak bisa diubah)
final String NEGARA = "Indonesia";
```

---

## 2. Percabangan (if/else)

### if/else Biasa

```java
int nilai = 80;

if (nilai >= 90) {
    System.out.println("A");
} else if (nilai >= 80) {
    System.out.println("B");
} else if (nilai >= 70) {
    System.out.println("C");
} else {
    System.out.println("D");
}
```

### Ternary Operator

```java
int umur = 20;
String status = (umur >= 17) ? "Dewasa" : "Anak-anak";
```

### Switch

```java
String hari = "Senin";

switch (hari) {
    case "Senin":
        System.out.println("Hari kerja");
        break;
    case "Sabtu":
    case "Minggu":
        System.out.println("Hari libur");
        break;
    default:
        System.out.println("Hari biasa");
}
```

### Switch Expression (Java 14+)

```java
String hari = "Senin";

String jenis = switch (hari) {
    case "Senin", "Selasa", "Rabu", "Kamis", "Jumat" -> "Kerja";
    case "Sabtu", "Minggu" -> "Libur";
    default -> "Tidak diketahui";
};
```

---

## 3. Perulangan (for, while)

### for Biasa

```java
for (int i = 0; i < 5; i++) {
    System.out.println("Angka: " + i);
}
```

### for-each (Enhanced for)

```java
String[] buah = {"Apel", "Mangga", "Jeruk"};

for (String b : buah) {
    System.out.println(b);
}
```

### while

```java
int i = 0;

while (i < 5) {
    System.out.println("Angka: " + i);
    i++;
}
```

### do-while

```java
int i = 0;

do {
    System.out.println("Angka: " + i);
    i++;
} while (i < 5);
```

### break dan continue

```java
for (int i = 0; i < 10; i++) {
    if (i == 3) continue; // lewati angka 3
    if (i == 7) break;    // berhenti di angka 7
    System.out.println(i);
}
// Output: 0 1 2 4 5 6
```

---

## 4. Method

### Method Dasar

```java
public class Kalkulator {

    // method tanpa return
    public void spiHalo() {
        System.out.println("Halo!");
    }

    // method dengan return
    public int tambah(int a, int b) {
        return a + b;
    }

    // method dengan parameter
    public String spiNama(String nama) {
        return "Halo, " + nama;
    }
}
```

### Method Static vs Non-Static

```java
public class MathUtil {

    // static: dipanggil tanpa membuat object
    public static int kuadrat(int x) {
        return x * x;
    }

    // non-static: harus membuat object dulu
    public int kali(int a, int b) {
        return a * b;
    }
}

// Penggunaan:
int hasil1 = MathUtil.kuadrat(5);       // static, langsung

MathUtil util = new MathUtil();
int hasil2 = util.kali(3, 4);           // non-static, perlu object
```

### Method Overloading

```java
public class Printer {

    public void cetak(String pesan) {
        System.out.println(pesan);
    }

    public void cetak(String pesan, int kali) {
        for (int i = 0; i < kali; i++) {
            System.out.println(pesan);
        }
    }

    public void cetak(int angka) {
        System.out.println(angka);
    }
}
```

Nama method sama, tapi parameter berbeda. Java memilih method berdasarkan parameter yang diberikan.

---

## 5. Class dan Object

### Class Sederhana

```java
public class User {
    // field (property)
    private String nama;
    private int umur;

    // constructor
    public User(String nama, int umur) {
        this.nama = nama;
        this.umur = umur;
    }

    // getter
    public String getNama() {
        return nama;
    }

    public int getUmur() {
        return umur;
    }

    // setter
    public void setNama(String nama) {
        this.nama = nama;
    }

    public void setUmur(int umur) {
        this.umur = umur;
    }

    // method
    public String info() {
        return nama + " (" + umur + " tahun)";
    }
}
```

### Membuat dan Menggunakan Object

```java
User user1 = new User("Budi", 25);
User user2 = new User("Ani", 22);

System.out.println(user1.getNama());  // Budi
System.out.println(user2.info());     // Ani (22 tahun)

user1.setUmur(26);
System.out.println(user1.getUmur());  // 26
```

### Access Modifier

```text
public    → bisa diakses dari mana saja
private   → hanya bisa diakses dari dalam class itu sendiri
protected → bisa diakses dari class yang sama dan subclass
default   → bisa diakses dari package yang sama (tanpa keyword)
```

Di Spring Boot, yang paling sering dipakai:

- `private` untuk field
- `public` untuk method, constructor, dan class

---

## 6. Constructor

### Default Constructor

```java
public class Produk {
    private String nama;
    private double harga;

    // default constructor (tanpa parameter)
    public Produk() {
        this.nama = "Unknown";
        this.harga = 0;
    }
}
```

### Parameterized Constructor

```java
public class Produk {
    private String nama;
    private double harga;

    public Produk(String nama, double harga) {
        this.nama = nama;
        this.harga = harga;
    }
}
```

### Multiple Constructor

```java
public class Produk {
    private String nama;
    private double harga;

    public Produk() {
        this("Unknown", 0);
    }

    public Produk(String nama) {
        this(nama, 0);
    }

    public Produk(String nama, double harga) {
        this.nama = nama;
        this.harga = harga;
    }
}

// Penggunaan:
Produk p1 = new Produk();                 // Unknown, 0
Produk p2 = new Produk("Buku");           // Buku, 0
Produk p3 = new Produk("Pensil", 5000);   // Pensil, 5000
```

---

## 7. Interface

Interface mendefinisikan kontrak method yang harus diimplementasikan oleh class.

### Membuat Interface

```java
public interface PaymentService {
    void bayar(double jumlah);
    double cekSaldo();
}
```

### Implementasi Interface

```java
public class BankPayment implements PaymentService {

    private double saldo = 1000000;

    @Override
    public void bayar(double jumlah) {
        saldo -= jumlah;
        System.out.println("Bayar via Bank: " + jumlah);
    }

    @Override
    public double cekSaldo() {
        return saldo;
    }
}

public class EwalletPayment implements PaymentService {

    private double saldo = 500000;

    @Override
    public void bayar(double jumlah) {
        saldo -= jumlah;
        System.out.println("Bayar via E-Wallet: " + jumlah);
    }

    @Override
    public double cekSaldo() {
        return saldo;
    }
}
```

### Menggunakan Interface

```java
PaymentService payment = new BankPayment();
payment.bayar(50000);

// bisa diganti implementasi tanpa ubah code lain
PaymentService payment2 = new EwalletPayment();
payment2.bayar(25000);
```

Kenapa penting di Spring Boot:

```text
Interface di Spring Boot dipakai untuk:
├── Service layer (UserService → UserServiceImpl)
├── Repository layer (JpaRepository)
└── Dependency Injection (Spring pilih implementasi otomatis)
```

---

## 8. Inheritance

Inheritance memungkinkan sebuah class mewarisi field dan method dari class lain.

### Contoh Inheritance

```java
// parent class
public class Hewan {
    protected String nama;

    public Hewan(String nama) {
        this.nama = nama;
    }

    public void suara() {
        System.out.println(nama + " bersuara");
    }
}

// child class
public class Kucing extends Hewan {

    public Kucing(String nama) {
        super(nama); // panggil constructor parent
    }

    @Override
    public void suara() {
        System.out.println(nama + " bersuara: Meow!");
    }
}

// child class lain
public class Anjing extends Hewan {

    public Anjing(String nama) {
        super(nama);
    }

    @Override
    public void suara() {
        System.out.println(nama + " bersuara: Guk!");
    }
}
```

### Penggunaan

```java
Hewan h1 = new Kucing("Mimi");
Hewan h2 = new Anjing("Bobby");

h1.suara(); // Mimi bersuara: Meow!
h2.suara(); // Bobby bersuara: Guk!
```

### Abstract Class

```java
public abstract class Kendaraan {
    protected String merk;

    public Kendaraan(String merk) {
        this.merk = merk;
    }

    // method abstract: harus diimplementasikan oleh child
    public abstract void jalan();

    // method biasa: bisa langsung dipakai
    public String getMerk() {
        return merk;
    }
}

public class Mobil extends Kendaraan {

    public Mobil(String merk) {
        super(merk);
    }

    @Override
    public void jalan() {
        System.out.println(merk + " melaju di jalan raya");
    }
}
```

---

## 9. Exception

### Try-Catch Dasar

```java
try {
    int hasil = 10 / 0;
} catch (ArithmeticException e) {
    System.out.println("Error: " + e.getMessage());
} finally {
    System.out.println("Selalu dijalankan");
}
```

### Multiple Catch

```java
try {
    String text = null;
    text.length();
} catch (NullPointerException e) {
    System.out.println("Null pointer: " + e.getMessage());
} catch (Exception e) {
    System.out.println("Error umum: " + e.getMessage());
}
```

### Throw dan Throws

```java
public class UserService {

    public User cariUser(Long id) throws Exception {
        if (id == null) {
            throw new IllegalArgumentException("ID tidak boleh null");
        }
        // cari user...
        return null;
    }
}
```

### Custom Exception

```java
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}

// Penggunaan:
public User cariUser(Long id) {
    // misalnya tidak ditemukan
    throw new UserNotFoundException("User dengan ID " + id + " tidak ditemukan");
}
```

Di Spring Boot, custom exception sering dipakai untuk error handling API:

```text
Client request
     ↓
Controller
     ↓
Service → throw UserNotFoundException
     ↓
ExceptionHandler → return 404 response
```

---

## 10. Collection

### List

```java
import java.util.ArrayList;
import java.util.List;

List<String> nama = new ArrayList<>();
nama.add("Budi");
nama.add("Ani");
nama.add("Citra");

// akses
System.out.println(nama.get(0));  // Budi
System.out.println(nama.size());  // 3

// loop
for (String n : nama) {
    System.out.println(n);
}

// hapus
nama.remove("Ani");

// cek ada atau tidak
boolean ada = nama.contains("Budi"); // true
```

### Map

```java
import java.util.HashMap;
import java.util.Map;

Map<String, Integer> umur = new HashMap<>();
umur.put("Budi", 25);
umur.put("Ani", 22);
umur.put("Citra", 30);

// akses
System.out.println(umur.get("Budi")); // 25

// loop
for (Map.Entry<String, Integer> entry : umur.entrySet()) {
    System.out.println(entry.getKey() + ": " + entry.getValue());
}

// cek key
boolean ada = umur.containsKey("Ani"); // true
```

### Set

```java
import java.util.HashSet;
import java.util.Set;

Set<String> kota = new HashSet<>();
kota.add("Jakarta");
kota.add("Bandung");
kota.add("Jakarta"); // duplikat, tidak masuk

System.out.println(kota.size()); // 2

for (String k : kota) {
    System.out.println(k);
}
```

### Kapan Pakai Apa

```text
List  → urutan penting, boleh duplikat     → daftar user, daftar produk
Map   → pasangan key-value                 → konfigurasi, lookup data
Set   → tidak boleh duplikat, urutan bebas  → daftar tag, daftar role
```

---

## 11. Generic

Generic memungkinkan class atau method bekerja dengan berbagai tipe data.

### Generic Class

```java
public class Kotak<T> {
    private T isi;

    public Kotak(T isi) {
        this.isi = isi;
    }

    public T getIsi() {
        return isi;
    }

    public void setIsi(T isi) {
        this.isi = isi;
    }
}

// Penggunaan:
Kotak<String> kotak1 = new Kotak<>("Buku");
Kotak<Integer> kotak2 = new Kotak<>(100);

String isi1 = kotak1.getIsi(); // "Buku"
Integer isi2 = kotak2.getIsi(); // 100
```

### Generic Method

```java
public class Util {

    public static <T> void cetak(T data) {
        System.out.println(data);
    }
}

Util.cetak("Halo");
Util.cetak(123);
Util.cetak(true);
```

### Di Spring Boot

Generic banyak dipakai di repository:

```java
// JpaRepository<Entity, TipeId>
public interface UserRepository extends JpaRepository<User, Long> {
}

public interface ProductRepository extends JpaRepository<Product, Long> {
}
```

---

## 12. Lambda Dasar

Lambda adalah cara singkat menulis anonymous function.

### Tanpa Lambda

```java
List<String> nama = List.of("Budi", "Ani", "Citra");

nama.forEach(new Consumer<String>() {
    @Override
    public void accept(String n) {
        System.out.println(n);
    }
});
```

### Dengan Lambda

```java
List<String> nama = List.of("Budi", "Ani", "Citra");

nama.forEach(n -> System.out.println(n));
```

### Lambda dengan Stream

```java
List<Integer> angka = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

// filter angka genap
List<Integer> genap = angka.stream()
    .filter(n -> n % 2 == 0)
    .toList();
// [2, 4, 6, 8, 10]

// map: transformasi data
List<String> namaUpper = List.of("budi", "ani")
    .stream()
    .map(n -> n.toUpperCase())
    .toList();
// ["BUDI", "ANI"]

// reduce: gabungkan jadi satu nilai
int total = angka.stream()
    .reduce(0, (a, b) -> a + b);
// 55
```

### Method Reference

```java
// lambda
nama.forEach(n -> System.out.println(n));

// method reference (lebih singkat)
nama.forEach(System.out::println);
```

---

## 13. Optional Dasar

Optional dipakai untuk menghindari `NullPointerException`.

### Tanpa Optional (Bahaya)

```java
User user = cariUser(1L);
System.out.println(user.getNama()); // bisa NullPointerException!
```

### Dengan Optional (Aman)

```java
import java.util.Optional;

Optional<User> user = cariUser(1L);

// cara 1: isPresent
if (user.isPresent()) {
    System.out.println(user.get().getNama());
} else {
    System.out.println("User tidak ditemukan");
}

// cara 2: ifPresent
user.ifPresent(u -> System.out.println(u.getNama()));

// cara 3: orElse (nilai default)
User u = user.orElse(new User("Guest", 0));

// cara 4: orElseThrow
User u2 = user.orElseThrow(() ->
    new UserNotFoundException("User tidak ditemukan")
);
```

### Membuat Optional

```java
// dari nilai yang ada
Optional<String> opt1 = Optional.of("Budi");

// dari nilai yang mungkin null
Optional<String> opt2 = Optional.ofNullable(null);

// kosong
Optional<String> opt3 = Optional.empty();
```

### Di Spring Boot

Optional banyak dipakai di repository:

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

// di service:
User user = userRepository.findByEmail("budi@email.com")
    .orElseThrow(() -> new UserNotFoundException("Email tidak ditemukan"));
```

---

## Rangkuman

```text
Konsep Java          Dipakai di Spring Boot untuk
────────────────────────────────────────────────────
Variable & Tipe Data  Field entity, DTO, parameter
if/else               Logic di service layer
for/while             Proses batch, loop data
Method                Controller, service method
Class & Object        Entity, DTO, service, controller
Constructor           Dependency injection, inisialisasi
Interface             Service layer, repository
Inheritance           Base entity, custom exception
Exception             Error handling API
Collection            List data, response API
Generic               Repository, ResponseEntity
Lambda                Stream processing, callback
Optional              Null safety di repository
```
