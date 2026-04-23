# Legacy-Portierungsplan

Dieser Plan übersetzt die Altinhalte aus `migrations/` in die heutige IA, ohne die V1-Struktur unnötig wieder aufzubrechen.

## Leitplanken

- Nicht jede Altseite verdient wieder eine eigene Route. Besonders bei Fußball und Tischtennis ist ein verdichteter Zielzustand besser als eine 1:1-Kopie der alten Navigation.
- Rechts- und Datenschutztexte dürfen nicht blind übernommen werden. Das gilt besonders für `datenschutz`, `ihre-rechte`, Cookie-Texte und alles rund um Jugendschutz.
- Bilder erst nach Sichtung veröffentlichen. Der Import in `migrations/downloads/legacy-assets/` ist ein Materiallager, kein Freifahrtschein für ungeprüfte Veröffentlichung.

## Priorität A

| Altmaterial | Ziel im V1 | Empfehlung |
| --- | --- | --- |
| `migrations/raw-content/sv-fasanenhof/historie.md` | `content/verein/historie.md` | Die jetzige Seite mit der echten Vereinszeitleiste anreichern, inklusive Ehrenmitglieder und Wendepunkte wie 1980, 1995/96, 2010, 2019. |
| `migrations/raw-content/sv-fasanenhof/vorstand.md` | `content/verein/vorstand.md` | Platzhalter durch echte Rollen, Zuständigkeiten und Kontaktwege ersetzen. Vor Veröffentlichung aktuelle Daten gegenprüfen. |
| `migrations/raw-content/sv-fasanenhof/mitglieder-beitrage.md` und `migrations/crawl-output/bsg-fasanenhof/beitraege-beitritt.md` | `content/verein/beitraege.md` | Hauptvereinsbeiträge und BSG-Abteilungsbeitrag sauber zusammenführen statt nur allgemein zu bleiben. |
| `migrations/raw-content/sv-fasanenhof/gaststatte.md` und `migrations/raw-content/sv-fasanenhof/index.md` | `content/standort/index.md` | Da Angelo, öffentliche Nutzung, Anfahrt und Platzregeln ausformulieren. Die Inhalte sind bereits vorgesehen und sollten aus dem Altmaterial vollständig portiert werden. |
| `migrations/raw-content/bsg-fasanenhof/bsg-fasanenhof-home.md` | `content/bogenschiessen/index.md` | Die Landingpage inhaltlich näher an die alte BSG-Startseite ziehen: familiärer Charakter, 65 Aktive, U6/Anfahrt, Parkplatz, Maskottchen nur falls gewünscht. |
| `migrations/raw-content/bsg-fasanenhof/bsg-trainingzeiten.md` | `content/bogenschiessen/training.md` | Die bestehende Struktur ist richtig, aber Details wie Distanzen, Hallenhinweise, App-Buchung und freies Training fehlen noch oder sind zu knapp. |
| `migrations/raw-content/bsg-fasanenhof/bsg-schnupperkurse.md` | `content/bogenschiessen/schnupperkurse.md` | Der alte Text enthält deutlich mehr Substanz: Teilnehmerzahl, Ansprechpartner, Ablauf, AGB-Logik. Das sollte fast vollständig eingearbeitet werden. |
| `migrations/content-archive/bsg-sponsoren.md` | `content/bogenschiessen/sponsoren.md` | Die aktuelle Seite ist nur ein Kurzabriss. Altmaterial liefert echte Förderfälle, Jahreskontexte und Namen. |
| `migrations/content-archive/bsg-links.md` und Live-Seiten unter `infos/` | `content/bogenschiessen/wissen/*.md` | Die Wissensseiten existieren, sind aber noch überwiegend Hüllen. Alte Fachinhalte, Links und Praxiswissen sollten gezielt in die vorhandenen Zielseiten verteilt werden. |

## Priorität B

