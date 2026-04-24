---
name: "1. SV Fasanenhof Website"
description: "Warm, structured public website for a Stuttgart sports club."
colors:
  background: "#f5f1e8"
  background-top: "#faf7f0"
  background-bottom: "#f2ecdf"
  paper: "#fffdf8"
  paper-strong: "#ffffff"
  ink: "#151515"
  ink-soft: "#3f3f3f"
  ink-muted: "#66604f"
  line: "#ddd5c6"
  green: "#2f6b3b"
  green-dark: "#173c1f"
  green-soft: "#dceadf"
  gold: "#d6ab45"
  footer-muted: "#d9d4c8"
typography:
  display:
    fontFamily: "Merriweather, serif"
    fontSize: "clamp(2.6rem, 5vw, 4.8rem)"
    fontWeight: 700
    lineHeight: 1.02
    letterSpacing: "normal"
  headline:
    fontFamily: "Merriweather, serif"
    fontSize: "clamp(2rem, 4vw, 3.3rem)"
    fontWeight: 700
    lineHeight: 1.05
    letterSpacing: "normal"
  title:
    fontFamily: "Merriweather, serif"
    fontSize: "1.55rem"
    fontWeight: 700
    lineHeight: 1.2
    letterSpacing: "normal"
  body:
    fontFamily: "Nunito, sans-serif"
    fontSize: "1rem"
    fontWeight: 400
    lineHeight: 1.65
    letterSpacing: "normal"
  label:
    fontFamily: "Nunito, sans-serif"
    fontSize: "0.76rem"
    fontWeight: 900
    lineHeight: 1.2
    letterSpacing: "0.16em"
rounded:
  media: "8px"
  menu-item: "12px"
  menu: "18px"
  photo-card: "22px"
spacing:
  xs: "0.35rem"
  sm: "0.85rem"
  md: "1rem"
  lg: "2rem"
  xl: "3.75rem"
components:
  button-primary:
    backgroundColor: "{colors.green}"
    textColor: "{colors.paper-strong}"
    padding: "0.9rem 1.2rem"
  button-primary-hover:
    backgroundColor: "{colors.ink}"
    textColor: "{colors.paper-strong}"
    padding: "0.9rem 1.2rem"
  button-secondary:
    backgroundColor: "transparent"
    textColor: "{colors.ink}"
    padding: "0.9rem 1.2rem"
  card:
    backgroundColor: "{colors.paper}"
    textColor: "{colors.ink}"
    padding: "2rem"
---

# Design System: 1. SV Fasanenhof Website

## 1. Overview

**Creative North Star: "The Vereinsheim Noticeboard"**

The site should feel like a well-kept public club noticeboard translated into a modern web surface: warm paper tones, honest photography, clear routes, and stable typography. It is not a campaign microsite and not a software product. It exists to help people understand the Verein, choose a department, and arrive prepared.

The visual system uses a restrained warm neutral base with club green as the main signal and gold as a small heritage accent. Surfaces are structured and readable, with enough softness to feel local and welcoming. Department pages can carry more imagery and depth, especially Bogenschießen, but the system should never lose practical clarity.

**Key Characteristics:**

- Warm neutral background, green navigational emphasis, occasional gold accent.
- Editorial serif headings paired with a rounded, readable sans serif body.
- Real images of the Gelände, teams, training, and Bogensport instead of abstract decoration.
- Department-first navigation with strong scanning cues.
- Public-service clarity over promotional polish.

## 2. Colors

The palette is warm, grounded, and Vereinsheim-like: cream paper, deep green, muted ink, and restrained gold.

### Primary

- **Vereinsgrün** (`#2f6b3b`): Primary navigation, active states, links, buttons, key labels, and focus borders.
- **Tiefgrün** (`#173c1f`): Reserved for stronger green emphasis or deep contrast when `#2f6b3b` is too light.

### Secondary

- **Jubiläumsgold** (`#d6ab45`): Heritage accent for subtle highlights, quotes, and small celebratory details. Use sparingly.

### Neutral

- **Geländesand** (`#f5f1e8`): Base page background.
- **Warmpapier** (`#fffdf8`): Main surface color for hero copy and content panels.
- **Reinpapier** (`#ffffff`): Strongest surface, mostly for media and search results.
- **Vereinsink** (`#151515`): Primary text and dark footer surface.
- **Weichink** (`#3f3f3f`): Body-adjacent copy and quieter supporting text.
- **Notizgrau** (`#66604f`): Muted metadata, labels, and secondary brand text.
- **Linie** (`#ddd5c6`): Borders, dividers, table rules, and structural separation.
- **Fußzeile Muted** (`#d9d4c8`): Footer secondary text.

### Named Rules

**The Green Is a Route Rule.** Use green where it helps visitors move, identify active location, or act. Do not turn entire sections green by default.

**The Gold Is Heritage Rule.** Gold should feel like a 60-year club accent, not a luxury palette.

## 3. Typography

**Display Font:** Merriweather, with serif fallback  
**Body Font:** Nunito, with sans-serif fallback  
**Label/Mono Font:** Nunito, with sans-serif fallback

