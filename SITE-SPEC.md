# SV Fasanenhof - Website Specification

## Technology Stack

- Static HTML site
- Build script: Kotlin
- Hosting: GitHub Pages
- No CMS required

---

## Site Structure

```
sv-fasanenhof/
├── index.html
├── ueber-uns/
│   ├── index.html
│   ├── geschichte.html
│   ├── vorstand.html
│   ├── satzung.html
│   ├── beitraege-beitritt.html
│   ├── ehrenmitglieder.html
│   └── standort-anfahrt.html
├── sportabteilungen/
│   ├── index.html
│   ├── bogenschiessen/
│   │   ├── index.html
│   │   ├── training.html
│   │   ├── kurse.html
│   │   ├── regelwerk.html
│   │   └── meisterschaften.html
│   ├── fussball/
│   │   └── index.html
│   └── tischtennis/
│       └── index.html
├── anfahrt-sportstaetten/
│   ├── index.html
│   ├── hauptgelände.html
│   ├── schulzentrum-hengstaecker.html
│   └── anne-frank-schule.html
├── training-kurse/
│   ├── index.html
│   └── schnupperkurse.html
├── meisterschaften/
│   ├── index.html
│   └── archiv.html
├── bilder-medien/
│   ├── index.html
│   ├── galerie-2024.html
│   ├── galerie-2023.html
│   ├── galerie-2022.html
│   └── presse.html
├── aktuelles/
│   ├── index.html
│   └── [news-posts].html
├── infos-service/
│   ├── index.html
│   ├── regelkunde.html
│   ├── sachkunde.html
│   └── wheelchair-archery.html
├── sponsoren/
│   └── index.html
├── kontakt/
│   └── index.html
└── impressum/
    └── index.html
```

---

## Page Mapping

### Old Site → New Site Mapping

| Old URL                             | New Path                                          | Notes                         |
|-------------------------------------|---------------------------------------------------|-------------------------------|
| **sv-fasanenhof.de**                |                                                   |                               |
| `/`                                 | `/index.html`                                     | Main homepage                 |
|                                     | `/ueber-uns/index.html`                           | Club info from SV site        |
|                                     | `/sportabteilungen/bogenschiessen/index.html`     | Archery section               |
|                                     | `/anfahrt-sportstaetten/index.html`               | Venue info + Da Angelo hours  |
|                                     | `/anfahrt-sportstaetten/hauptgelände.html`        | Public access rules           |
|                                     | `/kontakt/index.html`                             | Contact info                  |
| **bsg-fasanenhof.jimdofree.com**    |                                                   |                               |
| `/home/`                            | `/sportabteilungen/bogenschiessen/index.html`     | Main archery content          |
| `/verein/vorstand/`                 | `/ueber-uns/vorstand.html`                        | Kuno Betz, Birgit Dirksmöller |
| `/verein/schiessordnung/`           | `/sportabteilungen/bogenschiessen/regelwerk.html` | Shooting regulations          |
| `/verein/datenschutzerklaerung/`    | `/impressum/index.html`                           | Privacy section               |
| `/verein/satzung/`                  | `/ueber-uns/satzung.html`                         | Statutes                      |
| `/verein/historie/`                 | `/ueber-uns/geschichte.html`                      | Club history                  |
| `/verein/beitraege-beitritt/`       | `/ueber-uns/beitraege-beitritt.html`              | Fees & membership             |
| `/verein/hauptverein/`              | `/index.html`                                     | Link back to main             |
| `/aktuelles/`                       | `/aktuelles/index.html`                           | News section                  |
| `/training/`                        | `/sportabteilungen/bogenschiessen/training.html`  | Training overview             |
| `/training/trainingzeiten/`         | `/sportabteilungen/bogenschiessen/training.html`  | 3 locations + schedules       |
| `/training/ausbilder/`              | `/sportabteilungen/bogenschiessen/training.html`  | Instructor info               |
| `/training/schnupperkurse/`         | `/training-kurse/schnupperkurse.html`             | Taster courses + AGBs         |
| `/training/events/`                 | `/aktuelles/index.html`                           | Events in news                |
| `/meisterschaften/`                 | `/meisterschaften/index.html`                     | Recent results                |
| `/meisterschaften/2025-*/`          | `/meisterschaften/index.html`                     | Latest results                |
| `/meisterschaften/2024-*/`          | `/meisterschaften/index.html`                     | 2024 results                  |
| `/meisterschaften/2023-*/`          | `/meisterschaften/archiv.html`                    | Archive section               |
| `/meisterschaften/2022-*/`          | `/meisterschaften/archiv.html`                    | Archive section               |
| `/meisterschaften/2021-*/`          | `/meisterschaften/archiv.html`                    | Archive section               |
| `/meisterschaften/2016-2020-links/` | `/meisterschaften/archiv.html`                    | Archive section               |
| `/bilder/2024/`                     | `/bilder-medien/galerie-2024.html`                | Photo gallery                 |
| `/bilder/2023/`                     | `/bilder-medien/galerie-2023.html`                | Photo gallery                 |
| `/bilder/2022/`                     | `/bilder-medien/galerie-2022.html`                | Photo gallery                 |
| `/bilder/2020-2021/`                | `/bilder-medien/index.html`                       | Older galleries               |
| `/presse/`                          | `/bilder-medien/presse.html`                      | Press coverage                |
| `/infos/`                           | `/infos-service/index.html`                       | Educational content           |
| `/infos/ehrenmitglieder/`           | `/ueber-uns/ehrenmitglieder.html`                 | Honorary members              |
| `/infos/wusstet-du/`                | `/infos-service/index.html`                       | Did you know...               |
| `/infos/boSchi-mit-rolli/`          | `/infos-service/wheelchair-archery.html`          | Wheelchair archery            |
| `/infos/boSchi-im-alter/`           | `/infos-service/index.html`                       | Senior archery info           |
| `/infos/sachkunde/`                 | `/infos-service/sachkunde.html`                   | Expertise articles            |
| `/infos/kleine-regelkunde/`         | `/infos-service/regelkunde.html`                  | Rules overview                |
| `/links/`                           | `/infos-service/index.html`                       | External links                |
| `/sponsoren/`                       | `/sponsoren/index.html`                           | Sponsors                      |
| `/sponsoren/sponsorensuche/`        | `/sponsoren/index.html`                           | Sponsorship info              |

