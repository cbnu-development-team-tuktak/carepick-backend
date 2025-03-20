package com.callrapport.component.crawler.hospital

import com.callrapport.component.crawler.WebCrawler
import com.callrapport.model.common.Image
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.stereotype.Component
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.net.URLDecoder

@Component
class HospitalImageCrawler(
    private val webCrawler: WebCrawler
) {
    fun extractOriginalImageUrl(thumbnailUrl: String): String {
        return try {
            val regex = Regex("src=([^&]+)") // `src=` 이후의 URL만 추출
            val match = regex.find(thumbnailUrl)
            if (match != null) {
                val encodedUrl = match.groupValues[1]
                URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString()) // URL 디코딩
            } else {
                thumbnailUrl // 원본 URL을 찾지 못하면 기존 URL 반환
            }
        } catch (e: Exception) {
            println("⚠️ Failed to extract original URL: ${e.message}")
            thumbnailUrl // 에러 발생 시 기존 URL 반환
        }
    }

    fun crawlHospitalImages(hospitalName: String): List<Image> {
        val driver = webCrawler.createWebDriver()
        return try {
            val encodedHospitalName = URLEncoder.encode(hospitalName, StandardCharsets.UTF_8.toString())
            val searchUrl = "https://search.naver.com/search.naver?where=image&mode=column&section=place&query=$encodedHospitalName"
    
            println("🔍 Opening Naver Image Search: $searchUrl")
            driver.get(searchUrl)
    
            val wait = WebDriverWait(driver, Duration.ofSeconds(20))
    
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.tile_item._fe_image_tab_content_tile[data-type='image']")))
    
            val imageDivs: List<WebElement> = driver.findElements(By.cssSelector("div.tile_item._fe_image_tab_content_tile[data-type='image']"))
    
            val imageList = imageDivs.mapNotNull { div ->
                try {
                    val imgElement = div.findElement(By.cssSelector("img._fe_image_tab_content_thumbnail_image"))
                    val thumbnailUrl = imgElement.getAttribute("src")
    
                    if (!thumbnailUrl.isNullOrBlank() && thumbnailUrl.startsWith("https://search.pstatic.net/common")) {
                        val originalUrl = extractOriginalImageUrl(thumbnailUrl) // 원본 URL 변환
                        Image(url = originalUrl, alt = "${hospitalName}_이미지")
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }.take(5)
    
            println("✅ Extracted ${imageList.size} high-resolution image URLs: $imageList")
            return imageList
        } catch (e: Exception) {
            println("⚠️ Failed to extract images: ${e.message}")
            emptyList()
        } finally {
            driver.quit()
        }
    }
}