**Character:** Merriweather gives the site a civic, editorial tone suited to history, Verein, and long-form department content. Nunito keeps navigation, body copy, and practical information friendly and readable.

### Hierarchy

- **Display** (700, `clamp(2.6rem, 5vw, 4.8rem)`, `1.02`): Home and major page hero headlines only.
- **Headline** (700, `clamp(2rem, 4vw, 3.3rem)`, `1.05`): Section headings and department feature headings.
- **Title** (700, `1.55rem`, `1.2`): Card, news, and row titles.
- **Body** (400, `1rem`, `1.65`): Default site text. Keep long prose around 65 to 75 characters where layout allows.
- **Lead** (400, `1.2rem`, inherited line height): Introductory page copy, capped near `40rem`.
- **Label** (900, `0.76rem`, `0.16em`, uppercase): Kicker text, pills, metadata, breadcrumbs, and dates.

### Named Rules

**The Serif Earns Space Rule.** Use Merriweather for hierarchy and identity, not for dense navigation or every small label.

## 4. Elevation

The system uses a hybrid of borders, warm surfaces, and soft ambient shadows. Elevation should make content readable against the warm background, not create a floating app-card look. Most depth comes from a one-pixel warm border plus low-opacity shadow.

### Shadow Vocabulary

- **Ambient Surface** (`0 20px 60px rgba(21, 21, 21, 0.08)`): Main hero copy, feature cards, news cards, prose blocks, news lists, and rows.
- **Raised Menu** (`0 18px 44px rgba(21, 21, 21, 0.1), 0 3px 10px rgba(21, 21, 21, 0.05)`): Dropdown menus and image media blocks.
- **Photo Surface** (`0 16px 38px rgba(21, 21, 21, 0.08)`): Prose images, gallery figures, and year cards.
- **Focus Ring** (`0 0 0 4px rgba(47, 107, 59, 0.14)`): Search inputs and form focus states.

### Named Rules

**The Place Before Polish Rule.** If elevation makes the site feel like a SaaS dashboard, remove shadow and use border, spacing, or imagery instead.

## 5. Components

### Buttons

- **Shape:** Square-cornered text buttons by default, no decorative pill styling in the current implementation.
- **Primary:** `#2f6b3b` background, white text, `0.9rem 1.2rem` padding, `900` weight.
- **Hover / Focus:** Translate up by `1px`; primary hover switches to `#151515`.
- **Secondary:** Transparent background with `2px` ink border and ink text; hover fills ink with white text.

### Cards / Containers

- **Corner Style:** Main content cards are squared by default. Media cards use `8px`; gallery/photo cards use `22px`.
- **Background:** `rgba(255,255,255,0.82)` for cards; `#fffdf8` for hero paper surfaces.
- **Shadow Strategy:** Use Ambient Surface only for major content blocks.
- **Border:** `1px solid #ddd5c6`.
- **Internal Padding:** `2rem` desktop, `1.35rem` mobile.

### Inputs / Fields

- **Style:** Full-width field, `2px` warm border, `#ffffff` background, inherited font, `0.8rem 1rem` padding.
- **Focus:** Border switches to Vereinsgrün with a soft green focus ring.
- **Error / Disabled:** Not yet defined in shipped V1. Add explicit states before introducing forms beyond search.

### Navigation

- **Header:** Sticky, lightly translucent paper background, bottom border, and subtle shadow.
- **Brand:** Logo plus Merriweather name, italic green emphasis, uppercase small descriptor.
- **Main nav:** Bold Nunito links with green active and hover underline.
- **Section nav:** Department and section dropdowns use inline summaries, warm menu surfaces, `12px` item radius, and small horizontal motion on hover.
- **Mobile:** Header stacks vertically; nav wraps instead of hiding core routes.

### Image Blocks

- **Department media:** Fixed aspect ratio `16 / 10`, `8px` radius, real imagery, cover fit.
- **Photo showcase:** Dense gallery layouts with captions and actual club images.
- **Rule:** Images must reveal the real club, department, training, Gelände, teams, or venue.

## 6. Do's and Don'ts

### Do:

- **Do** keep the warm neutral base and use `#2f6b3b` as the main route and action color.
- **Do** use real German with umlauts in public copy.
- **Do** prefer actual Vereinsgelände, team, training, and Bogensport imagery over illustration.
- **Do** keep Fußball and Tischtennis concise until real content exists.
- **Do** update `SITE-SPEC.md` whenever IA, navigation, or page structure changes.
- **Do** preserve clear focus states and readable contrast.

### Don't:

- **Don't** add migration notes, placeholders, curation comments, or internal editorial process to production copy.
- **Don't** use generic sports-club templates, neon performance-sports styling, dark betting-site energy, glossy startup marketing, or premium lifestyle-brand polish.
- **Don't** use gradient text, decorative glassmorphism, side-stripe card accents, or hero-metric templates.
- **Don't** turn every page into identical icon-card grids.
- **Don't** invent department content when real source material is missing.
- **Don't** bury practical routes such as Training, Mitglied werden, Kontakt, Standort, or Schnupperkurse behind decorative sections.
