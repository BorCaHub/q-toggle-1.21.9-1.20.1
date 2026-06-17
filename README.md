# Q Toggle (Drop Lock) — Mod Fabric untuk Minecraft 1.20.x & 1.21.x

## Fungsi Mod
- Tombol **Q** secara default **TIDAK** akan drop item (aman dari drop tidak sengaja saat PvP).
- Ada **keybind toggle** (default: **G**) untuk menyalakan/mematikan mode drop:
  - **OFF (default)** → Q dikunci, tidak drop apapun.
  - **ON** → Q berfungsi normal seperti vanilla (drop item).
- Status ON/OFF ditampilkan sebagai teks singkat di **action bar** (atas hotbar) setiap kali toggle ditekan.
- Keybind toggle bisa diganti di **Options > Controls > Drop Toggle**.

---

## Versi yang Didukung

| Minecraft | fabric_version          | loader_version | loom_version | Java |
|-----------|-------------------------|----------------|--------------|------|
| 1.21.4    | `0.114.0+1.21.4`        | 0.16.10        | 1.9.2        | 21   |
| 1.21.1    | `0.116.5+1.21.1`        | 0.16.10        | 1.9.2        | 21   |
| 1.21      | `0.100.1+1.21`          | 0.15.11        | 1.7.4        | 21   |
| 1.20.6    | `0.100.8+1.20.6`        | 0.15.10        | 1.6.12       | 21   |
| 1.20.4    | `0.97.3+1.20.4`         | 0.15.6         | 1.6.12       | 21   |
| 1.20.2    | `0.91.6+1.20.2`         | 0.15.1         | 1.4.2        | 17+  |
| 1.20.1    | `0.92.2+1.20.1`         | 0.14.21        | 1.4.2        | 17   |

> Cek versi terbaru di: https://fabricmc.net/develop

---

## Yang Dibutuhkan untuk Compile

- **JDK 21** (untuk semua versi di atas; 1.20.1 bisa pakai JDK 17)
- Koneksi internet (Maven Fabric + Mojang)

---

## Cara Ganti Versi Minecraft Target

Buka `gradle.properties`, ganti 3 baris ini sesuai tabel di atas:

```properties
minecraft_version=1.21.4
fabric_version=0.114.0+1.21.4
loom_version=1.9.2
```

Contoh untuk 1.20.1:
```properties
minecraft_version=1.20.1
fabric_version=0.92.2+1.20.1
loom_version=1.4.2
```
Untuk 1.20.1, juga ganti `it.options.release = 21` menjadi `17` di `build.gradle`,
dan `java-version: '21'` menjadi `'17'` di workflow.

---

## Langkah Compile

```bash
# Linux / macOS
./gradlew build

# Windows
gradlew.bat build
```

Hasil jar ada di `build/libs/qtoggle-1.0.0.jar`.

---

## Instalasi di Minecraft

1. Pasang **Fabric Loader** ≥ 0.15.0 dan **Fabric API** sesuai versi MC.
2. Copy `qtoggle-1.0.0.jar` ke folder `mods`.
3. Jalankan game → **Options > Controls > Drop Toggle** untuk cek/ganti keybind toggle (default: **G**).

---

## Perbedaan Teknis Versi 26.1 vs 1.20.x–1.21.x

| Aspek                    | Versi 26.1 (proyek lama)              | Versi 1.20.x / 1.21.x (proyek ini)         |
|--------------------------|---------------------------------------|---------------------------------------------|
| Java                     | 25                                    | 21                                          |
| Gradle                   | 9.x                                   | 8.x (Loom 1.6–1.9)                         |
| Keybind helper           | `KeyMappingHelper`                    | `KeyBindingHelper`                          |
| Kategori keybind         | `KeyMapping.Category.register()`      | String langsung di konstruktor `KeyMapping` |
| `modImplementation`      | `implementation` (no remap)           | `modImplementation` (Loom remap)            |
| Mappings                 | Tidak perlu (MC 26.1 unobfuscated)    | `loom.officialMojangMappings()` wajib ada   |
| settings.gradle          | Butuh `dependencyResolutionManagement`| Tidak perlu (Loom inject otomatis)          |

---

## Kompatibilitas Server
Mod ini **client-side only** (`"environment": "client"`) — aman di server manapun.

---

## Kalau Ada Error Compile

- **`KeyBindingHelper` tidak ditemukan** → pastikan `modImplementation` Fabric API sudah benar, bukan `implementation`.
- **`keyDrop` tidak ditemukan** → di 1.20.2+ namanya `keyDrop`; di 1.20.1 mungkin masih `keyDrop` (sama). Jika beda, buka IntelliJ dan lihat `Options` class.
- **`setOverlayMessage` tidak ada** → di versi sangat lama (1.18-) pakai `sendMessage` atau `setOverlayMessage` di `Hud` class.
- Gunakan **IntelliJ IDEA + plugin Minecraft Development** untuk autocomplete nama field/method yang benar.
