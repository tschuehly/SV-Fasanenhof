# SV Fasanenhof - Website Specification

## Technology Stack

- Static HTML site
- Build script: Kotlin script SSG (`build.main.kts`)
- Hosting: GitHub Pages
- Local development: `./watch.sh` with live reload on `http://localhost:8091`
- No CMS required

---

## Status

This document now describes the shipped **V1** site structure. It should track the real implementation closely and should not describe speculative features as if they already exist.

---

## Information Architecture

**Department-first IA.** The root page is the Verein overview and primary club entry. It combines club identity, department entry points, venue, news, and membership/service links. The departments then operate as self-contained sub-sites at whatever depth their content justifies.

Rationale: ~95% of substantive content is archery-specific. A topic-first IA (top-level `/meisterschaften/`, `/bilder-medien/`, `/training-kurse/`) would imply club-wide scope while hiding the fact that those sections are BSG-only, and would force duplicated paths between `/bogenschiessen/training` and `/training-kurse/`. Department-first avoids the duplication and scales cleanly as Fußball and Tischtennis grow.

---

## Site Structure

```
sv-fasanenhof/
├── index.html                          Verein overview: hero, departments, cross-dept news, venue teaser, CTAs
├── aktuelles/index.html                Aggregated news from all departments
├── termine/index.html                  Recurring training anchors and event pointers
├── mitglied-werden/index.html          Membership entry, fees and contacts
├── suche/index.html                    Static client-side site search
├── barrierefreiheit/index.html         Accessibility notes
├── jugendschutz/index.html             Youth protection contact routes
│
├── verein/                             Club identity
│   ├── index.html                      Overview + links to subpages
│   ├── historie.html                   Club history since 1965 (incl. Ehrenmitglieder)
│   ├── vorstand.html                   Main board + dept leads (anchors per dept)
│   ├── satzung.html                    Statutes and linked rules
│   ├── beitraege.html                  Fees & Beitritt
│   ├── standort.html                   Logauweg 21, public-access rules, Anfahrt (U6), Parken
│   ├── gaststaette.html                Da Angelo
│   └── faq.html                        Membership and contact FAQ
│
├── bogenschiessen/                     BSG (deep)
│   ├── index.html                      Landing: 65 Schützen, Alter 8–80, Bogentypen, CTAs
│   ├── training.html                   3 Orte, Zeiten, Saisons (Outdoor/Indoor), Ausbilder
│   ├── schnupperkurse.html             Ablauf, Preise, AGBs, Buchung
│   ├── trainer.html                    Betreuung und Trainingskontakt
│   ├── ausruestung.html                Recurve, Langbogen, Blankbogen, Compound, Leihmaterial
│   ├── schiessordnung.html             Regeln & Sicherheit
│   ├── faq.html                        Entry, material, training and safety FAQ
│   ├── meisterschaften/
│   │   ├── index.html                  Aktuelle Ergebnisse (2025, 2024)
│   │   └── archiv.html                 2016–2023
│   ├── galerie/
│   │   ├── index.html                  Jahresübersicht
│   │   ├── 2024.html
│   │   ├── 2023.html
│   │   └── …
│   ├── wissen/                         Educational content (archery-specific pedagogy)
│   │   ├── index.html
│   │   ├── regelkunde.html
│   │   ├── sachkunde.html
│   │   ├── wusstest-du.html
│   │   ├── boschi-mit-rolli.html
│   │   ├── boschi-im-alter.html
│   │   ├── bedienungsanleitungen.html
│   │   └── webinare.html
│   ├── aktuelles/
│   │   └── index.html                  BSG-Newsfeed
│   ├── links-und-verbaende.html        External links and association references
│   ├── presse.html                     Press coverage (archery-only today)
│   └── sponsoren.html                  Archery sponsors
│
├── fussball/
│   ├── index.html                      Landing
│   ├── mannschaften.html               Aktive, youth groups and Spielgemeinschaft
│   ├── training.html                   Football training times and location
│   ├── sponsoren.html                  Sponsoring contact
│   ├── faq.html                        Football entry FAQ
│   └── aktuelles/
│       └── index.html                  Football newsfeed
│
├── tischtennis/
│   ├── index.html                      Landing
│   ├── training.html                   Table-tennis training time and location
│   ├── faq.html                        Table-tennis entry FAQ
│   └── aktuelles/
│       └── index.html                  Table-tennis newsfeed
│
├── kontakt/
│   └── index.html                      Form, dept contacts, address, map
│
├── impressum/
│   └── index.html
└── datenschutz/
    └── index.html
```

