# TeamCreatorr ⚽

**TeamCreatorr** is een complete Android-app voor vriendengroepen die regelmatig
zaalvoetbal spelen. De app verdeelt spelers automatisch over twee eerlijke,
willekeurige teams, houdt wedstrijden en uitslagen bij en berekent een ranking
op basis van aanwezigheid en overwinningen.

Alle gegevens worden **lokaal** op het apparaat opgeslagen. De app werkt volledig
zonder internetverbinding.

---

## Nieuwe functies (v1.1.0)

- **Nicknames**: optioneel per speler, zichtbaar in de ranking en in het
  WhatsApp-deelbericht.
- **Trofeeën** voor de top 3 van de ranking (goud, zilver, brons).
- **Teamindeling delen** via de Android-deelsheet (WhatsApp en meer).
- **Iedereen selecteren / alles wissen** bij het maken van een wedstrijd.
- **Wedstrijdstatussen**: Teamindeling bevestigd → Gespeeld → Afgerond.
  Nog niet gespeelde wedstrijden leveren (nog) geen punten op.
- **Gelijkspelpunten** apart instelbaar (standaard 1 punt).
- **Handmatige puntenmutaties** per speler, met volledige puntenopbouw.
- **Spelersrating (1-99)** en optionele **ranking-gebaseerde teamindeling**.
- **Profielfoto's** via de Android Photo Picker (automatisch geoptimaliseerd).
- **Centraal duobeheer** in Instellingen, met tijdelijke duo's per wedstrijd.

> **Ratingberekening:** rating = `1 + 98 × (gemiddelde wedstrijdpunten / max_punten_per_wedstrijd)`,
> afgerond en begrensd op 1–99. `max_punten_per_wedstrijd = aanwezigheid + max(winst, gelijkspel)`.
> Spelers zonder gespeelde wedstrijden krijgen de neutrale rating 50. Handmatige
> puntenmutaties tellen niet mee voor de rating.

## Wat kan de app?

### Spelersbeheer
- Spelers toevoegen (één voor één of meerdere tegelijk).
- Namen aanpassen en spelers verwijderen.
- Spelers tijdelijk uitschakelen zonder hun historie te verliezen.
- Elke speler heeft een vaste unieke ID, zodat statistieken behouden blijven
  als een naam wordt aangepast.

### Wedstrijden maken
- Kies een teamgrootte van **3 tegen 3** tot en met **11 tegen 11** (standaard 5 tegen 5).
- Selecteer precies het juiste aantal spelers (de app rekent en valideert automatisch).
- Stel een datum en optionele notitie in.
- Twee of meer spelers verplicht bij elkaar houden via **duo's**.
- Teams automatisch willekeurig verdelen, met een knop **Opnieuw indelen**.
- Teamindeling bevestigen → wedstrijd wordt opgeslagen en aanwezigheidspunten
  worden toegekend (dubbel bevestigen wordt voorkomen).

### Uitslagen & ranking
- Voer de score van Team A en Team B in; de app bepaalt winnaar of gelijkspel.
- Uitslagen later aanpassen zonder dubbele punten.
- Wedstrijden verwijderen (met bevestiging); alle punten verdwijnen automatisch.
- Ranking met positie, deelnames, winst/verlies/gelijk, aanwezigheidspunten,
  winstpunten en totaal. De top 3 wordt visueel uitgelicht.

### Puntentelling aanpasbaar
- Standaard: aanwezigheid = 1 punt, winst = 2 extra punten.
- Beide waarden zijn aanpasbaar in Instellingen; de **volledige ranking wordt
  automatisch opnieuw berekend** (ook voor historische wedstrijden).

### Profielen & geschiedenis
- Per speler een uitgebreid profiel met statistieken en winpercentage.
- Persoonlijke wedstrijdgeschiedenis, met het eigen team duidelijk gemarkeerd.
- Filter op alle / gewonnen / verloren / gelijke wedstrijden.
- Algemene wedstrijdgeschiedenis met alle details per wedstrijd.

### Extra's
- Dashboard met top 3, aantallen en de laatste uitslag.
- CSV-export van ranking en wedstrijden.
- Eigen app-icoon en een sportief groen design.

---

## Technologie

| Onderdeel | Keuze |
|---|---|
| Taal | Kotlin 2.2 |
| UI | Jetpack Compose + Material 3 |
| Architectuur | MVVM (ViewModel + StateFlow) |
| Opslag | Room (lokale database) |
| Navigatie | Navigation Compose |
| Build | Gradle 9.6, AGP 9.5, KSP (Room) |

Projectstructuur:

```
app/src/main/java/nl/tartaruzz/teamcreatorr/
├── data/          # Room entities, DAO's, database, repository
├── domain/        # TeamGenerator, RankingCalculator (pure Kotlin, testbaar)
├── ui/            # Screens, ViewModels, thema, navigatie, componenten
└── util/          # Datum-/formathelpers
```

De teamverdeling en puntentelling zijn bewust als **pure Kotlin** geïmplementeerd,
zodat ze met gewone JVM-unit-tests getest kunnen worden.

---

## Installeren

Zie [docs/INSTALLATIE.md](docs/INSTALLATIE.md) voor een uitgebreide handleiding.

Kort:
1. Download de APK van de nieuwste [GitHub Release](../../releases).
2. Open de APK op je Android-telefoon.
3. Sta installatie van onbekende bronnen toe wanneer daarom wordt gevraagd.
4. Open **TeamCreatorr** en voeg je spelers toe.

### Zelf bouwen (voor ontwikkelaars)

Vereisten: JDK 17+ en de Android SDK.

```bash
./gradlew assembleDebug        # debug-APK
./gradlew assembleRelease      # release-APK (getekend met debug-sleutel)
./gradlew testDebugUnitTest    # unit-tests
```

---

## Gebruiken

Zie [docs/GEBRUIKERSHANDLEIDING.md](docs/GEBRUIKERSHANDLEIDING.md).

---

## Versie

Huidige versie: **1.0.0** (zie [CHANGELOG.md](CHANGELOG.md)).
