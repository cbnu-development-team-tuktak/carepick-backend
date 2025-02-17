package com.callrapport.utils.html

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

@Component
class HtmlAnalyzer {
    // 전체 HTML 반환
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

    // 택스트를 포함한 HTML 반환 
    fun fetchHtmlWithTextContent(url: String, textContent: String): String {
        return try {
            val document: Document = Jsoup.connect(url).get()
            
            // 텍스트를 포함한 요소를 필터링
            val elementsContainingText: List<Element> = document.allElements.filter { element ->
                val elementText = element.ownText().trim()
                elementText.contains(textContent, ignoreCase = true)
            }
            
            // 텍스트를 포함한 요소가 없다면, 경고 안내문을 반환
            if (elementsContainingText.isEmpty()) {
                println("⚠️ No elements found containing $textContent")
                "⚠️ No elements found containing $textContent"
            } 

            // 텍스트를 포함한 요소 개수 출력
            println("✅ Found ${elementsContainingText.size} elements containing '$textContent'")
            
            // 주요 정보를 파악하기 위해 JSON 형식으로 변환
            val tagData = elementsContainingText.map { element ->
                val tagName: String = element.tagName() // 태그명
                val elementText: String = element.ownText() // 텍스트 내용
                val className: String = element.className() // 클래스명
                val elementId: String = element.id() // 고유 ID
                val parentTag: String = element.parent()?.tagName() ?: "" // 부모 태그
                val outerHtmlContent: String = element.outerHtml() // 해당 태그의 전체 HTML (자식 요소 포함)

                mapOf(
                    "tag" to tagName, // 태그명
                    "text" to elementText, // 텍스트 내용
                    "class" to className, // 클래스명
                    "id" to elementId, // 태그 ID
                    "parent_tag" to parentTag, // 부모 태그명
                    "html" to outerHtmlContent // 전체 HTML 내용
                )
            }

            val objectMapper = jacksonObjectMapper()
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tagData)
        } catch (exception: Exception) {
            println("⚠️ ${url} HTML text search failed: ${exception.message}")
            "⚠️ HTML text search failed: ${exception.message}"
        }
    }
}