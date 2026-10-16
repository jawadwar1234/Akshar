# Voice Commands & Speech Recognition Guide — Akshar Dyslexia Reader

## Overview
Akshar includes on-device voice control designed specifically for students with dyslexia and motor difficulties. Students can navigate text, control text-to-speech, resize fonts, and switch modes hands-free without relying on fine touch controls.

---

## Supported Voice Commands Table

| Action | English | Hindi (हिन्दी) | Marathi (मराठी) | Gujarati (ગુજરાતી) | Bengali (বাংলা) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Next Line** | "next line", "down", "forward", "scroll down" | "अगली पंक्ति", "आगे", "नीचे", "अगला" | "पुढची ओळ", "पुढे", "खाली" | "આગળની લીટી", "આગળ", "નીચે" | "পরের লাইন", "নিচে", "সামনে" |
| **Previous Line** | "previous line", "back", "up", "scroll up" | "पिछली पंक्ति", "पीछे", "ऊपर", "वापस" | "मागची ओळ", "मागे", "वर" | "પાછળની લીટી", "પાછળ", "ઉપર" | "আগের লাইন", "উপরে", "পেছনে" |
| **Read Aloud** | "read", "read to me", "play", "start reading" | "पढ़ो", "सुनाओ", "पढ़कर सुनाओ", "शुरू करो" | "वाचा", "वाचून दाखवा", "ऐकवा" | "વાંચો", "વાંચી સંभળાવો", "સાંભળાવો" | "পড়ুন", "পড়ে শোনাও", "শুরু করুন" |
| **Stop Reading** | "stop", "pause", "quiet", "hush" | "रुको", "रुक जाओ", "बंद करो", "शांत" | "थांबा", "बंद करा", "शांत" | "રોકો", "બંધ કરો", "શાંત" | "থামুন", "থামো", "বন্ধ করুন" |
| **Bigger Text** | "bigger text", "bigger", "larger", "zoom in" | "बड़ा करो", "बड़ा टेक्स्ट", "अक्षर बड़े करो" | "मोठे करा", "मोठा फॉन्ट", "अक्षरे मोठी करा" | "મોટું કરો", "મોટા અક્ષર" | "বড় করুন", "বড় লেখা" |
| **Smaller Text** | "smaller text", "smaller", "decrease font" | "छोटा करो", "छोटा टेक्स्ट", "अक्षर छोटे करो" | "लहान करा", "लहान फॉन्ट" | "નાનું કરો", "નાના અક્ષર" | "ছোট করুন", "ছোট লেখা" |
| **Scan Page** | "scan page", "scan", "take photo", "camera" | "फोटो खींचो", "स्कैन करो", "कैमरा खोलो" | "फोटो काढा", "स्कॅन करा", "कॅमेरा उघडा" | "ફોટો પાડો", "સ્કેન કરો" | "ছবি তুলুন", "স্ক্যান করুন" |
| **Open Library** | "open library", "library", "books", "home" | "लाइब्रेरी खोलो", "किताबें", "होम" | "वाचनालय उघडा", "पुस्तके", "होम" | "લાઇબ્રેરી ખોલો", "પુસ્તકો" | "লাইব্রেরি খুলুন", "বইগুলো" |

---

## Forgiving Matcher & Speech Dyspraxia Support
- **Normalized string cleaning**: Automatically strips punctuation and whitespace.
- **Synonym dictionary**: Accommodates natural speaking variations in all 5 languages.
- **Fuzzy Levenshtein distance matching**: Tolerates minor typos and mispronunciations (e.g., "nekxt line", "stoppp", "biger") with dynamic thresholding based on word length.

---

## Offline Speech Recognition on Android
Akshar requests offline recognition by setting `RecognizerIntent.EXTRA_PREFER_OFFLINE = true`.

### Offline Speech Pack Availability:
- **English (India - `en-IN`)**: Bundled / Available offline by default on Google Play Services and Android 8+.
- **Hindi (`hi-IN`)**: Free 1-click download available in Google Voice Recognition Offline Speech settings.
- **Marathi (`mr-IN`)**, **Gujarati (`gu-IN`)**, **Bengali (`bn-IN`)**: Supported via Android Google Speech Services. If offline model is not yet downloaded, Akshar displays a helpful guidance screen and directly offers a button to open **System Speech Settings**.

### Setup Steps for Offline Indian Languages:
1. Open device **Settings** $\rightarrow$ **System** $\rightarrow$ **Languages & Input**.
2. Select **Voice Input / Google Voice Typing** $\rightarrow$ **Offline speech recognition**.
3. Under **All**, download your preferred language pack (e.g. Hindi - 42 MB).

---

## Hands-Free Mode
- Keeps the microphone in a continuous listening loop.
- Automatically re-arms after executing a command or on silence timeout.
- Designed for learners with limited motor dexterity or physical textbook holding constraints.