**Notes on placement decisions:**

- **Presse remains under `/bogenschiessen/`** because today's press content is archery-only. Sponsoring is department-scoped where a department needs a page.
- **News is per-department**, with the Verein overview aggregating. Simpler than a club-wide feed with tags, and matches who writes what.
- **A root news page exists as a read view**, aggregating department feeds without changing where posts are authored.
- **Calendar and search are static V1 features.** `/termine/` is editorial, while `/suche/` uses generated `search-index.json`.
- **Educational content lives at `/bogenschiessen/wissen/`**, not a top-level `/infos-service/`. All of it is archery pedagogy.
- **Training locations are described on `/bogenschiessen/training.html`**, not as separate pages per venue. The public-facing venue page (`/verein/standort.html`) lives under the club section and focuses on the Hauptgelände; the schools are training sites used only by BSG.

---

## Page Mapping

### Old Site → New Site Mapping

| Old URL                             | New Path                                        | Notes                            |
|-------------------------------------|-------------------------------------------------|----------------------------------|
| **sv-fasanenhof.de**                |                                                 |                                  |
| `/`                                 | `/index.html`                                   | Verein overview and main entry   |
|                                     | `/bogenschiessen/index.html`                    | Archery landing                  |
|                                     | `/verein/standort.html`                         | Venue + Da Angelo + access rules |
|                                     | `/kontakt/index.html`                           | Contact info                     |
| **bsg-fasanenhof.jimdofree.com**    |                                                 |                                  |
| `/home/`                            | `/bogenschiessen/index.html`                    | Main archery content             |
| `/verein/vorstand/`                 | `/verein/vorstand.html`                         | Kuno Betz, Birgit Dirksmöller    |
| `/verein/schiessordnung/`           | `/bogenschiessen/schiessordnung.html`           | Shooting regulations             |
| `/verein/datenschutzerklaerung/`    | `/datenschutz/index.html`                       | Privacy                          |
| `/verein/satzung/`                  | `/verein/satzung.html`                          | Statutes                         |
| `/verein/historie/`                 | `/verein/historie.html`                         | Club history                     |
| `/verein/beitraege-beitritt/`       | `/verein/beitraege.html`                        | Fees & membership                |
| `/verein/hauptverein/`              | `/index.html`                                   | Link back to main                |
| `/aktuelles/`                       | `/bogenschiessen/aktuelles/index.html`          | BSG news (aggregated on home)    |
| `/training/`                        | `/bogenschiessen/training.html`                 | Training overview                |
| `/training/trainingzeiten/`         | `/bogenschiessen/training.html`                 | 3 Orte + Zeiten                  |
| `/training/ausbilder/`              | `/bogenschiessen/training.html`                 | Instructor info                  |
| `/training/schnupperkurse/`         | `/bogenschiessen/schnupperkurse.html`           | Taster courses + AGBs            |
| `/training/events/`                 | `/bogenschiessen/aktuelles/index.html`          | Events in news                   |
| `/meisterschaften/`                 | `/bogenschiessen/meisterschaften/index.html`    | Recent results                   |
| `/meisterschaften/2025-*/`          | `/bogenschiessen/meisterschaften/index.html`    | Latest results                   |
| `/meisterschaften/2024-*/`          | `/bogenschiessen/meisterschaften/index.html`    | 2024 results                     |
| `/meisterschaften/2023-*/`          | `/bogenschiessen/meisterschaften/archiv.html`   | Archive                          |
| `/meisterschaften/2022-*/`          | `/bogenschiessen/meisterschaften/archiv.html`   | Archive                          |
| `/meisterschaften/2021-*/`          | `/bogenschiessen/meisterschaften/archiv.html`   | Archive                          |
| `/meisterschaften/2016-2020-links/` | `/bogenschiessen/meisterschaften/archiv.html`   | Archive                          |
| `/bilder/2024/`                     | `/bogenschiessen/galerie/2024.html`             | Photo gallery                    |
| `/bilder/2023/`                     | `/bogenschiessen/galerie/2023.html`             | Photo gallery                    |
| `/bilder/2022/`                     | `/bogenschiessen/galerie/2022.html`             | Photo gallery                    |
| `/bilder/2020-2021/`                | `/bogenschiessen/galerie/index.html`            | Older galleries                  |
| `/presse/`                          | `/bogenschiessen/presse.html`                   | Press coverage                   |
| `/infos/`                           | `/bogenschiessen/wissen/index.html`             | Educational hub                  |
| `/infos/ehrenmitglieder/`           | `/verein/historie.html`                         | Folded into history              |
| `/infos/wusstet-du/`                | `/bogenschiessen/wissen/wusstest-du.html`       | Did you know…                    |
| `/infos/boSchi-mit-rolli/`          | `/bogenschiessen/wissen/boschi-mit-rolli.html`  | Wheelchair archery               |
| `/infos/boSchi-im-alter/`           | `/bogenschiessen/wissen/boschi-im-alter.html`   | Senior archery                   |
| `/infos/sachkunde/`                 | `/bogenschiessen/wissen/sachkunde.html`         | Expertise articles               |
| `/infos/kleine-regelkunde/`         | `/bogenschiessen/wissen/regelkunde.html`        | Rules overview                   |
| `/links/`                           | `/bogenschiessen/wissen/index.html`             | External links section           |
| `/sponsoren/`                       | `/bogenschiessen/sponsoren.html`                | Sponsors                         |
| `/sponsoren/sponsorensuche/`        | `/bogenschiessen/sponsoren.html`                | Sponsorship info                 |

