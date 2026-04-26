#!/usr/bin/env kotlin

@file:DependsOn("org.commonmark:commonmark:0.24.0")
@file:DependsOn("org.commonmark:commonmark-ext-gfm-tables:0.24.0")
@file:DependsOn("org.commonmark:commonmark-ext-autolink:0.24.0")
@file:OptIn(kotlin.io.path.ExperimentalPathApi::class)

import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.io.path.writeText

val projectRoot = __FILE__.absoluteFile.parentFile.toPath()
val contentRoot = projectRoot.resolve("content")
val outputRoot = projectRoot.resolve("build/site")
val assetsOutput = outputRoot.resolve("assets")
val liveReload = args.contains("--live-reload")

val parser = Parser.builder()
    .extensions(listOf(TablesExtension.create(), AutolinkExtension.create()))
    .build()

val renderer = HtmlRenderer.builder()
    .extensions(listOf(TablesExtension.create(), AutolinkExtension.create()))
    .escapeHtml(false)
    .build()

data class MarkdownDocument(
    val frontmatter: Map<String, String>,
    val body: String,
)

data class Page(
    val source: Path,
    val relativeSource: Path,
    val urlPath: String,
    val outputRelativePath: Path,
    val outputPath: Path,
    val title: String,
    val description: String,
    val kicker: String,
    val lead: String,
    val template: String,
    val sectionKey: String,
    val bodyHtml: String,
    val summary: String,
)

data class NewsPost(
    val page: Page,
    val departmentKey: String,
    val departmentLabel: String,
    val date: LocalDate,
    val dateLabel: String,
)

data class HomeDepartment(
    val title: String,
    val kicker: String,
    val text: String,
    val url: String,
    val image: String,
    val imageAlt: String,
    val primaryLabel: String,
    val links: List<SectionNavEntry>,
)

data class SearchItem(
    val title: String,
    val url: String,
    val section: String,
    val summary: String,
    val text: String,
)

data class NavItem(
    val label: String,
    val url: String,
    val sectionKey: String,
)

data class Crumb(
    val label: String,
    val url: String,
)

data class SectionNavEntry(
    val label: String,
    val url: String,
)

data class SectionNavGroup(
    val label: String,
    val entries: List<SectionNavEntry>,
)

val navItems = listOf(
    NavItem("Verein", "/", "verein"),
    NavItem("Bogenschießen", "/bogenschiessen/", "bogenschiessen"),
    NavItem("Fußball", "/fussball/", "fussball"),
    NavItem("Tischtennis", "/tischtennis/", "tischtennis"),
    NavItem("Kontakt", "/kontakt/", "kontakt"),
)

val sectionNavGroups = mapOf(
    "verein" to listOf(
        SectionNavGroup(
            "Standort",
            listOf(SectionNavEntry("Standort", "/verein/standort.html")),
        ),
        SectionNavGroup(
            "Beiträge",
            listOf(SectionNavEntry("Beiträge", "/verein/beitraege.html")),
        ),
        SectionNavGroup(
            "Vereinsleben",
            listOf(
                SectionNavEntry("Historie", "/verein/historie.html"),
                SectionNavEntry("Gaststätte", "/verein/gaststaette.html"),
                SectionNavEntry("FAQ", "/verein/faq.html"),
            ),
        ),
        SectionNavGroup(
            "Organisation",
            listOf(
                SectionNavEntry("Vorstand", "/verein/vorstand.html"),
                SectionNavEntry("Satzung und Ordnungen", "/verein/satzung.html"),
            ),
        ),
    ),
    "bogenschiessen" to listOf(
        SectionNavGroup(
            "Mitmachen",
            listOf(
                SectionNavEntry("Training", "/bogenschiessen/training.html"),
                SectionNavEntry("Schnupperkurse", "/bogenschiessen/schnupperkurse.html"),
                SectionNavEntry("Trainer und Übungsleiter", "/bogenschiessen/trainer.html"),
                SectionNavEntry("Ausrüstung", "/bogenschiessen/ausruestung.html"),
                SectionNavEntry("FAQ", "/bogenschiessen/faq.html"),
                SectionNavEntry("Schießordnung", "/bogenschiessen/schiessordnung.html"),
            ),
        ),
        SectionNavGroup(
            "Wettkampf",
            listOf(
                SectionNavEntry("Meisterschaften", "/bogenschiessen/meisterschaften/"),
                SectionNavEntry("Galerie", "/bogenschiessen/galerie/"),
            ),
        ),
        SectionNavGroup(
            "Aktuelles",
            listOf(SectionNavEntry("Aktuelles", "/bogenschiessen/aktuelles/")),
        ),
        SectionNavGroup(
            "Abteilung",
            listOf(
                SectionNavEntry("Wissen", "/bogenschiessen/wissen/"),
                SectionNavEntry("Links und Verbände", "/bogenschiessen/links-und-verbaende.html"),
                SectionNavEntry("Presse", "/bogenschiessen/presse.html"),
                SectionNavEntry("Sponsoren", "/bogenschiessen/sponsoren.html"),
            ),
        ),
    ),
    "fussball" to listOf(
        SectionNavGroup(
            "Training",
            listOf(SectionNavEntry("Training", "/fussball/training.html")),
        ),
        SectionNavGroup(
            "Mannschaften",
            listOf(SectionNavEntry("Mannschaften", "/fussball/mannschaften.html")),
        ),
        SectionNavGroup(
            "Aktuelles",
            listOf(SectionNavEntry("Aktuelles", "/fussball/aktuelles/")),
        ),
        SectionNavGroup(
            "Abteilung",
            listOf(
                SectionNavEntry("Sponsoren", "/fussball/sponsoren.html"),
                SectionNavEntry("FAQ", "/fussball/faq.html"),
            ),
        ),
    ),
    "tischtennis" to listOf(
        SectionNavGroup(
            "Training",
            listOf(SectionNavEntry("Training", "/tischtennis/training.html")),
        ),
        SectionNavGroup(
            "Aktuelles",
            listOf(SectionNavEntry("Aktuelles", "/tischtennis/aktuelles/")),
        ),
        SectionNavGroup(
            "Infos",
            listOf(
                SectionNavEntry("FAQ", "/tischtennis/faq.html"),
            ),
        ),
    ),
)