| Altmaterial | Ziel im V1 | Empfehlung |
| --- | --- | --- |
| `migrations/crawl-output/sv-fasanenhof/fussball*.md` | `content/fussball/index.md` und `content/fussball/aktuelles/` | Nicht die alte WordPress-Unterstruktur nachbauen. Besser: eine belastbare Landingpage mit Teams, Trainingszeiten, Jugend, Gastmannschaften und 1–2 echte Meldungen. |
| `migrations/crawl-output/sv-fasanenhof/tischtennis*.md` | `content/tischtennis/index.md` | Trainingszeiten, sportlicher Status und Ansprechpartner aus dem Altmaterial einbauen. Eine zusätzliche Unterseite ist noch nicht nötig. |
| `migrations/crawl-output/bsg-fasanenhof/meisterschaften/*.md` plus Live-Seiten | `content/bogenschiessen/meisterschaften/index.md` und `archiv.md` | Die Seiten sollten nicht nur Wettbewerbsnamen listen, sondern die alten Ergebnisse strukturierter verdichten: Jahr, Format, Ort, ggf. kurze Leistungshinweise. |
| BSG-Bildseiten `bilder/2015` bis `bilder/2024` | `content/bogenschiessen/galerie/` | Nach dem Bildimport die Jahrgänge 2015–2021 mindestens als Archiv ergänzen. Nicht nur 2022–2024 offen lassen, wenn das Altmaterial mehr hergibt. |
| BSG-`presse/` | `content/bogenschiessen/presse.md` | Nur portieren, wenn die alte Seite echte Berichte oder Artikelhinweise enthält. Leere Presse-Rhetorik ist wertlos. |

## Priorität C oder nur nach Prüfung

| Altmaterial | Ziel im V1 | Empfehlung |
| --- | --- | --- |
| `jugendaktivitäten/spiel-und-spaß/`, `projektwoche-im-ferienwaldheim/`, `jugendaustausch-mit-straßburg/` | eher `content/bogenschiessen/aktuelles/` oder `galerie/` | Das sind meist Ereignisse, keine Evergreen-Seiten. Als Archiv-Meldungen oder Galerien besser aufgehoben als als neue Hauptnavigation. |
| `jugendaktivitäten/jugendschutzkonzept/` und `schutzbeauftragte/` | ggf. neue geprüfte Vereins-/Service-Seite | Inhaltlich wichtig, aber riskant. Nur mit fachlicher Aktualisierung portieren, sonst lieber vorerst archiviert lassen. |
| `migrations/content-archive/bsg-ihre-rechte.md` | keine direkte Portierung | Alte Einwilligungs- und Veröffentlichungslogik nicht blind übernehmen. Falls nötig, neu schreiben und juristisch prüfen. |
| `migrations/crawl-output/*/datenschutz*.md` | `content/datenschutz/index.md` nur nach Review | Der aktuelle Platzhalter ist absichtlich defensiv. Das ist richtig und sollte nicht durch ungeprüftes Altmaterial verschlechtert werden. |
| `migrations/content-archive/bsg-hauptverein.md` | keine eigene Seite | Das war nur ein Rücksprung auf die Hauptseite. Im V1 ohne Mehrwert. |

## Praktische Reihenfolge

1. Bilder und Dokumente aus den Altseiten lokal sichern.
2. Seiten mit echtem Nutzwert zuerst ausbauen: `verein`, `standort`, `bogenschiessen/training`, `bogenschiessen/schnupperkurse`.
3. Danach Sportarten mit dünner Bestandsseite ergänzen: `fussball`, `tischtennis`.
4. Erst im Anschluss Archive vervollständigen: `meisterschaften`, `galerie`, `wissen`, `presse`.

## Offene Entscheidungen

- Soll `jugendaktivitäten` als eigener Bereich sichtbar werden oder reicht eine Verteilung auf `aktuelles`, `galerie` und eine spätere geprüfte Service-Seite?
- Sollen ältere Galeriejahrgänge einzeln erscheinen oder ab `2015–2021` in Sammelseiten gruppiert werden?
- Welche Kontakte aus den Altseiten sind noch aktuell genug für die Produktion?
