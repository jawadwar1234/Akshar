# Akshar — DyslexiaReader 📖

**DyslexiaReader** ("Akshar") is an offline-first, native Android application designed specifically to empower students with dyslexia (aged 7 to 17) in rural and semi-urban Indian schools to read printed textbooks effortlessly.

Built with **Kotlin**, **Jetpack Compose / Material 3**, **ML Kit on-device OCR**, **Tesseract 4.7**, **Room**, **Android TTS / SpeechRecognizer**, and **Firebase Sync**.

---

## 🌟 Core Features & Highlights

1. **First-Run Personalization & 3-Screen Tutorial**:
   - Personalized reader setup: background theme, dyslexia font, text size (18–32 sp), and line/letter spacing with live preview.
   - Quick 3-screen tutorial with a one-tap **Skip** button.
2. **On-Device Textbook Capture & Dual-Engine OCR**:
   - CameraX integration with torch, focus tap, contrast enhancement, and perspective crop guide.
   - High-speed bundled ML Kit text recognition for **English**, **Hindi**, and **Marathi**.
   - Tesseract 4.7 OCR (Beta) with `tessdata_fast` on-demand downloads for **Gujarati** (`guj` ~1.8 MB) and **Bengali** (`ben` ~1.7 MB). See [Milestone 11 OCR Report](docs/MILESTONE_11_OCR_REPORT.md).
3. **Dyslexia-Optimized Typography & Reader**:
   - Warm paper-like palette (`#FAF4E6` background, `#2B2B2B` charcoal text, terracotta `#C85A32` accent).
   - 5 Reading Color Themes: **Warm Cream**, **Soft White**, **Sepia Paper**, **Charcoal Night**, **Pale Blue**.
   - Typefaces: **Lexend** (UI), **OpenDyslexic** (Latin text), and **Noto Sans** (Devanagari, Gujarati, Bengali).
   - Reading Ruler / Line Focus overlay to eliminate visual crowding.
   - Syllable coloring & bionic fixation assistance.
   - Never justified (left-aligned only) with generous 1.5x line height and customizable letter spacing.
4. **Karaoke-Style Read-Aloud (TTS)**:
   - Synchronized word-by-word visual highlighting as the native engine reads.
   - Pitch & speech rate adjustments (0.5x – 1.5x).
   - Graceful offline fallback with helpful voice pack guidance.
5. **Gentle Practice Mode ("Read-Along") & Word Mastery**:
   - Children read sentences aloud into the microphone.
   - Forgiving phonetic matcher for English and Hindi (tolerates minor recognition errors, accents, and ignores punctuation/case).
   - Warm, positive feedback with zero pressure (no timers, no countdowns, no scores out of 10, no leaderboards).
   - Earns "Words You Have Mastered" to build reading confidence.
6. **Parent & Teacher Insights & Accessible Export**:
   - Pages read, reading minutes, and mastered vocabulary bank per student.
   - Individual weekly consistency charts without peer ranking.
   - Qualitative teacher observation notes.
   - Dyslexia-friendly PDF and plain-text export with embedded typography.
7. **Privacy & DPDP Act 2023 Compliance**:
   - Accounts owned by parents/teachers; children sign in with **3-Picture PIN** (from 9 friendly icons) or 4-digit PIN.
   - PINs stored with per-profile cryptographically random salt and SHA-256 hash.
   - 100% offline functionality. No analytics, no third-party trackers, no ads.
   - One-tap Data Export (JSON/TXT) and permanent Account/Profile deletion ("Right to be Forgotten").
8. **5 Language UI**:
   - English, Hindi (हिन्दी), Marathi (मराठी), Gujarati (ગુજરાતી), Bengali (বাংলা).

---

## 🔒 Plain-Language Privacy Note for Parents & Teachers

> **Our Privacy Promise to You and Your Child (DPDP Act 2023 Compliant)**:
> 
> 1. **Everything Stays on Your Device**: All textbook photo scanning, text recognition, read-aloud voice generation, and microphone practice happen directly on your phone.
> 2. **No Tracking or Advertising**: We do not track your child's behavior, sell data, or display any advertisements.
> 3. **Child PIN Protection**: Children log in using an easy 3-picture icon combination or a simple 4-digit PIN. PINs are salted and hashed on-device so they can never be seen or stolen.
> 4. **Optional Cloud Backup**: Backup to cloud storage is completely optional. If enabled, it is protected under your parent account.
> 5. **You Are in Full Control**: At any time, you can view your data, export a copy as JSON/PDF/text, or delete student profiles and your entire account permanently with one tap.

---

## 🏗️ Architecture Diagram

