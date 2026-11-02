# 📱 MultiClone - Android App Cloner & Virtual Sandbox

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![Java](https://img.shields.io/badge/Java-8-orange.svg)](https://openjdk.java.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Material Design](https://img.shields.io/badge/Material-Design%203-blue.svg)](https://m3.material.io/)

A powerful, high-performance Android application cloner and privacy sandbox inspired by advanced app virtualization engines. MultiClone allows users to run multiple independent instances of any installed app simultaneously, spoof device identifiers per clone, modify virtual GPS locations, and secure hidden clones within an encrypted PIN vault.

---

## 🌟 Key Features (A to Z)

| Feature | Description |
|---|---|
| **App Cloning** | Create unlimited parallel instances of social media and gaming applications. |
| **App Selection** | Instant searchable package manager listing all installed third-party apps. |
| **Custom Identity** | Customize clone name, color accent badge, and custom icons. |
| **Device Privacy** | Per-clone hardware ID spoofing (IMEI, Android ID, MAC address, Model, Manufacturer). |
| **Fake GPS Location** | Inject mock latitude & longitude per clone with world presets (London, Tokyo, New York, etc.). |
| **Isolated Storage** | Separate tasks and preferences namespace per clone instance. |
| **Privacy Space Vault** | Hardware-backed keystore encrypted 4-digit PIN vault for confidential clones. |
| **VIP Subscription** | Built-in tiers (Monthly, Yearly, Lifetime) and feature gating interface. |
| **Dark & Light Mode** | Native Material 3 UI themes with dynamic accent colors. |

---

## 🏗️ Architecture Overview

```
                      ┌────────────────────────┐
                      │    MultiClone UI       │
                      │  (Activities & Adapters)│
                      └───────────┬────────────┘
                                  │
                                  ▼
                      ┌────────────────────────┐
                      │      CloneManager      │
                      │  (Orchestration Layer) │
                      └───────────┬────────────┘
                                  │
         ┌────────────────────────┼────────────────────────┐
         ▼                        ▼                        ▼
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│ Task Isolation  │      │ Device Profile  │      │  Location Spoofer│
│  (Custom Tasks  │      │  (Fake IMEI, ID,│      │ (Mock Lat/Long  │
│   & Affinities) │      │   MAC, Sensors) │      │   Injection)    │
└─────────────────┘      └─────────────────┘      └─────────────────┘
         │                        │                        │
         └────────────────────────┼────────────────────────┘
                                  ▼
                      ┌────────────────────────┐
                      │  Isolated App Runtime  │
                      └────────────────────────┘
```

---

## 📂 Project Structure

```
MultiClone/
├── app/
│   ├── build.gradle                 # App dependencies & SDK levels (21 - 34)
│   ├── src/main/
│   │   ├── AndroidManifest.xml      # Full permissions, launcher, and activities
│   │   ├── java/com/example/cloneapp/
│   │   │   ├── activities/          # 12 Activities (Main, Select, Privacy, GPS, Vault, VIP)
│   │   │   ├── adapters/            # RecyclerView adapters for clone grid & app picker
│   │   │   ├── models/              # CloneModel & DeviceProfile entities
│   │   │   └── utils/               # CloneManager, PrefsHelper, AppUtils
│   │   └── res/                     # Layouts, vector drawables, Material 3 themes & colors
├── build.gradle                     # Top-level build config
├── gradle.properties                # AndroidX & Jetifier flags
├── settings.gradle                  # Repository definitions
├── LICENSE                          # MIT License
└── README.md                        # Documentation
```

---

## 🚀 Getting Started

### Prerequisites
* **Android Studio** Hedgehog (2023.1.1) or newer
* **JDK:** OpenJDK 17 or 21
* **Android SDK:** Platform API 34, Build Tools 34.0.0

### Installation
1. Clone the repository or extract the ZIP:
   ```bash
   git clone https://github.com/your-username/MultiClone.git
   ```
2. Open **Android Studio** and select **Open an existing Project**.
3. Select the `MultiClone` folder.
4. Allow Gradle to sync dependencies.
5. Connect your device or start an emulator, then click **Run (Shift + F10)**.

---

## 🛡️ Security & Privacy Notice
* All sensitive credentials and vault PINs are secured via `androidx.security.crypto.EncryptedSharedPreferences`.
* Hardware spoofing is demonstrated for virtualization sandboxes and compatibility layers.

---

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