generateSite()

fun generateSite() {
    if (outputRoot.exists()) {
        outputRoot.deleteRecursively()
    }
    assetsOutput.createDirectories()

    val pages = loadPages()
    val pagesByUrl = pages.associateBy(Page::urlPath)
    val newsPosts = pages
        .filter { it.relativeSource.invariantSeparatorsPathString.contains("/aktuelles/posts/") }
        .map { page ->
            val date = LocalDate.parse(page.source.nameWithoutExtension.take(10))
            val departmentKey = page.relativeSource.getName(0).toString()
            NewsPost(
                page = page,
                departmentKey = departmentKey,
                departmentLabel = departmentLabel(departmentKey),
                date = date,
                dateLabel = date.format(DateTimeFormatter.ofPattern("d. MMMM yyyy", Locale.GERMAN)),
            )
        }
        .sortedByDescending(NewsPost::date)

    copyStaticAssets()

    pages.forEach { page ->
        val html = renderPage(page, pagesByUrl, newsPosts)
        page.outputPath.parent.createDirectories()
        page.outputPath.writeText(html)
    }

    writeSearchIndex(pages)
    outputRoot.resolve(".nojekyll").writeText("")
    println("Generated ${pages.size} pages into ${outputRoot.absolutePathString()}")
}

fun loadPages(): List<Page> {
    return Files.walk(contentRoot)
        .filter { Files.isRegularFile(it) && it.extension == "md" }
        .filter { path -> !path.relativeTo(contentRoot).invariantSeparatorsPathString.startsWith(".") }
        .sorted(compareBy<Path> { it.invariantSeparatorsPathString })
        .map { source ->
            val relative = source.relativeTo(contentRoot)
            val document = parseMarkdown(source.readText())
            val urlPath = urlPathFor(relative)
            val outputRelativePath = outputPathFor(relative)
            val outputPath = outputRoot.resolve(outputRelativePath)
            val title = document.frontmatter["title"] ?: titleFromFilename(relative)
            val description = document.frontmatter["description"] ?: ""
            val kicker = document.frontmatter["kicker"] ?: title
            val lead = document.frontmatter["lead"] ?: description
            val template = document.frontmatter["template"] ?: inferredTemplate(relative)
            val sectionKey = sectionKeyFor(relative)
            Page(
                source = source,
                relativeSource = relative,
                urlPath = urlPath,
                outputRelativePath = outputRelativePath,
                outputPath = outputPath,
                title = title,
                description = description,
                kicker = kicker,
                lead = lead,
                template = template,
                sectionKey = sectionKey,
                bodyHtml = renderer.render(parser.parse(document.body.trim())),
                summary = document.frontmatter["summary"] ?: excerpt(document.body),
            )
        }
        .toList()
}

fun parseMarkdown(raw: String): MarkdownDocument {
    if (!raw.startsWith("---")) {
        return MarkdownDocument(emptyMap(), raw.trim())
    }

    val lines = raw.lines()
    if (lines.size < 3) {
        return MarkdownDocument(emptyMap(), raw.trim())
    }

    val frontmatterLines = mutableListOf<String>()
    var index = 1
    while (index < lines.size && lines[index] != "---") {
        frontmatterLines += lines[index]
        index++
    }
    if (index >= lines.size) {
        return MarkdownDocument(emptyMap(), raw.trim())
    }

    val meta = frontmatterLines
        .mapNotNull { line ->
            val separator = line.indexOf(':')
            if (separator <= 0) return@mapNotNull null
            line.substring(0, separator).trim() to line.substring(separator + 1).trim().trim('"')
        }
        .toMap()

    val body = lines.drop(index + 1).joinToString("\n").trim()
    return MarkdownDocument(meta, body)
}

