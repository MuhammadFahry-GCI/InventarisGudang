# 📦 InventarisGudang — Sistem Inventaris Barang.

Aplikasi desktop sederhana berbasis **JavaFX + Maven** untuk membantu pengelolaan data inventaris barang secara lebih rapi dan terstruktur menggunakan database MySQL.

---

## ✨ Fitur Utama

- ➕ Tambah data barang
- ✏️ Edit data barang
- ❌ Hapus data barang
- 📋 Menampilkan daftar inventaris
- 🔍 Pencarian data barang
- 💾 Penyimpanan data menggunakan MySQL

---

## 🛠️ Teknologi yang Digunakan

| Teknologi | Keterangan |
|---|---|
| Java 17 | Bahasa pemrograman utama |
| JavaFX | Framework antarmuka desktop |
| Maven | Dependency management |
| MySQL | Database |
| JDBC | Koneksi database |

---

## 📁 Struktur Proyek

```text
InventarisGudang/
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

## ⚙️ Persyaratan

Sebelum menjalankan aplikasi, pastikan sudah menginstal:

- Java JDK 17+
- Maven 3.6+
- MySQL / XAMPP
- IDE seperti IntelliJ IDEA atau VS Code

---

## 🚀 Cara Menjalankan

### 1️⃣ Clone Repository

```bash
git clone https://github.com/MuhammadFahry-GCI/InventarisGudang.git
cd InventarisGudang
```

---

### 2️⃣ Import Database

Jalankan file SQL melalui phpMyAdmin atau MySQL CLI:

```sql
source inventaris.sql
```

---

### 3️⃣ Konfigurasi Database

Buka file:

```text
DBConnection.java
```

Lalu sesuaikan konfigurasi berikut:

```java
private static final String URL  = "jdbc:mysql://localhost:3306/inventaris_db";
private static final String USER = "root";
private static final String PASS = "";
```

---

### 4️⃣ Jalankan Aplikasi

```bash
mvn javafx:run
```

---

## 📋 Menu Aplikasi

| Menu | Fungsi |
|---|---|
| Dashboard | Menampilkan ringkasan data |
| Barang | Mengelola data inventaris |
| Database | Penyimpanan data barang |

---

## 🧱 Arsitektur Aplikasi

```text
UI (JavaFX) → DAO → DBConnection → MySQL
```

- **UI** → Menangani tampilan aplikasi
- **DAO** → Operasi CRUD database
- **DBConnection** → Koneksi database
- **MySQL** → Penyimpanan data

---

## 🎨 Tampilan Aplikasi

Aplikasi dibuat dengan tampilan sederhana, modern, dan mudah digunakan untuk membantu pengelolaan inventaris barang.

---

## 👨‍💻 Developer

Dikembangkan sebagai project pembelajaran Java Desktop menggunakan JavaFX dan MySQL.

🔗 Repository GitHub:  
https://github.com/MuhammadFahry-GCI/InventarisGudang

---

## 📄 Lisensi

Project ini dibuat untuk keperluan pembelajaran dan pengembangan aplikasi desktop Java.