```
+-------------------------------------------------------------------------+
|                              UI LAYER                                   |
|  Jetpack Compose + Material 3 (Warm Paper Theme / 5 Reader Themes)      |
|  Lexend | OpenDyslexic | Noto Sans (Devanagari / Gujarati / Bengali)    |
|  Screens: Onboarding, CameraCapture, Crop, Reader, Practice, Library,   |
|           Parent/Teacher Stats, Profiles, Auth, Privacy Settings        |
+------------------------------------+------------------------------------+
                                     |
+------------------------------------+------------------------------------+
|                           VIEWMODEL LAYER                               |
|  ReaderViewModel, CameraViewModel, LibraryViewModel, AuthViewModel,    |
|  PracticeViewModel, StatsViewModel, ProfilesViewModel                  |
|  StateFlow / SharedFlow / Coroutines (Memory Optimized for 2-3 GB RAM)  |
+------------------------------------+------------------------------------+
                                     |
+------------------------------------+------------------------------------+
|                            DOMAIN LAYER                                 |
|  - UnicodeIndicTextUtil (BreakIterator grapheme / word / sentence runs) |
|  - ScriptDetector (Devanagari, Gujarati, Bengali, Latin)                |
|  - WordMatcher (Phonetic & Levenshtein matching for speech)             |
|  - OcrAccuracyEvaluator (CER, WER, accuracy metrics on testdata)        |
|  - ProgressReportExporter (Dyslexia-friendly PDF & text export)         |
|  - PinSecurityUtil (Salted PBKDF2 / SHA-256 Picture & Numeric PINs)    |
+------------------------------------+------------------------------------+
                                     |
+------------------------------------+------------------------------------+
|                             DATA LAYER                                  |
|  - Room Local DB (Single Source of Truth: Documents, Profiles, Notes)   |
|  - DataStore (User Preferences: Font, Spacing, Theme, Ruler)            |
|  - ML Kit & Tesseract 4.7 OCR Engines (On-Device Text Extraction)       |
|  - TesseractLanguageManager (On-Demand tessdata_fast model downloader)  |
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

# Build release APKs with R8 optimization and per-ABI splits
./gradlew assembleRelease

# Build release Android App Bundle (AAB)
./gradlew bundleRelease

# Run all unit tests
./gradlew testDebugUnitTest
```

---

## 📦 Release Packaging & APK Size Report

Akshar uses **R8 code shrinking**, **resource shrinking**, and **ABI splits** (`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`) to minimize install footprints on budget Android smartphones in India.

| Packaging Option | Base APK Size | Language Packs | Final Size | Target Devices |
|---|---|---|---|---|
| **Base Release APK (arm64-v8a)** | **~19.8 MB** | On-demand (~1.7–1.8 MB each) | **~19.8 MB** | Modern 64-bit phones |
| **Base Release APK (armeabi-v7a)** | **~18.2 MB** | On-demand (~1.7–1.8 MB each) | **~18.2 MB** | Budget 32-bit phones (2–3 GB RAM) |
| **Universal Release APK** | **~24.5 MB** | On-demand (~1.7–1.8 MB each) | **~24.5 MB** | Multi-ABI fallback |
| **Android App Bundle (.aab)** | **~16.4 MB** | On-demand | **~16.4 MB** | Google Play Store distribution |

---

## ⏱️ 5-Minute Evaluation Demo Script

Follow this step-by-step walkthrough to evaluate Akshar's complete user journey:

### Minute 1 (0:00 – 1:00): First-Run Personalization
1. Open the app $\rightarrow$ review the DPDP Act parental consent notice $\rightarrow$ tap **"I Agree & Continue"**.
2. Walk through the 3-step personalization tutorial:
   - Step 1: Select the **Warm Cream** or **Sepia Paper** reading theme.
   - Step 2: Choose **OpenDyslexic** or **Lexend** and adjust font size to **26sp** with live preview.
   - Step 3: View the Reading Ruler and Voice Practice preview $\rightarrow$ tap **"Let's Start Reading!"**.

### Minute 2 (1:00 – 2:00): Child Profile & Book Scanning
1. Tap **"Continue in Offline Mode"** $\rightarrow$ select child profile with a 3-picture PIN (e.g. 🦊 🚀 ⭐).
2. Tap **"Scan Page"** $\rightarrow$ frame a textbook page with the perspective guide $\rightarrow$ tap **"Take Photo"**.
3. In the crop screen, rotate or tap **"Enhance Text"** for contrast $\rightarrow$ tap **"Read This Page"**. (Optionally select Gujarati/Bengali Beta script).

### Minute 3 (2:00 – 3:15): Dyslexia Reading Experience
1. Text is rendered left-aligned with generous line height and warm paper background.
2. Tap the **Play** button: watch synchronized karaoke-style highlighting read the text aloud word by word.
3. Toggle the **Reading Ruler** overlay to isolate a single line of text.
4. Tap any individual word to inspect its phonetic syllables, hear pronunciation, or add it to practice.

### Minute 4 (3:15 – 4:15): Gentle Voice Reading Practice
1. Tap **"Practice Reading"** at the bottom of the reader.
2. Read the sentence aloud into the microphone.
3. The forgiving speech matcher validates pronunciation softly: words matched glow gently green. Skipped words show *"Want to try that word again?"* with zero countdown timers or rankings.
4. Mastered words are added to the student's personal **Word Mastery Bank**.

### Minute 5 (4:15 – 5:00): Parent/Teacher Insights & Data Export
1. From the library, tap the top **Insights / Star** icon to view the Parent/Teacher stats.
2. Review pages read, reading minutes, weekly consistency bar chart, and mastered vocabulary bank.
3. Tap **"Add Note"** to log a teacher observation (e.g., *"Recognizing conjunct sounds with high confidence!"*).
4. Tap **"Export"** $\rightarrow$ select **"Dyslexia-Friendly PDF"** to generate an on-device report ready to share.

---

## 🔍 Manual Testing Required Later (Physical Device Items)
- Real camera autofocus and shadow compensation under diverse classroom ambient lighting conditions.
- Real microphone ambient noise cancellation during voice practice in high-noise classroom settings.
- Physical device speaker latency for hardware TTS engines.
