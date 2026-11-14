# TeamCreatorr ⚽

**TeamCreatorr** is een complete, volledig offline Android-app voor vriendengroepen die
regelmatig zaalvoetbal spelen. De app verdeelt spelers automatisch over twee eerlijke
teams, houdt wedstrijden en uitslagen bij en berekent een ranking op basis van
aanwezigheid en resultaten.

Alle gegevens worden **lokaal** op het apparaat opgeslagen. Geen account, geen
internetverbinding nodig.

| 📖 [Documentation](docs/INSTALLATIE.md) | 📚 [Guides](docs/GEBRUIKERSHANDLEIDING.md) | 📦 [Download](https://github.com/notoriouzz/TeamCreatorr/releases/latest) |
|---|---|---|

---

## Kenmerken

- **Standentabel** (Premier League-stijl) met positie, P/W/D/L, winpercentage, punten en
  de **Form** van de laatste vijf wedstrijden.
- **Meertalig**: Nederlands (standaard), Engels en Turks.
- **Zwevende pill-navigatie** met vijf pictogrammen: Home, Spelers, Wedstrijd, Ranking
  en Instellingen.
- **Wedstrijden**: automatische, eerlijke teamindeling (met optionele duo's die samen of
  juist gescheiden moeten worden ingedeeld), uitslagen invoeren met numerieke scrollers en
  teamindeling delen.
- **Spelers**: profielfoto's, nicknames, uitgebreide statistieken en een selectiesysteem
  om meerdere spelers tegelijk te beheren.
- **Back-up & herstel**: exporteer en importeer alle gegevens als JSON-bestand.
- **Resetopties**: alles, wedstrijdgeschiedenis of spelers.
- **Donkere modus**: schakelbaar in Instellingen en blijvend opgeslagen.
- **Toast-meldingen**: instelbare positie (boven, midden, onder) en aan/uit.
- **Uniforme, sportieve vormgeving** met een groene huisstijl.

---

## ☕ Fueled by coffee and a passion for creating! ☕

A lot of time and effort goes into making TeamCreatorr better. If you enjoy using the app, why not fuel the next update with a coffee? No pressure, just appreciation!

[Buy me a coffee on Ko-fi](https://ko-fi.com/tartaruzz)

---

## Technologie

| Onderdeel | Keuze |
|---|---|
| Taal | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architectuur | MVVM (ViewModel + StateFlow) |
| Opslag | Room (lokale database) |
| Navigatie | Navigation Compose |
| Build | Gradle + AGP + KSP (Room) |

Projectstructuur:

```
app/src/main/java/nl/tartaruzz/teamcreatorr/
├── data/          # Room entities, DAO's, database, repository, back-up
├── domain/        # TeamGenerator, RankingCalculator (pure Kotlin, testbaar)
├── ui/            # Screens, ViewModels, thema, navigatie, componenten
└── util/          # Datum-/formathelpers, fotoverwerking, taalvoorkeur
```

De teamverdeling en puntentelling zijn bewust als **pure Kotlin** geïmplementeerd, zodat
ze met gewone JVM-unit-tests getest kunnen worden.

---

## Installeren & bouwen

### Download (aanbevolen)

1. Download de nieuwste APK van de [GitHub Releases](https://github.com/notoriouzz/TeamCreatorr/releases/latest).
2. Open de APK op je Android-telefoon en sta installatie van onbekende bronnen toe.
3. Open **TeamCreatorr** en voeg je spelers toe.

### Zelf bouwen

Vereisten: JDK 17+ en de Android SDK.

```bash
./gradlew assembleDebug        # debug-APK
./gradlew assembleRelease      # release-APK
./gradlew testDebugUnitTest    # unit-tests
```

Meer details: [docs/INSTALLATIE.md](docs/INSTALLATIE.md).

---

## Gebruiken

Zie [docs/GEBRUIKERSHANDLEIDING.md](docs/GEBRUIKERSHANDLEIDING.md).

---

## Versie

Huidige versie: **1.5.0** (zie [CHANGELOG.md](CHANGELOG.md)).
