# Sahə — Ərazi ölçmə (Android)

Reklamsız, Azərbaycan dilində torpaq/sahə ölçmə tətbiqi. Brend: **SAHƏ**.

## Xüsusiyyətlər

- **Toxunaraq ölçmə** — xəritədə nöqtə basmaqla poliqon
- **Gəzərək ölçmə** — GPS ilə ətrafında yeriyərək
- **Vahidlər** — `m²`, `sot`, `put`, `ha` (seçilə bilən)
  - 1 sot = 100 m²
  - 1 put = 1000 m² (10 sot)
  - 1 ha = 10 000 m²
- **Hesab** — e-poçt/şifrə və Google (Firebase Auth)
- **Sinxron** — Room (lokal) + Firestore (bulud)
- **UI** — Azərbaycan dili, SAHƏ brendi

## Açmaq

1. Android Studio-da `saha/` qovluğunu açın
2. Firebase Console-da layihə yaradın:
   - Authentication → Email/Password + Google
   - Firestore Database
   - Android app: `az.saha.app`
3. `google-services.json` faylını `saha/app/` altına qoyun (placeholderı əvəz edin)
4. `strings.xml` içində `default_web_client_id` dəyərini Firebase Web client ID ilə dəyişin
5. `local.properties` yaradın:

```properties
sdk.dir=/path/to/Android/sdk
MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY
```

6. Google Cloud-da Maps SDK for Android aktiv edin
7. Firestore rules üçün `firestore.rules` faylına baxın

## Build

```bash
cd saha
./gradlew assembleDebug
./gradlew test
```

## Texniki stack

Kotlin · Jetpack Compose · Google Maps · Firebase Auth/Firestore · Room · Location Services
