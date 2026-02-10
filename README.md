# 🍽️ SnapCal — AI-Powered Nutrition Tracker

> **Platform:** Android (Mobile) & Web  
> **Min SDK:** 26 (Android 8.0+)

SnapCal is a nutrition tracking app that uses AI and computer vision to analyze your food from photos. Snap a pic, get the nutritional breakdown, track your daily intake, and receive personalized food recommendations — all powered by YOLO object detection, Google Gemini, and Groq.

📖 **[Baca dalam Bahasa Indonesia](README_ID.md)**

---

## ✨ Features

- **📸 AI Food Analysis** — Take a photo or pick from gallery, and the AI will identify the food and estimate its nutritional content.
- **🤖 Dual AI Pipeline** — YOLO model for fast food detection, with Google Gemini / Groq as fallback for more complex analysis.
- **📊 Nutrition Tracking** — Track calories, carbs, protein, fat, fiber, sugar, and more on a daily/weekly basis.
- **🍳 Meal Recommendations** — Get personalized breakfast, lunch, and dinner suggestions based on your profile and goals.
- **💬 AI Chat** — Chat with an AI nutritionist for food and diet advice.
- **👤 User Profiles** — Set your health goals, dietary preferences, allergies, and activity level.
- **✏️ Manual Entry** — Add food items manually if you prefer.
- **📅 Calendar View** — Browse your food history by date.
- **📢 Announcements** — Stay updated with app announcements and tips.
- **🔔 Push Notifications** — Firebase Cloud Messaging support.

---

## 🏗️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin |
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **Navigation** | Jetpack Navigation Compose |
| **Networking** | Retrofit 2 + OkHttp 4 |
| **Local DB** | Room |
| **Image Loading** | Coil |
| **Camera** | CameraX |
| **Auth** | Firebase Authentication |
| **Push Notifications** | Firebase Cloud Messaging |
| **Remote Config** | Firebase Remote Config |
| **Charts** | MPAndroidChart |
| **AI / ML** | YOLO (custom model), Google Gemini, Groq |
| **Build System** | Gradle (Kotlin DSL) + Version Catalog |

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Ladybug (2024.2.1) or newer
- **JDK 11+**
- **Android SDK 35**
- A Firebase project with Authentication enabled
- API keys for your backend service

### Setup

1. **Clone the repo**
   ```bash
   git clone https://github.com/farelyudapratama/SnapCal-Firebase-Gemini.git
   cd SnapCal-Firebase-Gemini
   ```

2. **Firebase setup**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/).
   - Enable **Email/Password** authentication.
   - Download `google-services.json` and place it in `app/`.

3. **Backend API**
   - The app connects to a backend API for food analysis, recommendations, and chat.
   - Update the base URL in `ApiConfig.kt` or via Firebase Remote Config.

4. **Build & Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or just hit ▶️ in Android Studio.

---

## 🔧 Architecture Overview

The app follows the **MVVM** pattern with a clean separation of concerns:

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   View       │===> │  ViewModel   │====>│  Repository  │
│ (Composable) │<====│  (StateFlow) │<====│  (API/Room)  │
└──────────────┘     └──────────────┘     └──────────────┘
```

**Key patterns:**
- **StateFlow** for reactive state management (migrated from LiveData).
- **Sealed classes** (`AuthState`, `ProfileState`, `RecommendationState`) for type-safe UI state.
- **Unified ViewModelFactory** for dependency injection across all ViewModels.
- **Repository pattern** — ViewModels never call `ApiService` directly.
- **Token caching** with automatic refresh via OkHttp `Authenticator`.

---

## 🔑 API & Authentication Flow

1. User logs in via **Firebase Authentication** (email/password).
2. Firebase returns a JWT token.
3. The token is cached and attached to every API request via OkHttp interceptor.
4. On 401 responses, the `Authenticator` automatically refreshes the token.
5. All API calls go through `ApiRepository` → `ApiService` → Backend.

---

## 🤖 AI Analysis Pipeline

When a user takes a food photo:

1. **YOLO Detection** — The image is sent to the backend, which runs a custom YOLO model for food object detection.
2. **Gemini / Groq Fallback** — If YOLO doesn't detect anything, the backend falls back to Google Gemini or Groq for AI-powered analysis.
3. **Nutrition Estimation** — The AI returns estimated nutritional values (calories, macros, etc.).
4. **User Review** — The user can review, edit, and save the analysis.

---

## 📝 Contributing

1. Fork the repo.
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add amazing feature'`
4. Push: `git push origin feature/amazing-feature`
5. Open a Pull Request.

### Code Style

- Keep composables in `ui/components/` if they're reusable or in `view/` if they're screen-specific.
- All new ViewModels should use `ApiRepository` (not `ApiService` directly) and register in `ViewModelFactory`.
- Use `StateFlow` for state management, not `LiveData`.
- Error messages should be in English (localization happens at the UI layer via string resources).

---

**Thank you for using SnapCal!**
