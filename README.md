# CRMP 3D Sandbox RP — Стартовая архитектура проекта

Готовый каркас мобильной многопользовательской 3D-игры в постсоветском сеттинге по вашей спецификации:
1. **Backend API (`backend_api/`)** — Node.js + Express + PostgreSQL + JWT (авторизация, версии/патчи, список серверов, профиль персонажа).
2. **Database (`backend_api/sql/schema.sql`)** — PostgreSQL-схема: аккаунты, персонажи, транспорт, недвижимость, бизнесы, фракции, сервера.
3. **Android Launcher (`android_launcher/`)** — Kotlin (Android 10–13 / API 29–33): регистрация, вход, проверка версии игры, выбор сервера и передача JWT-токена в Unity-клиент.
4. **Unity 2022 LTS Scripts (`unity_client/Assets/Scripts/`)** — C#-скрипты: приём токена из лаунчера, загрузка профиля, сенсорное управление от 3-го лица, цикл дня/ночи и погода, экономика и система розыска.
5. **CI/CD (`.github/workflows/build-launcher.yml`)** — автосборка `.apk` лаунчера в облаке через GitHub Actions.

---

## Быстрый старт

### 1. База данных (PostgreSQL)
```bash
psql -U postgres -f backend_api/sql/schema.sql
```

### 2. Backend API (Node.js)
```bash
cd backend_api
cp .env.example .env
npm install
npm start
```
API поднимется на `http://localhost:3000`.

### 3. Android-лаунчер (Android 10–13)
- Откройте `android_launcher/` в Android Studio (или залейте репозиторий на GitHub — сработает `.github/workflows/build-launcher.yml`).
- В `ApiService.kt` укажите URL вашего API (`http://10.0.2.2:3000` для эмулятора Android или реальный домен/IP).

### 4. Unity-клиент (Unity 2022 LTS)
- Создайте 3D (URP Mobile) проект в Unity 2022 LTS и скопируйте папку `unity_client/Assets/Scripts/` в `Assets/Scripts/` вашего проекта.
- Для мультиплеера на 150–300 игроков подключите **Mirror** или **FishNet** через Package Manager.
