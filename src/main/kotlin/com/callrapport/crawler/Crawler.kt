package com.callrapport.crawler

import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class Crawler {
    fun getUrlsFromListPage(listUrl: String, linkSelector: String, baseUrl: String = ""): List<String> {
        val urls = mutableListOf<String>()
        try {
            val doc = Jsoup.connect(listUrl).get()
            val links = doc.select(linkSelector)

            for (link in links) {
                val detailUrl = baseUrl + link.attr("href")
                urls.add(detailUrl)
            }
        } catch (e: Exception) {
            println("⚠️ ${listUrl} 목록 크롤링 실패 : ${e.message}")
        }  
        return urls
    }
}