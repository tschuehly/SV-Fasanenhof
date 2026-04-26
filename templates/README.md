# Templates

These files contain reusable HTML structure for the static site generator.

The canonical editable page content stays in `content/**/*.md`. The Kotlin script parses Markdown, derives generated structures such as breadcrumbs, news lists, search data and relative links, then composes those values into these templates.

Template placeholders use `{{name}}` slots. Unknown slots fail the build, so keep slot names explicit and verify changes with:

```bash
kotlin build.main.kts
```

Current split:

- `layout.html`: complete HTML document shell.
- `partials/header.html`: brand and main navigation.
- `partials/footer.html`: footer link groups.
- `partials/hero.html`: page hero from Markdown frontmatter.
- `partials/section-nav.html`: generated section navigation.
- `pages/page.html`: default Markdown page.
- `pages/home.html`: home composition around Markdown body content.
- `pages/news-index.html`: Markdown intro plus generated news list.
- `pages/search.html`: Markdown intro plus generated search UI.
