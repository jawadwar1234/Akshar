# Changelog

All notable changes to the DyslexiaReader ("Akshar") project will be documented in this file.

## [1.0.0] - 2026-10-06

### Added
- Native Android architecture with Jetpack Compose, Material 3, Hilt DI, Coroutines & Flow.
- Offline-first Room Database local source of truth with background Firestore sync.
- Design System: Warm paper palette (`#FAF4E6`, `#2B2B2B`, Terracotta `#C85A32`), 5 dyslexia reading themes (Cream, White, Sepia, Charcoal, Pale Blue).
- Multilingual UI support in 5 languages: English, Hindi, Marathi, Gujarati, Bengali.
- Unicode-safe text engine with `BreakIterator` grapheme cluster preservation for Indic scripts.
- On-device ML Kit OCR for Latin & Devanagari; Tesseract Beta OCR for Gujarati & Bengali.
- CameraX capture module with orientation, focus, torch, and manual perspective cropping.
- Dyslexia Reader with font picker (Lexend, OpenDyslexic, Noto Sans), line/letter spacing, reading ruler focus overlay, and syllable/bionic fixation.
- Read-Aloud Text-To-Speech with synchronized karaoke-style highlighting and speed/pitch controls.
- Practice Mode ("Read Along") with SpeechRecognizer, fuzzy phonetic word matching, and praise-only Word Mastery bank.
- DPDP Act 2023 compliant parent/teacher account ownership, child profiles with 3-picture / 4-digit PINs (salted SHA-256), and data export/deletion.
- Full Unit Test suite for Unicode safety, PIN security, WordMatcher, and Sync conflict resolution.