fun renderPage(page: Page, pagesByUrl: Map<String, Page>, allNewsPosts: List<NewsPost>): String {
    val content = when (page.template) {
        "home" -> renderHome(page, pagesByUrl, allNewsPosts)
        "news-index" -> renderNewsIndex(page, newsPostsForPage(page, allNewsPosts))
        "search" -> renderSearchPage(page)
        else -> renderStandardPage(page, pagesByUrl)
    }

    return """
        <!DOCTYPE html>
        <html lang="de">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>${escapeHtml(page.title)} — 1. SV Fasanenhof 1965 e.V.</title>
          <meta name="description" content="${escapeHtml(page.description.ifBlank { page.summary })}">
          <link rel="preconnect" href="https://fonts.googleapis.com">
          <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
          <link href="https://fonts.googleapis.com/css2?family=Merriweather:ital,wght@0,400;0,700;0,900;1,400;1,700&family=Nunito:wght@400;600;700;800;900&display=swap" rel="stylesheet">
          <link rel="stylesheet" href="${linkTo(page, "/assets/site.css")}">
        </head>
        <body>
          ${renderHeader(page, pagesByUrl)}
          <main class="site-main">
            $content
          </main>
          ${renderFooter(page)}
          ${navigationScript()}
          ${liveReloadScript()}
        </body>
        </html>
    """.trimIndent()
}

fun newsPostsForPage(page: Page, allNewsPosts: List<NewsPost>): List<NewsPost> {
    return if (page.sectionKey == "aktuelles") {
        allNewsPosts
    } else {
        allNewsPosts.filter { it.departmentKey == page.sectionKey }
    }
}

fun renderHome(page: Page, pagesByUrl: Map<String, Page>, allNewsPosts: List<NewsPost>): String {
    val bodyHtml = rewriteEmbeddedAssetPaths(page, page.bodyHtml)
    val latestNews = allNewsPosts.take(4).joinToString("") { post ->
        """
            <article class="news-card">
              <div class="news-meta">${escapeHtml(post.departmentLabel)} · ${escapeHtml(post.dateLabel)}</div>
              <h3><a href="${linkTo(page, post.page.urlPath)}">${escapeHtml(post.page.title)}</a></h3>
              <p>${escapeHtml(post.page.summary)}</p>
            </article>
        """.trimIndent()
    }

    val departmentSections = listOf(
        HomeDepartment(
            title = "Bogenschießen",
            kicker = "Bogensportabteilung",
            text = "Training, Schnupperkurse, Meisterschaften und Wissensseiten zeigen die ganze Breite der Bogensportabteilung.",
            url = "/bogenschiessen/",
            image = "/assets/bogenschiessen/landing/vereinspokal-2024.jpg",
            imageAlt = "Gruppenfoto der Bogensportabteilung beim Vereinspokal auf dem Vereinsgelände",
            primaryLabel = "Zum Bogenschießen",
            links = listOf(
                SectionNavEntry("Training", "/bogenschiessen/training.html"),
                SectionNavEntry("Schnupperkurse", "/bogenschiessen/schnupperkurse.html"),
                SectionNavEntry("Meisterschaften", "/bogenschiessen/meisterschaften/"),
            ),
        ),
        HomeDepartment(
            title = "Fußball",
            kicker = "Fußball am Logauweg",
            text = "Die Fußballabteilung nutzt den Kunstrasenplatz am Vereinsgelände und bündelt Aktive, Jugendgruppen und Spielgemeinschaften.",
            url = "/fussball/",
            image = "/assets/home/fussball-1-mannschaft.jpg",
            imageAlt = "Gruppenfoto der 1. Fußballmannschaft des 1. SV Fasanenhof",
            primaryLabel = "Zum Fußball",
            links = listOf(
                SectionNavEntry("Mannschaften", "/fussball/mannschaften.html"),
                SectionNavEntry("Training", "/fussball/training.html"),
                SectionNavEntry("Aktuelles", "/fussball/aktuelles/"),
            ),
        ),
        HomeDepartment(
            title = "Tischtennis",
            kicker = "Tischtennis in Möhringen",
            text = "Tischtennis verbindet regelmäßiges Training für Jung und Alt mit sportlichem Ehrgeiz und dem aktuellen Aufstieg in die A-Klasse.",
            url = "/tischtennis/",
            image = "/assets/home/tischtennis-team.jpg",
            imageAlt = "Tischtennismannschaft des 1. SV Fasanenhof in der Halle",
            primaryLabel = "Zum Tischtennis",
            links = listOf(
                SectionNavEntry("Training", "/tischtennis/training.html"),
                SectionNavEntry("Aktuelles", "/tischtennis/aktuelles/"),
                SectionNavEntry("FAQ", "/tischtennis/faq.html"),
            ),
        ),
    ).mapIndexed { index, department ->
        val reverse = if (index % 2 == 1) " department-layout-reverse" else ""
        val linksHtml = department.links.joinToString("") { link ->
            """<a href="${linkTo(page, link.url)}">${escapeHtml(link.label)}</a>"""
        }
        """
            <section class="department-band">
              <div class="shell department-layout$reverse">
                <figure class="department-media">
                  <img src="${linkTo(page, department.image)}" alt="${escapeHtml(department.imageAlt)}">
                </figure>
                <div class="department-copy">
                  <span class="kicker">${escapeHtml(department.kicker)}</span>
                  <h3>${escapeHtml(department.title)}</h3>
                  <p>${escapeHtml(department.text)}</p>
                  <div class="department-actions">
                    <a class="button button-primary" href="${linkTo(page, department.url)}">${escapeHtml(department.primaryLabel)}</a>
                    <div class="department-text-links">$linksHtml</div>
                  </div>
                </div>
              </div>
            </section>
        """.trimIndent()
    }.joinToString("")

    val venueSnippet = "Am Logauweg 21 liegen Kunstrasenplatz, Vereinsheim, Terrasse und Vereinsgaststätte direkt beieinander. Die Anlage ist mit der U6 über den Europaplatz erreichbar; vor Ort gibt es Parkplätze für Training, Spieltage und Gäste."
    val mapsUrl = "https://maps.app.goo.gl/HWWacL3amrz386ir8"

    return """
        <section class="hero shell">
          <div class="hero-copy">
            <span class="kicker">${escapeHtml(page.kicker)}</span>
            <h1>${escapeHtml(page.title)}</h1>
            <p class="lead">${escapeHtml(page.lead)}</p>
          </div>
        </section>

        <section class="shell section department-intro">
          <div class="section-head">
            <span class="kicker">Abteilungen</span>
            <h2>Drei Abteilungen, ein Verein</h2>
          </div>
        </section>
        $departmentSections

        <section class="shell section">
          <div class="section-head">
            <span class="kicker">Aktuelles</span>
            <h2><a href="${linkTo(page, "/aktuelles/")}">Neuigkeiten aus den Abteilungen</a></h2>
          </div>
          <div class="news-grid">$latestNews</div>
        </section>

        <section class="shell section training-teaser">
          <article class="feature-card">
            <span class="pill">Training</span>
            <h3><a href="${linkTo(page, "/bogenschiessen/training.html")}">Zeiten und Orte im Blick</a></h3>
            <p>Für den Bogensport sind Außenplatz, Schulhalle und Wintertraining klar gegliedert. So sieht man auf einen Blick, wann am Logauweg oder in den Hallen trainiert wird.</p>
          </article>
        </section>

        <section class="location-band">
          <div class="shell location-layout">
            <figure class="location-media">
              <img src="${linkTo(page, "/assets/home/vereinsheim.jpg")}" alt="Vereinsheim des 1. SV Fasanenhof mit Terrasse am Logauweg">
            </figure>
            <div class="location-copy">
              <span class="kicker">Standort</span>
              <h2>Logauweg 21 als gemeinsamer Treffpunkt</h2>
              <p>${escapeHtml(venueSnippet)}</p>
              <div class="location-actions">
                <a class="button button-primary" href="${linkTo(page, "/verein/standort.html")}">Standort ansehen</a>
                <a class="button button-secondary" href="$mapsUrl" target="_blank" rel="noopener">Route in Google Maps</a>
              </div>
            </div>
          </div>
        </section>

        <section class="shell prose-block home-summary">
          $bodyHtml
        </section>
    """.trimIndent()
}

