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

data class NavItem(
    val label: String,
    val url: String,
    val sectionKey: String,
)

data class Crumb(
    val label: String,
    val url: String,
)

val navItems = listOf(
    NavItem("Start", "/", "home"),
    NavItem("Verein", "/verein/", "verein"),
    NavItem("Standort", "/standort/", "standort"),
    NavItem("Bogenschießen", "/bogenschiessen/", "bogenschiessen"),
    NavItem("Fußball", "/fussball/", "fussball"),
    NavItem("Tischtennis", "/tischtennis/", "tischtennis"),
    NavItem("Kontakt", "/kontakt/", "kontakt"),
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
        "news-index" -> renderNewsIndex(page, allNewsPosts.filter { it.departmentKey == page.sectionKey })
        else -> renderStandardPage(page)
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
          ${renderTicker(page)}
          ${renderHeader(page)}
          <main class="site-main">
            $content
          </main>
          ${renderFooter(page)}
          ${liveReloadScript()}
        </body>
        </html>
    """.trimIndent()
}

fun renderHome(page: Page, pagesByUrl: Map<String, Page>, allNewsPosts: List<NewsPost>): String {
    val latestNews = allNewsPosts.take(4).joinToString("") { post ->
        """
            <article class="news-card">
              <div class="news-meta">${escapeHtml(post.departmentLabel)} · ${escapeHtml(post.dateLabel)}</div>
              <h3><a href="${linkTo(page, post.page.urlPath)}">${escapeHtml(post.page.title)}</a></h3>
              <p>${escapeHtml(post.page.summary)}</p>
            </article>
        """.trimIndent()
    }

    val departmentCards = listOf(
        Triple("/fussball/", "Fußball", "Kunstrasen, Jugendbereiche und Spielgemeinschaften am Logauweg."),
        Triple("/tischtennis/", "Tischtennis", "Training in Möhringen und der Aufstieg in die A-Klasse als aktueller Anker."),
        Triple("/bogenschiessen/", "Bogenschießen", "Die stärkste Inhaltstiefe mit Training, Kursen, Ergebnissen und Wissensbereich."),
    ).joinToString("") { (url, title, text) ->
        """
            <article class="feature-card">
              <span class="pill">${escapeHtml(title)}</span>
              <h3><a href="${linkTo(page, url)}">${escapeHtml(title)}</a></h3>
              <p>${escapeHtml(text)}</p>
            </article>
        """.trimIndent()
    }

    val venueSnippet = pagesByUrl["/standort/"]?.summary ?: "Vereinsgelände, Gaststätte, Parkplätze und Anfahrt mit der U6."

    return """
        <section class="hero shell">
          <div class="hero-copy">
            <span class="kicker">${escapeHtml(page.kicker)}</span>
            <h1>${escapeHtml(page.title)}<em> gemeinsam statt verstreut.</em></h1>
            <p class="lead">${escapeHtml(page.lead)}</p>
            <div class="hero-actions">
              <a class="button button-primary" href="${linkTo(page, "/bogenschiessen/schnupperkurse.html")}">Schnupperkurs ansehen</a>
              <a class="button button-secondary" href="${linkTo(page, "/standort/")}">Zum Standort</a>
            </div>
          </div>
          <aside class="hero-panel">
            <h2>Neuer Aufbau mit echter Informationsarchitektur</h2>
            <p>Die Seite folgt jetzt dem in der Spezifikation beschriebenen department-first-Modell. Das ist fachlich sauberer als eine künstlich club-weite Top-Level-Struktur für Inhalte, die heute fast vollständig aus dem Bogensport kommen.</p>
          </aside>
        </section>

        <section class="shell section">
          <div class="section-head">
            <span class="kicker">Abteilungen</span>
            <h2>Ein Vereinsdach, drei Einstiege</h2>
          </div>
          <div class="card-grid">$departmentCards</div>
        </section>

        <section class="shell section">
          <div class="section-head">
            <span class="kicker">Aktuelles</span>
            <h2>Neuigkeiten aus den Abteilungen</h2>
          </div>
          <div class="news-grid">$latestNews</div>
        </section>

        <section class="shell section split-section">
          <article class="feature-card">
            <span class="pill">Training</span>
            <h3><a href="${linkTo(page, "/bogenschiessen/training.html")}">Zeiten und Orte im Blick</a></h3>
            <p>Logauweg, Hengstäcker Schule und Anne-Frank-Gemeinschaftsschule sind im Bogensport klar getrennt dargestellt. Fußball und Tischtennis folgen derselben Denke, sobald die Inhalte weiter ausgebaut sind.</p>
          </article>
          <article class="feature-card">
            <span class="pill">Standort</span>
            <h3><a href="${linkTo(page, "/standort/")}">Logauweg 21 als gemeinsamer Treffpunkt</a></h3>
            <p>${escapeHtml(venueSnippet)}</p>
          </article>
        </section>

        <section class="shell prose-block">
          ${page.bodyHtml}
        </section>
    """.trimIndent()
}

fun renderNewsIndex(page: Page, posts: List<NewsPost>): String {
    val newsHtml = posts.joinToString("") { post ->
        """
            <article class="news-row">
              <div class="news-date">${escapeHtml(post.dateLabel)}</div>
              <div>
                <h3><a href="${linkTo(page, post.page.urlPath)}">${escapeHtml(post.page.title)}</a></h3>
                <p>${escapeHtml(post.page.summary)}</p>
              </div>
            </article>
        """.trimIndent()
    }

    return """
        ${renderHero(page)}
        <section class="shell prose-block">
          ${page.bodyHtml}
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

fun renderStandardPage(page: Page): String {
    return """
        ${renderHero(page)}
        <section class="shell prose-block">
          ${renderBreadcrumbs(page)}
          <article class="prose">
            ${page.bodyHtml}
          </article>
        </section>
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
          <aside class="hero-panel">
            <h2>${escapeHtml(page.description.ifBlank { page.title })}</h2>
            <p>${escapeHtml(page.summary)}</p>
          </aside>
        </section>
    """.trimIndent()
}

fun renderTicker(page: Page): String {
    val items = when (page.sectionKey) {
        "home" -> listOf(
            "60 Jahre Vereinsgeschichte",
            "Vereinsgelände am Logauweg 21",
            "Bogenschießen, Fußball und Tischtennis unter einem Dach",
            "Da Angelo täglich von 17.00 bis 22.30 Uhr",
        )
        "bogenschiessen" -> listOf(
            "65 Schützinnen und Schützen zwischen 8 und 80 Jahren",
            "Drei Trainingsorte in Stuttgart",
            "Schnupperkurse mit Leihmaterial",
            "Liga, Galerie und Wissensbereich aus einer Hand",
        )
        else -> listOf(
            page.title,
            "1. SV Fasanenhof 1965 e.V.",
            "Statische Website aus Kotlin-Skript",
            "Inhalte aus den Vereinsquellen migriert",
        )
    }
    val repeated = (items + items).joinToString("") { """<span><i class="dot"></i>${escapeHtml(it)}</span>""" }
    return """<div class="ticker"><div class="track">$repeated</div></div>"""
}

fun renderHeader(page: Page): String {
    val navHtml = navItems.joinToString("") { item ->
        val active = if (item.sectionKey == page.sectionKey) "active" else ""
        """<li><a class="$active" href="${linkTo(page, item.url)}">${escapeHtml(item.label)}</a></li>"""
    }
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
    """.trimIndent()
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
                <li><a href="${linkTo(page, "/verein/")}">Verein</a></li>
                <li><a href="${linkTo(page, "/standort/")}">Standort</a></li>
                <li><a href="${linkTo(page, "/kontakt/")}">Kontakt</a></li>
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
    if (page.urlPath == "/") return listOf(Crumb("Start", "/"))

    val crumbs = mutableListOf(Crumb("Start", "/"))
    var current = ""
    parts.forEachIndexed { index, part ->
        current += "/$part"
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
    return if (first == "index.md") "home" else first
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

fun pathLabel(part: String): String = when (part.removeSuffix(".html")) {
    "verein" -> "Verein"
    "standort" -> "Standort"
    "bogenschiessen" -> "Bogenschießen"
    "fussball" -> "Fußball"
    "tischtennis" -> "Tischtennis"
    "kontakt" -> "Kontakt"
    "impressum" -> "Impressum"
    "datenschutz" -> "Datenschutz"
    "meisterschaften" -> "Meisterschaften"
    "archiv" -> "Archiv"
    "galerie" -> "Galerie"
    "wissen" -> "Wissen"
    "aktuelles" -> "Aktuelles"
    "schnupperkurse" -> "Schnupperkurse"
    "ausruestung" -> "Ausrüstung"
    "schiessordnung" -> "Schießordnung"
    "training" -> "Training"
    "vorstand" -> "Vorstand"
    "historie" -> "Historie"
    "satzung" -> "Satzung"
    "beitraege" -> "Beiträge"
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
