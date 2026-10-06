# Akshar — DyslexiaReader 📖

**DyslexiaReader** ("Akshar") is an offline-first, native Android application designed specifically to empower students with dyslexia (aged 7 to 17) in rural and semi-urban Indian schools to read printed textbooks effortlessly.

Built with **Kotlin**, **Jetpack Compose / Material 3**, **ML Kit on-device OCR**, **Tesseract**, **Room**, **Android TTS / SpeechRecognizer**, and **Firebase Sync**.

---

## 🌟 Core Features & Highlights

1. **On-Device Textbook Capture & OCR**:
   - CameraX integration with torch, focus tap, and perspective crop guide.
   - High-speed bundled ML Kit text recognition for **English**, **Hindi**, and **Marathi**.
   - Tesseract OCR (Beta) for **Gujarati** and **Bengali**.
2. **Dyslexia-Optimized Typography & Reader**:
   - Warm paper-like palette (`#FAF4E6` background, `#2B2B2B` charcoal text, terracotta `#C85A32` accent).
   - 5 Reading Color Themes: **Cream**, **Soft White**, **Sepia**, **Charcoal Night**, **Pale Blue**.
   - Typefaces: **Lexend** (UI), **OpenDyslexic** (Latin text), and **Noto Sans** (Devanagari, Gujarati, Bengali).
   - Reading Ruler / Line Focus overlay to prevent visual crowding.
   - Syllable coloring & bionic fixation assistance.
   - Never justified (left-aligned only) with generous 1.5x line height and customizable letter spacing.
3. **Karaoke-Style Read-Aloud (TTS)**:
   - Synchronized word-by-word visual highlighting as the native engine reads.
   - Pitch & speech rate adjustments (0.5x – 1.5x).
   - Graceful offline fallback with helpful voice pack guidance.
4. **Practice Mode ("Read-Along") & Word Mastery**:
   - Children read sentences aloud into the microphone.
   - Phonetic fuzzy matching with zero-pressure feedback (no timers, no countdowns, no scores out of 10).
   - Earns "Words You Have Mastered" to build reading confidence.
5. **Privacy & DPDP Act 2023 Compliance**:
   - Accounts owned by parents/teachers; children sign in with **3-Picture PIN** (from 9 friendly icons) or 4-digit PIN.
   - PINs stored with per-profile cryptographically random salt and SHA-256 hash.
   - 100% offline functionality. No analytics, no third-party trackers, no ads.
   - One-tap Data Export (JSON/TXT) and Account/Profile deletion ("Right to be Forgotten").
6. **5 Language UI**:
   - English, Hindi (हिन्दी), Marathi (मराठी), Gujarati (ગુજરાતી), Bengali (বাংলা).

---

## 🏗️ Architecture Diagram

```
+-------------------------------------------------------------------------+
|                              UI LAYER                                   |
|  Jetpack Compose + Material 3 (Warm Paper Theme / 5 Reader Themes)      |
|  Lexend | OpenDyslexic | Noto Sans (Devanagari / Gujarati / Bengali)    |
|  Screens: CameraCapture, ImageCrop, Reader, PracticeMode, Library, Auth |
+------------------------------------+------------------------------------+
                                     |
+------------------------------------+------------------------------------+
|                           VIEWMODEL LAYER                               |
|  ReaderViewModel, CameraViewModel, LibraryViewModel, AuthViewModel     |
|  StateFlow / SharedFlow / Coroutines (Memory Optimized for 2-3 GB RAM)  |
+------------------------------------+------------------------------------+
                                     |
+------------------------------------+------------------------------------+
|                            DOMAIN LAYER                                 |
|  - UnicodeIndicTextUtil (BreakIterator grapheme / word / sentence runs) |
|  - ScriptDetector (Devanagari, Gujarati, Bengali, Latin)                |
|  - WordMatcher (Phonetic & Levenshtein matching for speech)             |
|  - PinSecurityUtil (Salted PBKDF2 / SHA-256 Picture & Numeric PINs)    |
+------------------------------------+------------------------------------+
                                     |
+------------------------------------+------------------------------------+
|                             DATA LAYER                                  |
|  - Room Local DB (Single Source of Truth: Documents, Profiles, Words)   |
|  - DataStore (User Preferences: Font, Spacing, Theme, Ruler)            |
|  - ML Kit & Tesseract OCR Engines (On-Device Text Extraction)           |
|  - Android TTS & SpeechRecognizer (Audio Read-Aloud & Practice)         |
|  - Firebase Auth & Firestore Sync (Background Sync when Online)         |
+-------------------------------------------------------------------------+
```

---

## 🚀 Setup & Build Instructions

### Prerequisites
- JDK 17 (`JAVA_HOME` pointing to JDK 17)
- Android SDK with Platform 35 and Build Tools 35.0.0+
- Android Studio Ladybug / Meerkat or Command-Line Gradle

### Build Commands
```bash
# Clean and compile debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Run lint checks
./gradlew lintDebug
```

---

## 🧪 Unit Testing Coverage
- `UnicodeIndicTextUtilTest`: Verifies matra preservation, conjunct segmentation, and Indic boundary safety.
- `ScriptDetectorTest`: Verifies Latin, Devanagari, Gujarati, and Bengali script identification.
- `PinSecurityUtilTest`: Verifies password salting, picture PIN encoding, and verification.
- `WordMatcherTest`: Verifies speech recognition phonetic forgiveness and accent tolerance.
- `SyncConflictResolverTest`: Verifies offline-first timestamp-based last-write-wins merge strategy.