fun renderNewsIndex(page: Page, posts: List<NewsPost>): String {
    val bodyHtml = rewriteEmbeddedAssetPaths(page, page.bodyHtml)
    val newsHtml = if (posts.isEmpty()) {
        """<p>Aktuell sind hier noch keine Meldungen veröffentlicht.</p>"""
    } else {
        posts.joinToString("") { post ->
            val meta = if (page.sectionKey == "aktuelles") {
                "${post.departmentLabel} · ${post.dateLabel}"
            } else {
                post.dateLabel
            }
            """
                <article class="news-row">
                  <div class="news-date">${escapeHtml(meta)}</div>
                  <div>
                    <h3><a href="${linkTo(page, post.page.urlPath)}">${escapeHtml(post.page.title)}</a></h3>
                    <p>${escapeHtml(post.page.summary)}</p>
                  </div>
                </article>
            """.trimIndent()
        }
    }

    return """
        ${renderHero(page)}
        <section class="shell prose-block">
          $bodyHtml
        </section>
        <section class="shell section">
          <div class="section-head">
            <span class="kicker">Beiträge</span>
            <h2>Chronologische Meldungen</h2>
          </div>
          <div class="news-list">$newsHtml</div>
        </section>
    """.trimIndent()
}

fun renderSearchPage(page: Page): String {
    val bodyHtml = rewriteEmbeddedAssetPaths(page, page.bodyHtml)
    return """
        ${renderHero(page)}
        <section class="shell prose-block">
          ${renderBreadcrumbs(page)}
          <article class="prose">
            $bodyHtml
            <form class="search-form" role="search">
              <label for="site-search">Suchbegriff</label>
              <input id="site-search" type="search" autocomplete="off" placeholder="Training, Beiträge, Kontakt ...">
            </form>
            <div class="search-status" aria-live="polite"></div>
            <div class="search-results"></div>
          </article>
        </section>
        <script>
          (function () {
            var input = document.getElementById('site-search');
            var status = document.querySelector('.search-status');
            var results = document.querySelector('.search-results');
            if (!input || !status || !results) return;

            var pages = [];
            var searchIndexUrl = '${linkTo(page, "/search-index.json")}';
            var siteRoot = new URL(searchIndexUrl, window.location.href);
            siteRoot.pathname = siteRoot.pathname.replace(/search-index\.json$/, '');

            function normalize(value) {
              return value.toLocaleLowerCase('de-DE').normalize('NFD').replace(/[\u0300-\u036f]/g, '');
            }

            function linkFor(url) {
              return new URL(url.replace(/^\/+/, ''), siteRoot).toString();
            }

            function render(matches, query) {
              results.innerHTML = '';
              if (!query) {
                status.textContent = 'Suchbegriff eingeben.';
                return;
              }
              if (!matches.length) {
                status.textContent = 'Keine Treffer gefunden.';
                return;
              }
              status.textContent = matches.length + (matches.length === 1 ? ' Treffer' : ' Treffer');
              matches.slice(0, 12).forEach(function (item) {
                var article = document.createElement('article');
                var section = document.createElement('span');
                var heading = document.createElement('h3');
                var link = document.createElement('a');
                var summary = document.createElement('p');
                article.className = 'search-result';
                section.textContent = item.section;
                link.href = linkFor(item.url);
                link.textContent = item.title;
                summary.textContent = item.summary;
                heading.appendChild(link);
                article.appendChild(section);
                article.appendChild(heading);
                article.appendChild(summary);
                results.appendChild(article);
              });
            }

            function runSearch() {
              var query = input.value.trim();
              var terms = normalize(query).split(/\s+/).filter(Boolean);
              if (!terms.length) {
                render([], '');
                return;
              }
              var matches = pages
                .map(function (item) {
                  var haystack = normalize([item.title, item.section, item.summary, item.text].join(' '));
                  var score = terms.reduce(function (sum, term) {
                    if (normalize(item.title).indexOf(term) >= 0) return sum + 4;
                    if (normalize(item.summary).indexOf(term) >= 0) return sum + 2;
                    if (haystack.indexOf(term) >= 0) return sum + 1;
                    return sum;
                  }, 0);
                  return { item: item, score: score };
                })
                .filter(function (match) { return match.score >= terms.length; })
                .sort(function (a, b) { return b.score - a.score || a.item.title.localeCompare(b.item.title, 'de-DE'); })
                .map(function (match) { return match.item; });
              render(matches, query);
            }

            fetch(searchIndexUrl)
              .then(function (response) { return response.json(); })
              .then(function (data) {
                pages = data;
                runSearch();
                input.addEventListener('input', runSearch);
              })
              .catch(function () {
                status.textContent = 'Die Suche konnte nicht geladen werden.';
              });
          })();
        </script>
    """.trimIndent()
}