---

## Page Content Summary

### Verein Overview

#### `/index.html`
- 60-year celebration hero
- Three image-led department sections (Bogenschießen, Fußball, Tischtennis)
- Aggregated latest news across departments
- Next Schnupperkurs CTA
- Training-times teaser
- Location section with Vereinsheim image, Standort link and maps route link

### Club Shell

#### `/verein/historie.html`
- Club history since 1965
- Honorary members section

#### `/verein/vorstand.html`
- Main club board
- Department leads with anchors (`#bsg`, `#fussball`, `#tischtennis`)
- Contact per role

#### `/verein/satzung.html`
- Statutes plus links to public rules and related legal/service pages

#### `/verein/beitraege.html`
- Membership fees
- How to join

#### `/verein/gaststaette.html`
- Da Angelo restaurant
- Opening hours, phone and link
- Link to venue page

#### `/verein/faq.html`
- Membership, fees and contact FAQ

#### `/verein/standort.html`
- Logauweg 21, 70565 Stuttgart + map
- Anfahrt: U6 Europaplatz, parking
- Da Angelo restaurant (daily 17:00–22:30)
- Public access rules (Tue–Fri 10–17; no bikes/scooters; footwear restrictions; Hausmeister authority)

### Aktuelles, Termine und Service

#### `/aktuelles/index.html`
- Aggregated chronological news from all department posts

#### `/termine/index.html`
- Recurring training anchors
- Links to department training pages and news

#### `/mitglied-werden/index.html`
- Membership entry steps
- Links to fees and department contacts

#### `/suche/index.html`
- Client-side static search over generated pages and posts
- Uses `build/site/search-index.json`

#### `/barrierefreiheit/index.html`
- Accessibility notes for the website

#### `/jugendschutz/index.html`
- Youth protection contact routes and related links

### Bogenschießen

#### `/bogenschiessen/index.html`
- 65 Schützen, Alter 8–80, Familienatmosphäre
- Bow types: Recurve, Langbogen, Blankbogen, Compound
- Department contact
- Primary CTAs: Schnupperkurs, Training

#### `/bogenschiessen/training.html`
- **Logauweg (Apr–Nov, outdoor):** Mo/Mi/Fr 17–19, Sa 9–13, So 9:30–12
- **Hengstäcker Schule (Margarete-Steiff):** Di/Mi 18–20
- **Anne-Frank-Gemeinschaftsschule (Oct–Mar, indoor):** Do 18:45–20:15
- Instructor information
- Indoor booking/app

#### `/bogenschiessen/schnupperkurse.html`
- Course description, what to bring
- Pricing (Jugend 50 €, Erwachsene 80 €)
- Schedule (3 sessions, Di/Mi 18–20)
- Full AGBs, booking

#### `/bogenschiessen/trainer.html`
- Training supervision and entry contact
- Links to Schnupperkurse and Training

#### `/bogenschiessen/ausruestung.html`
- Bow types in detail
- Rental equipment (Leihmaterial)

#### `/bogenschiessen/schiessordnung.html`
- Shooting regulations
- Safety guidelines
- Facility usage rules

#### `/bogenschiessen/faq.html`
- Entry, material, age range, bow types and training access FAQ

#### `/bogenschiessen/meisterschaften/index.html`
- Recent results (2025, 2024) with structured entries
- Link to archive

