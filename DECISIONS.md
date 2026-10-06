# Architectural & Technical Decisions Record (ADR)

## Decision 1: Jetpack Compose with Material 3 vs XML Views for Low-End Devices (2-3 GB RAM)
- **Context**: Low-end Indian Android devices have tight memory budgets (2-3 GB RAM).
- **Decision**: Use Jetpack Compose with Material 3, enhanced with strict performance constraints:
  - Stable models with `@Immutable` / `@Stable` data classes to avoid unnecessary recompositions.
  - Zero expensive graphic effects (no blurs, no complex glassmorphism, flat surfaces with 1dp borders).
  - Derived state and `remember` caching for text layout measurements and BreakIterator grapheme runs.
  - ABI splits (`armeabi-v7a`, `arm64-v8a`) to minimize APK download size and memory overhead.
  - Compose provides superior dynamic styling needed for dyslexia customizers (font family switching between Lexend, OpenDyslexic, Noto Sans, letter spacing, line height, reading ruler overlay, karaoke-style word highlighting).

## Decision 2: Unicode-Safe Indic Text Handling (BreakIterator over Raw Substrings)
- **Context**: Indic scripts (Devanagari, Gujarati, Bengali) make heavy use of conjuncts (उदा. क्त, त्र, ज्ञ, र्फ) and dependent vowel matras (ि, ी, ु, ू, etc.). Splitting by `char` or simple `String.substring` slices vowels off their base consonants, corrupting the script and confusing dyslexic children.
- **Decision**: Text processing must strictly use `java.text.BreakIterator`:
  - `BreakIterator.getCharacterInstance(locale)` for grapheme cluster boundaries.
  - `BreakIterator.getWordInstance(locale)` for word segmentation and practice mode matching.
  - `BreakIterator.getSentenceInstance(locale)` for TTS sentence chunking.

## Decision 3: Script Detection and Dynamic Typography
- **Context**: Dyslexic readers require specific font characteristics (heavy bottoms, distinct letter shapes, open counters). OpenDyslexic is optimal for Latin characters, but does not support Indic scripts. Lexend is optimal for UI, while Google Noto Sans (Devanagari, Gujarati, Bengali) provides highest clarity for Indic scripts.
- **Decision**: Implement dynamic script detection using Unicode block ranges (`\u0900-\u097F` Devanagari, `\u0A80-\u0AFF` Gujarati, `\u0980-\u09FF` Bengali, `\u0000-\u024F` Latin) to automatically select the optimal typeface per document and paragraph.

## Decision 4: Offline-First Local Source of Truth
- **Context**: Users in rural and semi-urban areas experience intermittent or zero internet connectivity.
- **Decision**: Room is the single local source of truth. All capture, OCR, reading, TTS, word mastery, and settings work 100% offline. Network is used exclusively for Firebase Auth and Firestore background sync when connected.

## Decision 5: DPDP Act 2023 Compliance & Zero Child Passwords
- **Context**: Target users are children aged 7-17. The Indian Digital Personal Data Protection (DPDP) Act 2023 mandates verifiable parental/guardian consent and prohibits behavioural tracking of minors.
- **Decision**:
  - Clear, transparent first-run parental consent screen.
  - Accounts owned by parents/teachers; children use 3-picture PINs (from 9 friendly icons) or 4-digit numeric PINs.
  - PINs stored securely using PBKDF2/SHA-256 with per-profile cryptographically random salt.
  - No analytics, no ads, no trackers. Export and account deletion ("Right to be Forgotten") fully supported.
