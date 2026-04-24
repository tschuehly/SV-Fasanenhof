# Workflow: Word in OneDrive und Markdown im Repo

Dieser Workflow ist für geteilte Abstimmung in Word geeignet, aber nur dann belastbar, wenn das Markdown im Repo die führende Quelle bleibt.

## Grundsatz

- Das Markdown ist die kanonische Planungsquelle.
- Die `.docx` ist das Abstimmungs- und Review-Artefakt für OneDrive und Teilen.
- Word darf kommentiert und ergänzt werden, aber Änderungen müssen danach bewusst gegen das Markdown geprüft werden.

Wenn du Word zur führenden Quelle machst, wird der Prozess schnell unsauber: Diffs sind schlechter nachvollziehbar, Git verliert an Wert, und strukturelle Änderungen landen leicht unkontrolliert in der Planung.

## Empfohlener Ablauf

1. Markdown im Repo pflegen.
2. Daraus die aktuelle `.docx` erzeugen oder aktualisieren.
3. Die `.docx` nach OneDrive verschieben und dort teilen.
4. Nach Review oder Bearbeitung die aktuelle Word-Datei zurück in das Repo holen und die lokale `.docx` ersetzen.
5. Markdown und Word vergleichen.
6. Für echte Abweichungen Workpackage-Specs erzeugen.
7. Änderungen im Markdown und bei Bedarf in der Website-Struktur einarbeiten.
8. Danach die `.docx` erneut aus dem aktuellen Markdown-Stand ableiten oder zumindest wieder synchronisieren.

## Vergleich und Spec-Erzeugung

Das Script [planning_doc_sync.py](/Users/tschuehly/IdeaProjects/SV-Fasanenhof/scripts/planning_doc_sync.py) vergleicht:

- die führende Markdown-Datei
- die zurückgespielte Word-Datei

und kann daraus:

- einen Diff-Report als Markdown erzeugen
- Workpackage-Specs für geänderte Arbeitseinheiten erzeugen

## Beispiel

```bash
python3 scripts/planning_doc_sync.py
```

Ohne Parameter verwendet das Script fest diese Dateien:

- Markdown: `/Users/tschuehly/IdeaProjects/SV-Fasanenhof/site-structure.md`
- Word: `/Users/tschuehly/Library/CloudStorage/OneDrive-Personal/Dokumente/Hobby/SV-Fasanenhof-Homepage.docx`
- Report: `/Users/tschuehly/IdeaProjects/SV-Fasanenhof/build/planning-doc-sync/report.md`

Mit Workpackage-Erzeugung:

```bash
python3 scripts/planning_doc_sync.py \
  --workpackages-dir migrations/workpackages/generated
```

## Ergebnis

- `/Users/tschuehly/IdeaProjects/SV-Fasanenhof/build/planning-doc-sync/report.md` enthält den inhaltlichen Vergleich
- `migrations/workpackages/generated/` enthält pro geänderter Arbeitseinheit eine Spec-Datei

## Was in eine Workpackage-Spec gehört

- betroffene Page oder Arbeitseinheit
- Änderung aus Word
- aktueller Markdown-Stand
- Ziel der Anpassung
- Akzeptanzkriterien
- offene Fragen
- fachliche Freigabe

## Praktische Regel

Nicht gleichzeitig dieselbe fachliche Änderung unabhängig in Markdown und Word editieren. Sonst erzeugst du künstliche Konflikte, die später wie echte Anforderungen aussehen.
