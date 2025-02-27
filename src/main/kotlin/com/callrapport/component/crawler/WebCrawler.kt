package com.callrapport.component.crawler

// Selenium 관련 라이브러리 (동적 HTML 크롤링)
import org.openqa.selenium.By // 요소를 찾기 위한 클래스
import org.openqa.selenium.WebDriver // 웹 브라우저 조작을 위한 인터페이스 
import org.openqa.selenium.WebElement // HTML 요소를 나타내는 객체
import org.openqa.selenium.chrome.ChromeDriver // Chrome 브라우저를 제어하는 드라이버 
import org.openqa.selenium.chrome.ChromeOptions // Chrome 실행 옵션 설정
import org.openqa.selenium.support.ui.WebDriverWait // 웹 페이지 로딩을 기다리는 기능
import org.openqa.selenium.support.ui.ExpectedConditions // 특정 조건이 만족할 때까지 대기하는 기능

// Jsoup 관련 라이브러리 (정적 HTML 크롤링)
import org.jsoup.Jsoup // HTML 파싱 및 문서 가져오기 
import org.jsoup.nodes.Document // HTML 문서를 나타내는 객체
import org.jsoup.nodes.Element // HTML 요소를 나타내는 객체

// Spring 및 JSON 관련 라이브러리
import org.springframework.stereotype.Component 
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

// 기타 유틸리티 라이브러리
import java.time.Duration

@Component
class WebCrawler {
    // 웹 드라이버 생성
    fun createWebDriver(): WebDriver {
        System.setProperty("webdriver.chrome.driver", "drivers/chromedriver.exe") // Chrome 드라이버 경로 설정
        val options = ChromeOptions()
        options.addArguments("--headless") // GUI 없이 실행 (백그라운드 동작)
        options.addArguments("--disable-gpu") // GPU 사용 비활성화 (호환성 문제 방지)
        options.addArguments("--no-sandbox") // 샌드박스 모드 비활성화 (권한 문제 방지)
        options.addArguments("--disable-dev-shm-usage") // 메모리 사용 최적화
        return ChromeDriver(options)
    }
}