# Changelog

Alle noemenswaardige wijzigingen aan TeamCreatorr worden hier bijgehouden.

Het formaat is gebaseerd op [Keep a Changelog](https://keepachangelog.com/nl/1.1.0/).

## [1.2.0] - 2026-11-13

### Toegevoegd
- Meertaligheid: Nederlands (standaard), Engels en Turks, met taalkeuze in Instellingen.
- Zwevende pill-navigatie met zes pictogrammen: Home, Spelers, Wedstrijd, Ranking, Historie, Instellingen.
- Volledig wedstrijdoverzicht op het tabblad Wedstrijd (gespeeld én nog te spelen).
- Back-up exporteren en importeren (JSON, versie 1) via de Android-bestandskiezer.
- Resetopties: alles, alleen wedstrijdgeschiedenis, alleen spelers of alleen ranking.
- Numerieke scrollers (zonder toetsenbord) voor uitslagen (0–30) en puntentelling (0–10).
- Ko-fi-donatieknop in Instellingen (https://ko-fi.com/tartaruzz).
- Profielfoto vergroten bij aantikken; foto wijzigen via het bewerkingsscherm (potlood).
- Uniforme groene kopbalk op alle pagina's.

### Gewijzigd
- Notities bij wedstrijden volledig verwijderd (inclusief databasekolom, migratie 2→3).
- "Opnieuw indelen" → "Opnieuw"; "Markeer als gespeeld" → "Uitslag toevoegen".
- "Puntentelling opslaan" → "Puntentelling bijwerken".
- "Deelnames" → "Gespeeld" in het spelersprofiel.
- Duo's worden uitsluitend in het wedstrijdscherm beheerd, met een schakelaar per duo.
- Correcte positionering ten opzichte van Android system bars (edge-to-edge + insets).

### Opgelost
- Navigatiebalk versprong in hoogte bij het wisselen van tabblad (nu uitsluitend pictogrammen).

## [1.1.0] - 2026-11-13

### Toegevoegd
- Optionele **nickname** per speler (zichtbaar in de ranking en bij het delen).
- **Trofee-iconen** (goud/zilver/brons) voor de top 3 in de ranking.
- **Teamindeling delen** via de Android-deelsheet (o.a. WhatsApp).
- **Iedereen selecteren / alles wissen** bij het aanmaken van een wedstrijd.
- **Wedstrijdstatussen**: Teamindeling bevestigd → Gespeeld → Afgerond.
  Nog niet gespeelde wedstrijden leveren geen punten op.
- **Gelijkspelpunten** instelbaar (standaard 1 punt).
- **Handmatige puntenmutaties** per speler (toevoegen/aftrekken, aanpassen,
  verwijderen) met volledige puntenopbouw in het profiel.
- **Spelersrating (1-99)** op basis van gemiddelde wedstrijdpunten.
- **Ranking-gebaseerde teamindeling** (optioneel, houdt rekening met duo's).
- **Profielfoto's** via de Android Photo Picker, automatisch geoptimaliseerd.
- **Centraal duobeheer** in Instellingen; per wedstrijd kunnen vaste duo's
  worden in-/uitgeschakeld en tijdelijke duo's worden toegevoegd.

### Gewijzigd
- Punten worden alleen nog toegekend aan **afgeronde** wedstrijden.
- De onderste navigatiebalk blijft op alle schermen zichtbaar.
- Duo's worden alleen nog centraal beheerd (Instellingen).
- Ranking toont nu ook gelijkspel- en handmatige punten.

### Opgelost
- Home/dashboard was soms niet meer bereikbaar (navigatiebalk is nu altijd
  beschikbaar en de backstack wordt correct afgehandeld).
- Plus-knop viel over de spelerslijst heen (extra onderruimte toegevoegd).

## [1.0.0] - 2026-11-13


### Toegevoegd
- Spelersbeheer: toevoegen (enkel of bulk), hernoemen, verwijderen en
  tijdelijk uitschakelen.
- Nieuwe wedstrijd aanmaken met teamgrootte 3 tegen 3 t/m 11 tegen 11
  (standaard 5 tegen 5).
- Automatische, willekeurige teamverdeling met "opnieuw indelen".
- Duo's: twee spelers verplicht bij elkaar houden, inclusief validatie van
  onmogelijke combinaties.
- Bevestigen van een teamindeling met automatische aanwezigheidsregistratie.
- Uitslagen invoeren en later aanpassen (zonder dubbele punten).
- Ranking met aanwezigheids- en winstpunten, sorteerbaar en met top 3-uitlichting.
- Aanpasbare puntentelling met automatische herberekening van alle historische data.
- Spelersprofielen met statistieken, winpercentage en persoonlijke
  wedstrijdgeschiedenis (met teammarkering en filters).
- Algemene wedstrijdgeschiedenis met detailweergave.
- Wedstrijd aanpassen en verwijderen (met bevestiging).
- Dashboard met top 3, aantallen en laatste uitslag.
- CSV-export van ranking en wedstrijden.
- Room-database voor lokale, permanente opslag.
- Unit-tests voor teamverdeling en puntentelling.
- Eigen app-icoon en sportief groen design.
