# Environment Monitor - Projekt Semestralny

Aplikacja mobilna na system Android służąca do monitorowania parametrów środowiskowych. Program umożliwia pomiar natężenia hałasu, rejestrację lokalizacji GPS oraz dokumentację fotograficzną miejsc pomiarowych.

## 📱 Funkcje aplikacji
* **Pomiar hałasu w czasie rzeczywistym:** Monitorowanie poziomu decybeli (dB) przy użyciu mikrofonu.
* **Lokalizacja GPS:** Automatyczne pobieranie współrzędnych geograficznych (Latitude, Longitude) dla każdego pomiaru.
* **Dokumentacja foto:** Możliwość wykonania zdjęcia miejsca pomiaru.
* **Baza danych (Room):** Trwałe przechowywanie pomiarów wraz z datą i godziną.
* **Integracja z Mapami Google:** Kliknięcie w pomiar w historii otwiera lokalizację bezpośrednio w aplikacji Google Maps.
* **Eksport danych:** Generowanie tekstowego raportu z pomiarów i możliwość wysłania go e-mailem.

## 🛠️ Wykorzystane sensory i funkcje
Zgodnie z wymaganiami projektu, aplikacja wykorzystuje 3 źródła danych:
1. **Mikrofon:** Analiza amplitudy dźwięku w celu wyliczenia poziomu decybeli.
2. **Lokalizacja (Fused Location Provider):** Precyzyjne określanie położenia użytkownika.
3. **Aparat fotograficzny:** Rejestracja obrazu powiązanego z danymi sensorów.

## 🏗️ Architektura i technologie
* **Język:** Kotlin
* **UI:** Jetpack Compose (Modern Toolkit)
* **Nawigacja:** Navigation Compose (Type-safe routes)
* **Zarządzanie stanem:** ViewModel + UiState (Flow)
* **Baza danych:** Room Persistence Library
* **Uprawnienia:** Pełna obsługa systemowych uprawnień Runtime (Camera, Location, Audio).

## 📸 Zrzuty ekranu
Znajdują się 

## 🚀 Instrukcja uruchomienia
1. Sklonuj repozytorium.
2. Otwórz projekt w **Android Studio (Ladybug lub nowsza)**.
3. Uruchom aplikację na fizycznym urządzeniu lub emulatorze.
4. Zaakceptuj wymagane uprawnienia przy pierwszym uruchomieniu, aby sensory mogły zbierać dane.

## 📦 Plik APK
Gotowy plik instalacyjny znajduje się w folderze `app/release/`.

---
*Projekt zrealizowany w ramach laboratorium: Programowanie urządzeń mobilnych.*
