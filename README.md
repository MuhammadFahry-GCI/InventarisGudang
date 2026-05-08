# 📦 Sistem Inventaris Barang Sederhana

Aplikasi desktop sederhana berbasis **JavaFX + Maven** untuk membantu pengelolaan data inventaris barang secara lebih rapi dan terstruktur.

---

# ✨ Fitur Utama

* Tambah data barang
* Edit data barang
* Hapus data barang
* Menampilkan daftar inventaris
* Penyimpanan data menggunakan MySQL

---

# 🛠️ Teknologi yang Digunakan

| Teknologi | Keterangan                |
| --------- | ------------------------- |
| Java 17   | Bahasa pemrograman utama  |
| JavaFX    | Tampilan desktop aplikasi |
| Maven     | Dependency management     |
| MySQL     | Database                  |
| JDBC      | Koneksi database          |

---

# 📁 Struktur Proyek

```text
inventaris-app/
├── pom.xml
└── src/main/java/com/inventaris/
    ├── Main.java
    ├── db/
    │   └── DBConnection.java
    ├── model/
    │   └── Barang.java
    ├── dao/
    │   └── BarangDAO.java
    └── ui/
        ├── MainScene.java
        └── BarangPane.java
```

---

# ⚙️ Persyaratan

* Java JDK 17+
* Maven 3.6+
* MySQL / XAMPP

---

# 🚀 Cara Menjalankan

## 1. Import Database

Jalankan file SQL:

```sql
source inventaris.sql
```

---

## 2. Atur Koneksi Database

Edit file:

```text
DBConnection.java
```

```java
private static final String URL  = "jdbc:mysql://localhost:3306/inventaris_db";
private static final String USER = "root";
private static final String PASS = "";
```

---

## 3. Jalankan Aplikasi

```bash
mvn javafx:run
```

---

# 📋 Fitur Inventaris

| Menu     | Fungsi                    |
| -------- | ------------------------- |
| Barang   | Mengelola data inventaris |
| Database | Menyimpan data barang     |

---

# 🧱 Arsitektur Aplikasi

```text
UI (JavaFX) → DAO → DBConnection → MySQL
```

---

# 👨‍💻 Developer

Project sederhana Java Desktop menggunakan JavaFX dan MySQL.
