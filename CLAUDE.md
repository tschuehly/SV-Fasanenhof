# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A unified static website for **1. SV Fasanenhof 1965 e.V.**, a sports club in Stuttgart (60 years old in 2025) with three departments:
- **Bogensportabteilung (BSG)**: Archery - 65 archers, ages 8-80, most comprehensive content
- **Fußball**: Football - minimal content, needs development
- **Tischtennis**: Table Tennis - minimal content, needs development

The project consolidates two existing websites (sv-fasanenhof.de and bsg-fasanenhof.jimdofree.com) into one cohesive platform hosted on GitHub Pages.

## Content Structure

### Content Directories

- **`content/sv-fasanenhof/`**: Main club content
  - `index.md`: Homepage (welcome, 60-year celebration, venue info, public access rules)
  - `fussball.md`, `tischtennis.md`, `bogenschiessen.md`: Department pages
  - `verein.md`, `vorstand.md`, `satzung.md`: Club governance
  - `mitglieder-beitrage.md`: Membership fees and joining info
  - `gaststatte.md`: Restaurant "Da Angelo" info (daily 17:00-22:30)
  - `historie.md`: Club history since 1965

- **`content/bsg-fasanenhof/`**: Archery department content
  - `bsg-fasanenhof-home.md`: BSG landing page
  - `bsg-trainingzeiten.md`: Training times at 3 locations
    - Logauweg (Apr-Nov): Mon/Wed/Fri 17-19, Sat 9-13, Sun 9:30-12
    - Hengstäcker School: Tue/Wed 18-20
    - Anne-Frank School (Oct-Mar): Thu 18:45-20:15
  - `bsg-schnupperkurse.md`: Taster courses (Youth: 50€, Adults: 80€, 3 sessions)

- **`content-archive/`**: Archived content from old sites
  - Legal pages, sponsor pages, etc. - reference for migration

### Content Map Files

- **`content/content-map-current.md`**: Mapping of current two-site structure
- **`content/content-map-new.md`**: Proposed unified site navigation and content hierarchy

## Planned Build Architecture

**Note: The Kotlin build script mentioned in SITE-SPEC.md has not been implemented yet.**

### Content Conventions

- Primary language: German
- Markdown format for all content
- Use "---" as section dividers
- Frontmatter style (Title, Description, Website fields) is present but not standardized

## Development Notes

### Current State
- Project is in planning/documentation phase
- Build script needs to be created (Kotlin as per specification)
- Content exists but needs structure refinement
- No existing HTML templates or build system

### When Creating the Build System
1. Implement markdown parsing with frontmatter support
2. Create reusable template components
3. Handle the content-to-URL mapping from content-map files
4. Support German language-specific needs (special characters, formatting)
5. Ensure responsive design for mobile users

### Content Strategy
- Archery content is comprehensive (meets needs)
- Football and Table Tennis content needs development
- Main club governance information needs consolidation
- Tournament archives (2016-2025) need integration
- Photo galleries by year (2015-2024) need organization
