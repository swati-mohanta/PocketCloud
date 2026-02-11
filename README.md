# 🖥 PocketCloud — Provider Mode

Provider Mode turns an Android device into a compute node that contributes
processing power to the PocketCloud network.

The provider continuously polls the backend, executes assigned tasks locally,
submits results, and earns tokens.

------------------------------------------------------------

## 🔧 Installation

Backend Requirements:
- Java 17+
- Maven
- Local Wi-Fi / LAN network

Android Requirements:
- Android Studio (latest stable)
- Android phone or emulator
- USB cable (for physical device)
- Backend and phone must be on the same network

------------------------------------------------------------

##  Run Backend Locally

Navigate to backend directory
cd pocketcloud-backend

Start Spring Boot backend
mvn spring-boot:run

Verify backend is running
http://<YOUR_LOCAL_IP>:3000/health

Expected response
PocketCloud backend is running

------------------------------------------------------------

## 🌐 Find Your Local IP Address

macOS / Linux
ifconfig | grep inet

Example output
inet 192.168.1.10

Windows
ipconfig

Example output
IPv4 Address : 192.168.1.10

------------------------------------------------------------

## ⚙️ Configure Backend IP in Android App

File location
app/src/main/java/com/xplora/pocketcloud/network/ServerConfig.kt

Edit ServerConfig.kt

object ServerConfig {

    fun baseUrl(isEmulator: Boolean): String {
        return if (isEmulator) {
            "http://10.0.2.2:3000"
        } else {
            "http://192.168.1.10:3000"
        }
    }
}

Important notes
10.0.2.2  -> Android Emulator
LAN IP   -> Real Android device

------------------------------------------------------------

## 📱 Run Provider Mode on Android

Using Emulator
- Start Android Emulator
- Run the app
- Select Provider Mode

Using Physical Device (Recommended)
- Enable Developer Options
- Enable USB Debugging
- Connect phone via USB
- Run app from Android Studio
- Select Provider Mode

------------------------------------------------------------

## ⚠️ Required Android Settings (VERY IMPORTANT)

Disable battery optimization
Settings → Battery → App usage → PocketCloud → Unrestricted

Enable auto-start (MIUI / Realme / Oppo)
Settings → Apps → PocketCloud → Auto-start → Enable

------------------------------------------------------------

## 🔄 How Provider Mode Works

Provider polls backend
→ Receives chunk
→ Processes locally
→ Submits result
→ Earns tokens

------------------------------------------------------------

## 🧪 Verify Provider Is Running

Android Logcat (Filter: PocketCloud-Provider)
ProviderService INSTANCE CREATED
onStartCommand CALLED
Polling THREAD STARTED
No chunk available

Backend logs
GET /provider/next/{providerId}

------------------------------------------------------------

## ▶️ Test Provider with a Consumer Task

- Open app on another device
- Select Consumer Mode
- Start Image Processing or Prime Benchmark

Provider log example
Chunk received | task=prime
Chunk submitted

------------------------------------------------------------

## 🛑 Stop Provider Mode

- Open Provider screen
- Tap Stop Provider

------------------------------------------------------------

## ❌ Common Issues

Provider not receiving tasks
- Check backend IP in ServerConfig.kt
- Ensure same Wi-Fi network
- Verify /health endpoint

Works on emulator but not real phone
- Use LAN IP instead of 10.0.2.2
- Disable battery optimization
- Enable auto-start

------------------------------------------------------------

