# EZ LDer

A lightweight lucid dreaming app for old Android phones (API 16+, 512MB+ RAM).

Big buttons, fast response, no bloat.

## Features

- **Dream Diary** — write dreams, voice-to-text toggle (Android SpeechRecognizer), saves as JSON .txt files
- **View Previous** — browse, edit, and update past entries
- **Reality Checks** — leave the diary open 25s: "how realistic is this?" → "what do you remember?" — two quick reality checks to build lucidity
- **Dream Parser** — keyword-matches 16 common dream themes (falling, flying, chased, teeth, water, death, etc.) with percentage bars
- **CSV Export** — save all dreams as CSV via Storage Access Framework or clipboard
- **No dependencies** — pure Android Java, Holo theme, no third-party libraries

## Build

```bash
git clone <repo-url>
cd EZ-LDer
./gradlew assembleDebug
```

APK at `app/build/outputs/apk/debug/app-debug.apk`

## Requirements

- Android 4.1+ (API 16)
- Microphone permission for voice input
- ~300KB install size

## License

MIT
