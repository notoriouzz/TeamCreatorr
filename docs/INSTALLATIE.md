# Installatiehandleiding — TeamCreatorr

## Direct installeren (aanbevolen)

1. Ga op je computer of telefoon naar de
   [GitHub Releases](../../releases)-pagina van dit project.
2. Download de nieuwste **APK** (bijvoorbeeld `app-release.apk`).
3. Zet de APK op je Android-telefoon (bijvoorbeeld via USB, e-mail of cloud).
4. Open de APK op je telefoon.
5. Android vraagt mogelijk om installatie van onbekende bronnen toe te staan.
   Volg de instructies op het scherm.
6. Open **TeamCreatorr** en voeg je spelers toe.

> Minimaal vereiste Android-versie: **Android 8.0 (API 26)**.

## Zelf bouwen vanuit de broncode

### Vereisten
- JDK 17 of hoger.
- Android SDK (platform 37, build-tools 36).
- Een werkende internetverbinding voor de eerste build (Gradle downloadt
  de afhankelijkheden).

### Stappen
```bash
# 1. Repository clonen
git clone https://github.com/notoriouzz/TeamCreatorr.git
cd TeamCreatorr

# 2. Debug-APK bouwen
./gradlew assembleDebug

# 3. (Optioneel) Release-APK bouwen
./gradlew assembleRelease
```

De APK's vind je in:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

### Installeren via adb
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
