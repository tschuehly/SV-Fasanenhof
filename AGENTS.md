# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Project Overview

A unified static website for **1. SV Fasanenhof 1965 e.V.**, a sports club in Stuttgart (60 years old in 2025) with three departments:
- **Bogensportabteilung (BSG)**: Archery - 65 archers, ages 8-80, most comprehensive content
- **Fußball**: Football - minimal content, needs development
- **Tischtennis**: Table Tennis - minimal content, needs development

The project consolidates two existing websites (sv-fasanenhof.de and bsg-fasanenhof.jimdofree.com) into one cohesive platform hosted on GitHub Pages.

## Current Architecture

- Static site generator: Kotlin script in `build.main.kts`
- Output directory: `build/site/`
- Local development: `./watch.sh` with live reload at `http://localhost:8091`
- Dev server: `devserver.py`
- Shared assets: `src/main/resources/assets/`
- Hosting: GitHub Pages via `.github/workflows/pages.yml`

This repository is now the **V1 production codebase**, not a mockup or planning sandbox. Keep documentation and implementation aligned to the shipped site instead of describing speculative future structure as if it already exists.

## Build Commands

- Production build:
  - `kotlin build.main.kts`
- Live-reload dev server:
  - `./watch.sh`
  - optional port override: `./watch.sh 8091`

Do not reintroduce a full Gradle project unless the script approach becomes a real blocker. For the current scope, a single Kotlin script is intentionally the simpler and better fit.

## Content Structure

### Canonical SSG Content

- **`content/index.md`**: Home
- **`content/verein/`**: Club shell
  - `index.md`, `historie.md`, `vorstand.md`, `satzung.md`, `beitraege.md`
- **`content/standort/`**: Venue, Da Angelo, access rules
- **`content/bogenschiessen/`**: Archery section
  - `index.md`, `training.md`, `schnupperkurse.md`, `ausruestung.md`, `schiessordnung.md`
  - `meisterschaften/`, `galerie/`, `wissen/`, `aktuelles/`
- **`content/fussball/`**: Football landing + `aktuelles/`
- **`content/tischtennis/`**: Table tennis landing + `aktuelles/`
- **`content/kontakt/`**, **`content/impressum/`**, **`content/datenschutz/`**

### Legacy Source Material

- **`migrations/raw-content/sv-fasanenhof/`**: migrated raw main-club source material
- **`migrations/raw-content/bsg-fasanenhof/`**: migrated raw archery source material
- **`migrations/crawl-output/`**: crawled source snapshots
- **`migrations/content-archive/`**: archived legacy content
- **`migrations/content-maps/`**: old planning/content-map documents

These directories are not render inputs anymore. Treat them as migration/reference inputs unless there is an explicit cleanup or archival task.

## Development Notes

### Current State
- V1 of the site is implemented and published from the Kotlin SSG
- Kotlin SSG is implemented
- GitHub Pages deploys the generated site from `build/site`
- Mockup phase has been removed from the repo
- Canonical content structure follows the department-first IA from `SITE-SPEC.md`
- The header includes a main navigation plus section-specific dropdown navigation for areas like `verein/` and `bogenschiessen/`

### Content Conventions
- Primary language: German
- Use real umlauts (`ä`, `ö`, `ü`) in German text
- Never put meta commentary into website copy. Production pages should not mention migration status, placeholders, curation notes, or internal editorial process.
- Markdown with frontmatter (`title`, `description`, `kicker`, `lead`, optional `template`, `summary`)
- Department-first IA:
  - club shell at root sections like `verein/`, `standort/`, `kontakt/`
  - deep content under `bogenschiessen/`
  - `fussball/` and `tischtennis/` stay slimmer until more content exists

### Live Reload
- `build.main.kts` supports `--live-reload`
- `watch.sh` rebuilds on content, asset, script, and dev-server changes
- `devserver.py` serves `build/site` and exposes `/__livereload`

### Important Constraints
- Do not put production assets back under `mockups/`
- Do not commit local Codex environment files such as `.codex/`
- Do not commit temporary Microsoft Office lock files such as `~$*.docx`
- Be careful with `content/datenschutz/index.md`: the old crawl contained unrelated third-party data, so that page is intentionally a reviewed placeholder rather than a blind migration
- Keep `content/` clean. Raw source imports and archival material belong under `migrations/`, not beside canonical site pages.
- When changing IA, navigation, or page structure, update `SITE-SPEC.md` in the same change if the intended architecture has changed.

### Content Strategy
- Bogensport remains the deepest content area and the best test bed for the SSG
- Fußball and Tischtennis should grow incrementally from real source material, not invented filler copy
- Main club governance information should stay in the `verein/` section
- Meisterschaften and galleries should continue to be organized primarily under `bogenschiessen/`

## Planning Docs Workflow

Use this workflow when collaborating on planning documents that live both in the repo and in a shared Word file.

### Source of Truth

- The Markdown file in the repo is the canonical planning source.
- The `.docx` file is a review and sharing artifact, for example for OneDrive sharing with stakeholders.
- Do not treat the shared Word file as the primary source of truth. That makes diffs harder to trust and quickly turns workpackage creation into manual cleanup.

### Expected Process

1. Update the canonical Markdown file in `migrations/content-maps/`.
2. Regenerate or refresh the matching `.docx` from that Markdown when needed for review.
3. Share or move the `.docx` through OneDrive for comments and stakeholder edits.
4. After review, bring the updated `.docx` back into the repo and compare it against the Markdown.
5. Turn real content differences into explicit workpackages before changing website content or architecture.
6. Re-sync the Markdown and `.docx` after changes are accepted.

### Comparison Tooling

- Use `scripts/planning_doc_sync.py` to compare the repo Markdown against the current `.docx`.
- The script can emit a diff report and generate workpackage specs for changed planning units.
- Preferred usage:

```bash
python3 scripts/planning_doc_sync.py
```

The script defaults are hardcoded to:

- Markdown: `/Users/tschuehly/IdeaProjects/SV-Fasanenhof/site-structure.md`
- Word: `/Users/tschuehly/Library/CloudStorage/OneDrive-Personal/Dokumente/Hobby/SV-Fasanenhof-Homepage.docx`
- Report: `/Users/tschuehly/IdeaProjects/SV-Fasanenhof/build/planning-doc-sync/report.md`

When workpackages should be generated as well, use:

```bash
python3 scripts/planning_doc_sync.py \
  --workpackages-dir migrations/workpackages/generated
```

### Workpackage Expectations

- Generate workpackages from reviewed content differences, not from formatting noise.
- Each workpackage should stay tied to a concrete planning unit such as a page or section.
- Before implementing against the website, verify whether the change is:
  - only a planning update,
  - a website content update,
  - or an IA / navigation change that also requires `SITE-SPEC.md` updates.