fun writeSearchIndex(pages: List<Page>) {
    val items = pages
        .filterNot { it.template == "search" }
        .map { page ->
            SearchItem(
                title = page.title,
                url = page.urlPath,
                section = sectionLabel(page.sectionKey),
                summary = page.summary,
                text = searchableText(page),
            )
        }
    val json = items.joinToString(prefix = "[\n", postfix = "\n]", separator = ",\n") { item ->
        """
          {
            "title": "${jsonEscape(item.title)}",
            "url": "${jsonEscape(item.url)}",
            "section": "${jsonEscape(item.section)}",
            "summary": "${jsonEscape(item.summary)}",
            "text": "${jsonEscape(item.text)}"
          }
        """.trimIndent()
    }
    outputRoot.resolve("search-index.json").writeText(json)
}

fun searchableText(page: Page): String {
    return page.bodyHtml
        .replace(Regex("<script[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), " ")
        .replace(Regex("<style[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), " ")
        .replace(Regex("<[^>]+>"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .take(1400)
}

fun renderStandardPage(page: Page, pagesByUrl: Map<String, Page>): String {
    val bodyHtml = rewriteEmbeddedAssetPaths(page, page.bodyHtml)
    val sectionDirectory = renderSectionDirectory(page, pagesByUrl)
    return """
        ${renderHero(page)}
        <section class="shell prose-block">
          ${renderBreadcrumbs(page)}
          <article class="prose">
            $bodyHtml
            $sectionDirectory
          </article>
        </section>
    """.trimIndent()
}

fun renderSectionDirectory(page: Page, pagesByUrl: Map<String, Page>): String {
    val groups = sectionNavGroups[page.sectionKey] ?: return ""
    val sectionRoot = navItems.firstOrNull { it.sectionKey == page.sectionKey }?.url ?: return ""
    if (page.urlPath != sectionRoot) return ""

    val groupsHtml = groups.mapNotNull { group ->
        val entries = group.entries
            .filter { it.url != page.urlPath }
            .mapNotNull { entry ->
                val target = pagesByUrl[entry.url] ?: return@mapNotNull null
                """
                    <li>
                      <a href="${linkTo(page, target.urlPath)}">${escapeHtml(entry.label)}</a>
                      <span>${escapeHtml(target.summary)}</span>
                    </li>
                """.trimIndent()
            }

        if (entries.isEmpty()) return@mapNotNull null

        """
            <div class="section-directory-group">
              <h3>${escapeHtml(group.label)}</h3>
              <ul>${entries.joinToString("")}</ul>
            </div>
        """.trimIndent()
    }

    if (groupsHtml.isEmpty()) return ""

    return """
        <nav class="section-directory" aria-labelledby="section-directory-title">
          <h2 id="section-directory-title">Alle Seiten in diesem Bereich</h2>
          <div class="section-directory-grid">
            ${groupsHtml.joinToString("")}
          </div>
        </nav>
    """.trimIndent()
}

fun renderHero(page: Page): String {
    return """
        <section class="hero shell">
          <div class="hero-copy">
            <span class="kicker">${escapeHtml(page.kicker)}</span>
            <h1>${escapeHtml(page.title)}</h1>
            <p class="lead">${escapeHtml(page.lead)}</p>
          </div>
        </section>
    """.trimIndent()
}

fun renderHeader(page: Page, pagesByUrl: Map<String, Page>): String {
    val navHtml = navItems.joinToString("") { item ->
        val active = if (item.sectionKey == page.sectionKey) "active" else ""
        """<li><a class="$active" href="${linkTo(page, item.url)}">${escapeHtml(item.label)}</a></li>"""
    }
    val sectionNav = renderSectionNav(page, pagesByUrl)
    return """
        <header class="site-header">
          <div class="shell header-shell">
            <a class="brand" href="${linkTo(page, "/")}">
              <img src="${linkTo(page, "/assets/logo.png")}" alt="Logo 1. SV Fasanenhof">
              <span>
                <strong>1. SV Fasanenhof <em>1965 e.V.</em></strong>
                <small>Sport und Gemeinschaft im Fasanenhof</small>
              </span>
            </a>
            <nav class="site-nav" aria-label="Hauptnavigation">
              <ul>$navHtml</ul>
            </nav>
          </div>
        </header>
        $sectionNav
    """.trimIndent()
}

fun renderSectionNav(page: Page, pagesByUrl: Map<String, Page>): String {
    val groups = sectionNavEntriesFor(page, pagesByUrl)
    if (groups.isEmpty()) return ""

    val desktopLinksHtml = groups.flatMap { it.entries }.distinctBy { it.url }.joinToString("") { entry ->
        val active = if (isSectionNavActive(page.urlPath, entry.url)) "active" else ""
        """<li><a class="$active" href="${linkTo(page, entry.url)}">${escapeHtml(entry.label)}</a></li>"""
    }
    val desktopNavHtml = """<ul class="section-nav-flat section-nav-desktop">$desktopLinksHtml</ul>"""

    val mobileNavHtml = groups.joinToString("") { group ->
        val groupActive = group.entries.any { entry -> isSectionNavActive(page.urlPath, entry.url) }
        val linksHtml = group.entries.joinToString("") { entry ->
            val active = if (isSectionNavActive(page.urlPath, entry.url)) "active" else ""
            """<li><a class="$active" href="${linkTo(page, entry.url)}">${escapeHtml(entry.label)}</a></li>"""
        }
        if (group.entries.size == 1) {
            """<ul class="section-nav-flat section-nav-mobile">$linksHtml</ul>"""
        } else {
        val summaryActive = if (groupActive) "active" else ""
        """
            <details class="section-nav-group">
              <summary class="$summaryActive">${escapeHtml(group.label)}</summary>
              <ul class="section-nav-menu">$linksHtml</ul>
            </details>
        """.trimIndent()
        }
    }

    return """
        <nav class="section-nav section-nav-${page.sectionKey}" aria-label="Bereichsnavigation">
          <div class="shell section-nav-shell">
            $desktopNavHtml
            <div class="section-nav-mobile-groups">
              $mobileNavHtml
            </div>
          </div>
        </nav>
    """.trimIndent()
}

fun sectionNavEntriesFor(page: Page, pagesByUrl: Map<String, Page>): List<SectionNavGroup> {
    val groups = sectionNavGroups[page.sectionKey] ?: return emptyList()
    return groups.mapNotNull { group ->
        val entries = group.entries.mapNotNull { entry ->
            pagesByUrl[entry.url] ?: return@mapNotNull null
            SectionNavEntry(entry.label, entry.url)
        }
        if (entries.isEmpty()) null else SectionNavGroup(group.label, entries)
    }
}

fun isSectionNavActive(currentUrl: String, targetUrl: String): Boolean {
    if (currentUrl == targetUrl) return true
    if (!targetUrl.endsWith("/")) return false
    val depth = targetUrl.trim('/').split('/').filter { it.isNotBlank() }.size
    return depth > 1 && currentUrl.startsWith(targetUrl)
}

fun renderFooter(page: Page): String {
    return """
        <footer class="site-footer">
          <div class="shell footer-grid">
            <div>
              <h3>1. SV Fasanenhof 1965 e.V.</h3>
              <p>Logauweg 21, 70565 Stuttgart. Gemeinsamer Auftritt für Verein, Fußball, Tischtennis und Bogensport.</p>
            </div>
            <div>
              <h4>Schnellzugriff</h4>
              <ul>
                <li><a href="${linkTo(page, "/aktuelles/")}">Aktuelles</a></li>
                <li><a href="${linkTo(page, "/termine/")}">Termine</a></li>
                <li><a href="${linkTo(page, "/mitglied-werden/")}">Mitglied werden</a></li>
                <li><a href="${linkTo(page, "/")}">Verein</a></li>
                <li><a href="${linkTo(page, "/verein/standort.html")}">Standort</a></li>
                <li><a href="${linkTo(page, "/kontakt/")}">Kontakt</a></li>
                <li><a href="${linkTo(page, "/suche/")}">Suche</a></li>
              </ul>
            </div>
            <div>
              <h4>Abteilungen</h4>
              <ul>
                <li><a href="${linkTo(page, "/fussball/")}">Fußball</a></li>
                <li><a href="${linkTo(page, "/tischtennis/")}">Tischtennis</a></li>
                <li><a href="${linkTo(page, "/bogenschiessen/")}">Bogenschießen</a></li>
              </ul>
            </div>
            <div>
              <h4>Rechtliches</h4>
              <ul>
                <li><a href="${linkTo(page, "/impressum/")}">Impressum</a></li>
                <li><a href="${linkTo(page, "/datenschutz/")}">Datenschutz</a></li>
                <li><a href="${linkTo(page, "/barrierefreiheit/")}">Barrierefreiheit</a></li>
                <li><a href="${linkTo(page, "/jugendschutz/")}">Jugendschutz</a></li>
              </ul>
            </div>
          </div>
        </footer>
    """.trimIndent()
}

fun renderBreadcrumbs(page: Page): String {
    val crumbs = breadcrumbsFor(page).joinToString("""<span class="crumb-sep">/</span>""") { crumb ->
        if (crumb.url == page.urlPath) """<span>${escapeHtml(crumb.label)}</span>"""
        else """<a href="${linkTo(page, crumb.url)}">${escapeHtml(crumb.label)}</a>"""
    }
    return """<nav class="breadcrumbs" aria-label="Brotkrumen">$crumbs</nav>"""
}

fun breadcrumbsFor(page: Page): List<Crumb> {
    val rawParts = page.urlPath.trim('/').split('/').filter { it.isNotBlank() }
    val parts = rawParts.filterNot { it == "posts" }
    if (page.urlPath == "/") return listOf(Crumb("Verein", "/"))

    val crumbs = mutableListOf(Crumb("Verein", "/"))
    var current = ""
    parts.forEachIndexed { index, part ->
        current += "/$part"
        if (index == 0 && part == "verein") return@forEachIndexed
        val isLast = index == parts.lastIndex
        val normalizedPart = part.removeSuffix(".html")
        val isFileLike = normalizedPart != part || (!isLast && rawParts.contains("${normalizedPart}.html"))
        val url = when {
            isLast -> page.urlPath
            isFileLike -> current
            else -> "$current/"
        }
        crumbs += Crumb(if (isLast) page.title else pathLabel(part), url)
    }
    return crumbs
}

fun sectionKeyFor(relative: Path): String {
    val first = relative.iterator().asSequence().firstOrNull()?.toString() ?: return "home"
    return if (first == "index.md") "verein" else first
}

fun inferredTemplate(relative: Path): String {
    val path = relative.invariantSeparatorsPathString
    return when {
        path == "index.md" -> "home"
        path.endsWith("/aktuelles/index.md") -> "news-index"
        else -> "page"
    }
}

fun titleFromFilename(relative: Path): String = pathLabel(relative.nameWithoutExtension)

fun urlPathFor(relative: Path): String {
    val normalized = relative.invariantSeparatorsPathString
    return if (normalized == "index.md") {
        "/"
    } else if (relative.name == "index.md") {
        "/" + relative.parent.invariantSeparatorsPathString + "/"
    } else {
        "/" + relative.parent?.invariantSeparatorsPathString?.let { if (it == ".") "" else "$it/" }.orEmpty() +
            relative.nameWithoutExtension + ".html"
    }.replace("//", "/")
}

fun outputPathFor(relative: Path): Path {
    return if (relative.invariantSeparatorsPathString == "index.md") {
        Path.of("index.html")
    } else if (relative.name == "index.md") {
        relative.parent.resolve("index.html")
    } else {
        relative.parent.resolve("${relative.nameWithoutExtension}.html")
    }
}

fun linkTo(fromPage: Page, targetUrl: String): String {
    val fromDir = fromPage.outputRelativePath.parent ?: Path.of("")
    val targetPath = when {
        targetUrl == "/" -> Path.of("index.html")
        targetUrl.endsWith("/") -> Path.of(targetUrl.trim('/')).resolve("index.html")
        else -> Path.of(targetUrl.trim('/'))
    }
    return fromDir.relativize(targetPath).invariantSeparatorsPathString
}

fun departmentLabel(key: String): String = when (key) {
    "bogenschiessen" -> "Bogenschießen"
    "fussball" -> "Fußball"
    "tischtennis" -> "Tischtennis"
    else -> key.replaceFirstChar { it.titlecase(Locale.GERMAN) }
}

fun sectionLabel(key: String): String = when (key) {
    "home" -> "Verein"
    "verein" -> "Verein"
    "standort" -> "Standort"
    "bogenschiessen" -> "Bogenschießen"
    "fussball" -> "Fußball"
    "tischtennis" -> "Tischtennis"
    "aktuelles" -> "Aktuelles"
    "termine" -> "Termine"
    "mitglied-werden" -> "Mitglied werden"
    "suche" -> "Suche"
    "barrierefreiheit" -> "Barrierefreiheit"
    "jugendschutz" -> "Jugendschutz"
    "kontakt" -> "Kontakt"
    "impressum" -> "Impressum"
    "datenschutz" -> "Datenschutz"
    else -> key.replace('-', ' ').replaceFirstChar { it.titlecase(Locale.GERMAN) }
}

fun pathLabel(part: String): String = when (part.removeSuffix(".html")) {
    "verein" -> "Verein"
    "standort" -> "Standort"
    "bogenschiessen" -> "Bogenschießen"
    "fussball" -> "Fußball"
    "tischtennis" -> "Tischtennis"
    "aktuelles" -> "Aktuelles"
    "termine" -> "Termine"
    "mitglied-werden" -> "Mitglied werden"
    "suche" -> "Suche"
    "barrierefreiheit" -> "Barrierefreiheit"
    "jugendschutz" -> "Jugendschutz"
    "kontakt" -> "Kontakt"
    "impressum" -> "Impressum"
    "datenschutz" -> "Datenschutz"
    "meisterschaften" -> "Meisterschaften"
    "archiv" -> "Archiv"
    "galerie" -> "Galerie"
    "wissen" -> "Wissen"
    "schnupperkurse" -> "Schnupperkurse"
    "ausruestung" -> "Ausrüstung"
    "schiessordnung" -> "Schießordnung"
    "training" -> "Training"
    "trainer" -> "Trainer und Übungsleiter"
    "vorstand" -> "Vorstand"
    "historie" -> "Historie"
    "satzung" -> "Satzung"
    "beitraege" -> "Beiträge"
    "gaststaette" -> "Gaststätte"
    "faq" -> "FAQ"
    "mannschaften" -> "Mannschaften"
    "links-und-verbaende" -> "Links und Verbände"
    "presse" -> "Presse"
    "sponsoren" -> "Sponsoren"
    else -> part.removeSuffix(".html").replace('-', ' ').replaceFirstChar { it.titlecase(Locale.GERMAN) }
}

fun copyStaticAssets() {
    val resourcesAssets = projectRoot.resolve("src/main/resources/assets")
    if (resourcesAssets.exists()) {
        Files.walk(resourcesAssets)
            .filter { Files.isRegularFile(it) }
            .forEach { source ->
                val target = assetsOutput.resolve(source.relativeTo(resourcesAssets).invariantSeparatorsPathString)
                target.parent.createDirectories()
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
            }
    }
}

fun rewriteEmbeddedAssetPaths(page: Page, html: String): String {
    val srcOrHrefPattern = Regex("""\b(src|href)=["'](/assets/[^"']+)["']""")
    val cssUrlPattern = Regex("""url\((['"]?)(/assets/[^)'"]+)\1\)""")

    val withRelativeAttributes = srcOrHrefPattern.replace(html) { match ->
        val attribute = match.groupValues[1]
        val assetPath = match.groupValues[2]
        val relativePath = linkTo(page, assetPath)
        """$attribute="$relativePath""""
    }

    return cssUrlPattern.replace(withRelativeAttributes) { match ->
        val quote = match.groupValues[1]
        val assetPath = match.groupValues[2]
        val relativePath = linkTo(page, assetPath)
        "url($quote$relativePath$quote)"
    }
}

fun liveReloadScript(): String {
    if (!liveReload) return ""
    return """
        <script>
          (function () {
            if (!window.EventSource) return;
            var source = new EventSource('/__livereload');
            var first = true;
            source.addEventListener('reload', function () {
              if (first) {
                first = false;
                return;
              }
              window.location.reload();
            });
            source.onerror = function () {
              source.close();
              setTimeout(function () { window.location.reload(); }, 1000);
            };
          })();
        </script>
    """.trimIndent()
}

fun navigationScript(): String {
    return """
        <script>
          (function () {
            var groups = Array.prototype.slice.call(document.querySelectorAll('.section-nav-group'));
            if (!groups.length) return;

            function closeAll() {
              groups.forEach(function (group) {
                group.removeAttribute('open');
              });
            }

            groups.forEach(function (group) {
              var summary = group.querySelector('summary');
              if (!summary) return;

              summary.addEventListener('click', function (event) {
                event.preventDefault();
                var willOpen = !group.hasAttribute('open');
                closeAll();
                if (willOpen) {
                  group.setAttribute('open', '');
                }
              });
            });

            document.addEventListener('click', function (event) {
              if (event.target.closest('.section-nav')) return;
              closeAll();
            });

            document.addEventListener('keydown', function (event) {
              if (event.key === 'Escape') {
                closeAll();
              }
            });

            document.addEventListener('click', function (event) {
              var link = event.target.closest('a');
              if (!link) return;
              closeAll();
            }, true);

            window.addEventListener('pagehide', closeAll);
          })();
        </script>
    """.trimIndent()
}

fun excerpt(markdown: String, maxLength: Int = 180): String {
    val plain = markdown
        .replace(Regex("<[^>]+>"), " ")
        .replace(Regex("""[#>*`\[\]\(\)\-_]"""), " ")
        .replace(Regex("""\s+"""), " ")
        .trim()
    return if (plain.length <= maxLength) plain else plain.take(maxLength).trimEnd() + " …"
}

fun escapeHtml(value: String): String = buildString(value.length) {
    value.forEach { char ->
        append(
            when (char) {
                '&' -> "&amp;"
                '<' -> "&lt;"
                '>' -> "&gt;"
                '"' -> "&quot;"
                '\'' -> "&#39;"
                else -> char
            },
        )
    }
}

fun jsonEscape(value: String): String = buildString(value.length) {
    value.forEach { char ->
        append(
            when (char) {
                '\\' -> "\\\\"
                '"' -> "\\\""
                '\b' -> "\\b"
                '\u000C' -> "\\f"
                '\n' -> "\\n"
                '\r' -> "\\r"
                '\t' -> "\\t"
                else -> if (char.code < 0x20) "\\u%04x".format(char.code) else char
            },
        )
    }
}