#### `/bogenschiessen/meisterschaften/archiv.html`
- Historical results 2016–2023, collapsible by year

#### `/bogenschiessen/galerie/index.html`
- Year index, thumbnails per year

#### `/bogenschiessen/galerie/[year].html`
- Lightbox gallery per year

#### `/bogenschiessen/wissen/index.html`
- Overview of educational content
- External links (was `/links/`)

#### `/bogenschiessen/wissen/{regelkunde,sachkunde,wusstest-du,boschi-mit-rolli,boschi-im-alter,bedienungsanleitungen,webinare}.html`
- Article per topic

#### `/bogenschiessen/links-und-verbaende.html`
- Links to Bogensportverband Stuttgart and Württembergischer Schützenverband
- Links back into internal knowledge pages

#### `/bogenschiessen/aktuelles/index.html`
- BSG news feed (chronological)

#### `/bogenschiessen/presse.html`
- Press coverage

#### `/bogenschiessen/sponsoren.html`
- Current archery sponsors
- Sponsoring opportunities

### Fußball

#### `/fussball/index.html`
- Department overview with links into Mannschaften, Training, Aktuelles, Sponsoren and FAQ
- `aktuelles/` exists as a separate section for football news

#### `/fussball/mannschaften.html`
- Aktive, Jugendgruppen and Spielgemeinschaft

#### `/fussball/training.html`
- Football training times, trainer and location

#### `/fussball/sponsoren.html`
- Sponsoring contact for the football department

#### `/fussball/faq.html`
- Entry, training and venue FAQ

### Tischtennis

#### `/tischtennis/index.html`
- Department overview with current sporting anchor and links into Training, Aktuelles and FAQ
- `aktuelles/` exists as a separate section for table-tennis news

#### `/tischtennis/training.html`
- Training time, location and Ansprechpartner

#### `/tischtennis/faq.html`
- Entry, training and school-holiday FAQ

### Service

#### `/kontakt/index.html`
- Department contacts
- Phone, email and address
- No contact form in V1

#### `/impressum/index.html`
- Legal notice per § 5 TMG

#### `/datenschutz/index.html`
- DSGVO-oriented placeholder structure
- Must be replaced or completed with a legally reviewed final text before a real public launch

---

## Build Script Requirements (Kotlin)

### Input Structure

```
content/
├── index.md
├── aktuelles/index.md
├── termine/index.md
├── mitglied-werden/index.md
├── suche/index.md
├── barrierefreiheit/index.md
├── jugendschutz/index.md
├── verein/
│   ├── index.md
│   ├── historie.md
│   ├── vorstand.md
│   ├── satzung.md
│   ├── beitraege.md
│   ├── standort.md
│   ├── gaststaette.md
│   └── faq.md
├── bogenschiessen/
│   ├── index.md
│   ├── training.md
│   ├── schnupperkurse.md
│   ├── trainer.md
│   ├── ausruestung.md
│   ├── schiessordnung.md
│   ├── faq.md
│   ├── meisterschaften/
│   │   ├── index.md
│   │   └── archiv.md
│   ├── galerie/
│   │   ├── index.md
│   │   └── 2024.md …
│   ├── wissen/
│   │   ├── index.md
│   │   └── …
│   ├── aktuelles/
│   │   ├── index.md
│   │   └── posts/*.md
│   ├── links-und-verbaende.md
│   ├── presse.md
│   └── sponsoren.md
├── fussball/
│   ├── index.md
│   ├── mannschaften.md
│   ├── training.md
│   ├── sponsoren.md
│   ├── faq.md
│   └── aktuelles/
│       ├── index.md
│       └── posts/*.md
├── tischtennis/
│   ├── index.md
│   ├── training.md
│   ├── faq.md
│   └── aktuelles/
│       ├── index.md
│       └── posts/*.md
├── kontakt/index.md
├── impressum/index.md
└── datenschutz/index.md
```

### Output

- HTML files with shared templates
- Static assets (CSS, JS, images)
- Optimized for GitHub Pages

### Features

- Template system (header/footer/navigation)
- Markdown to HTML conversion with frontmatter
- Asset bundling
- Cross-department news aggregator for Verein overview and `/aktuelles/` (pulls from `*/aktuelles/posts/*.md`)
- Static search index generation (`search-index.json`) and `/suche/` template
- Development server with live reload
- Section-specific dropdown navigation beneath the main header for areas with deeper structure
