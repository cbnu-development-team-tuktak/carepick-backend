package com.callrapport.utils.html

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component

@Component
class HtmlAnalyzer {
    fun fetchHtml(url: String): String {
        return try {
            val doc: Document = Jsoup.connect(url).get()
            println("🌐 ${url} HTML get success")
            doc.html() // HTML 문서 반환
        } catch (e: Exception) {
            println("⚠️ ${url} HTML get failed: ${e.message}")
            "⚠️ HTML get failed: ${e.message}"
        }
    }

    fun fetchHtmlWithTextContent(url: String, textContent: String): String {
        return try {
            val document: Document = Jsoup.connect(url).get()

            // 불필요한 HTML 태그 제거
            document.select("nav, footer, aside, header, script, style").remove()

            val elementsContainingText: List<Element> = document.allElements.filter { element ->
                val cleanText = element.ownText().trim()
                cleanText.contains(textContent, ignoreCase = true)
            }

            if (elementsContainingText.isEmpty()) {
                println("⚠️ No elements found containing: $textContent")
                "⚠️ No elements found containing: $textContent"
            } else {
                println("✅ Found ${elementsContainingText.size} elements containing '$textContent'")
                elementsContainingText.joinToString("\n") { element ->
                    element.outerHtml()
                }
            }
        } catch (exception: Exception) {
            println("⚠️ ${url} HTML text search failed: ${exception.message}")
            "⚠️ HTML text search failed: ${exception.message}"
        }
    }
}