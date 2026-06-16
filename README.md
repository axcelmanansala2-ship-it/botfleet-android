# BotFleet Android

Native Android app for [BotFleet](https://github.com/axcelmanansala2-ship-it) — a Telegram bot hosting manager.

## Features

- **Login & Register** — secure session-based auth
- **Dashboard** — view all your bots with live status, CPU & RAM usage
- **Start / Stop / Restart** bots with one tap
- **Real-time Logs** — live log streaming via WebSocket
- **Upload Bots** — upload `.py` or `.zip` Python bot files
- **Auto-restart** toggle per bot
- **System Stats** — server CPU, memory, and disk
- **Settings** — configure your BotFleet server URL

## Download APK

Go to [Releases](../../releases) to download the latest APK.

Or go to [Actions](../../actions) → latest build → `botfleet-debug-apk` artifact.

## Setup

1. Install the APK on your Android device (Android 7.0+)
2. Open the app → tap **Settings** (gear icon on dashboard)
3. Enter your deployed BotFleet server URL (e.g. `https://your-app.replit.app`)
4. Sign in with your BotFleet credentials

## Tech Stack

- **Kotlin** + **Jetpack Compose**
- **Hilt** for dependency injection
- **Retrofit** + **OkHttp** for REST API & WebSocket
- **DataStore** for local session storage
- **Navigation Compose** for screen routing
- **Material 3** dark theme

## Build Locally

```bash
git clone https://github.com/axcelmanansala2-ship-it/botfleet-android
cd botfleet-android
./gradlew assembleDebug
# APK at: app/build/outputs/apk/debug/app-debug.apk
```

Requires JDK 17 and Android SDK.

## Server

The backend (FastAPI/Python) runs on Replit. Deploy it and copy the URL into the app's Settings screen.