---

## Page Content Summary

### Core Pages (Must Have)

#### `/index.html`

- Welcome message (60 years celebration)
- Sports overview (Archery, Football, Table Tennis)
- Latest news teaser
- Quick training times
- Venue/restaurant info

#### `/ueber-uns/index.html`

- Club overview
- Brief history
- Contact info teaser
- Links to subpages

#### `/ueber-uns/geschichte.html`

- Club history since 1965

#### `/ueber-uns/vorstand.html`

- Main club board
- Archery department leadership (Kuno Betz, Birgit Dirksmöller)

#### `/ueber-uns/satzung.html`

- Club statutes

#### `/ueber-uns/beitraege-beitritt.html`

- Membership fees
- How to join

#### `/ueber-uns/ehrenmitglieder.html`

- Honorary members

#### `/ueber-uns/standort-anfahrt.html`

- Map
- Directions (U6 Europaplatz)
- Parking info

### Archery Pages

#### `/sportabteilungen/bogenschiessen/index.html`

- Overview: 65 archers, ages 8-80, family atmosphere
- Bow types (Recurve, Longbow, Blankbogen, Compound)
- Contact for department

#### `/sportabteilungen/bogenschiessen/training.html`

- Training times at 3 locations:
    - Logauweg (Apr-Nov): Mon/Wed/Fri 17-19, Sat 9-13, Sun 9:30-12
    - Hengstäcker School: Tue/Wed 18-20
    - Anne-Frank School (Oct-Mar): Thu 18:45-20:15
- Instructor information
- Booking/app for indoor training

#### `/sportabteilungen/bogenschiessen/kurse.html`

- Taster course info
- Pricing (Youth: 50€, Adults: 80€)
- Schedule (3 sessions, Tue/Wed 18-20)
- Full AGBs

#### `/sportabteilungen/bogenschiessen/regelwerk.html`

- Shooting regulations
- Safety guidelines

#### `/sportabteilungen/bogenschiessen/meisterschaften.html`

- Link to main championships section

### Venue Pages

#### `/anfahrt-sportstaetten/index.html`

- All training locations overview
- Restaurant "Da Angelo" (daily 17:00-22:30)

#### `/anfahrt-sportstaetten/hauptgelände.html`

- Logauweg 21, 70565 Stuttgart
- Public access: Tue-Fri 10-17
- Usage rules (no bikes, footwear restrictions, etc.)

#### `/anfahrt-sportstaetten/schulzentrum-hengstaecker.html`

- Hengstäcker, Gewann 12, 70567 Stuttgart

#### `/anfahrt-sportstaetten/anne-frank-schule.html`

- Hechingerstr. 73, 70567 Stuttgart

### Championships Pages

#### `/meisterschaften/index.html`

- Recent results (2025, 2024)
- Link to archive

#### `/meisterschaften/archiv.html`

- Historical results 2016-2023

### Media Pages

#### `/bilder-medien/index.html`

- Gallery overview
- Year navigation

#### `/bilder-medien/galerie-[YEAR].html`

- Photo galleries by year

#### `/bilder-medien/presse.html`

- Press coverage

### Service Pages

#### `/training-kurse/index.html`

- Course overview

#### `/training-kurse/schnupperkurse.html`

- Full course details and booking info

#### `/aktuelles/index.html`

- News and events

#### `/infos-service/index.html`

- Educational articles overview

#### `/kontakt/index.html`

- Contact form
- Phone/email
- Map

#### `/sponsoren/index.html`

- Current sponsors
- Sponsorship opportunities

#### `/impressum/index.html`

- Legal notice
- Privacy policy
- Cookie policy

---

## Build Script Requirements (Kotlin)

### Input Structure

```
content/
├── index.md
├── ueber-uns/
│   ├── index.md
│   ├── geschichte.md
│   └── ...
├── sportabteilungen/
│   └── ...
└── ...
```

### Output

- HTML files with shared templates
- Static assets (CSS, JS, images)
- Optimized for GitHub Pages

### Features

- Template system (header/footer/navigation)
- Markdown to HTML conversion
- Asset bundling
- Development server (optional)
