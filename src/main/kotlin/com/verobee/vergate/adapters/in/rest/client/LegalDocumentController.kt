package com.verobee.vergate.adapters.`in`.rest.client

import com.verobee.vergate.adapters.`in`.rest.dto.client.LegalLinkInfo
import com.verobee.vergate.common.response.ApiResponse
import com.verobee.vergate.domain.model.LegalContentType
import com.verobee.vergate.domain.model.LegalDocType
import com.verobee.vergate.domain.model.LegalDocument
import com.verobee.vergate.domain.service.LegalDocumentService
import io.swagger.v3.oas.annotations.tags.Tag
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Client - Legal Documents")
@RestController
@RequestMapping("/api/v1/legal")
class LegalDocumentController(
    private val legalDocService: LegalDocumentService,
) {
    private val markdownParser = Parser.builder().build()
    private val markdownRenderer = HtmlRenderer.builder().build()

    /** JSON — list of legal document links for this app */
    @GetMapping("/{appKey}")
    fun list(@PathVariable appKey: String): ApiResponse<List<LegalLinkInfo>> {
        val docs = legalDocService.findByAppKeyActive(appKey)
        return ApiResponse.ok(docs.map { doc ->
            LegalLinkInfo(
                type = doc.docType.name,
                title = doc.title,
                url = "/api/v1/legal/$appKey/${doc.docType.toPath()}",
            )
        })
    }

    /** HTML — rendered privacy policy page */
    @GetMapping("/{appKey}/privacy-policy", produces = [MediaType.TEXT_HTML_VALUE])
    fun privacyPolicy(@PathVariable appKey: String): String {
        val doc = legalDocService.findByAppKeyAndType(appKey, LegalDocType.PRIVACY_POLICY)
        return renderPage(doc)
    }

    /** HTML — rendered terms of service page */
    @GetMapping("/{appKey}/terms", produces = [MediaType.TEXT_HTML_VALUE])
    fun terms(@PathVariable appKey: String): String {
        val doc = legalDocService.findByAppKeyAndType(appKey, LegalDocType.TERMS_OF_SERVICE)
        return renderPage(doc)
    }

    private fun renderPage(doc: LegalDocument): String {
        val bodyHtml = when (doc.contentType) {
            LegalContentType.MARKDOWN -> markdownRenderer.render(markdownParser.parse(doc.content))
            LegalContentType.HTML -> doc.content
        }
        return buildHtmlPage(doc.title, bodyHtml)
    }

    private fun buildHtmlPage(title: String, content: String): String = """
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>$title</title>
  <style>
    *, *::before, *::after { box-sizing: border-box; }
    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
                   'Helvetica Neue', Arial, sans-serif;
      margin: 0; padding: 0;
      background: #f8f9fa; color: #212529;
      -webkit-text-size-adjust: 100%;
    }
    .container {
      max-width: 800px; margin: 0 auto;
      padding: 32px 20px 64px; background: #fff;
      min-height: 100vh;
    }
    h1 {
      font-size: 1.4rem; font-weight: 700; color: #111;
      border-bottom: 2px solid #e9ecef;
      padding-bottom: 16px; margin: 0 0 28px;
    }
    h2 { font-size: 1.05rem; font-weight: 600; color: #333; margin-top: 32px; }
    h3 { font-size: 0.95rem; font-weight: 600; color: #555; margin-top: 20px; }
    p, li { font-size: 0.9rem; line-height: 1.8; color: #444; }
    ul, ol { padding-left: 22px; margin: 8px 0; }
    li + li { margin-top: 4px; }
    a { color: #0d6efd; text-decoration: none; }
    a:hover { text-decoration: underline; }
    table {
      width: 100%; border-collapse: collapse;
      margin: 16px 0; font-size: 0.875rem;
    }
    th, td {
      border: 1px solid #dee2e6; padding: 8px 12px; text-align: left;
    }
    th { background: #f8f9fa; font-weight: 600; color: #333; }
    code {
      font-family: 'SFMono-Regular', Consolas, monospace;
      font-size: 0.85em; background: #f1f3f5;
      padding: 2px 5px; border-radius: 3px;
    }
    pre { background: #f1f3f5; padding: 16px; border-radius: 6px; overflow-x: auto; }
    pre code { background: none; padding: 0; }
    blockquote {
      margin: 16px 0; padding: 12px 16px;
      border-left: 4px solid #dee2e6; color: #6c757d;
    }
    hr { border: none; border-top: 1px solid #e9ecef; margin: 28px 0; }
    strong { font-weight: 600; }
  </style>
</head>
<body>
  <div class="container">
    $content
  </div>
</body>
</html>
    """.trimIndent()
}
