package com.callrapport.controller.disease

// Repository 관련 import
import com.callrapport.repository.disease.CategoryRepository // Category 엔티티를 다루는 리포지토리

// Model 관련 import
import com.callrapport.model.disease.Category // 분류(Category) 엔티티

// Spring Web 관련 import
import org.springframework.web.bind.annotation.GetMapping // GET 요청 매핑 어노테이션
import org.springframework.web.bind.annotation.RequestMapping // 컨트롤러의 기본 URL 설정
import org.springframework.web.bind.annotation.RestController // REST API 컨트롤러 명시

@RestController
@RequestMapping("/api/categories")
class CategoryEntityController(
    private val categoryRepository: CategoryRepository // 분류 데이터를 처리할 리포지토리 의존성 주입
) {
    // 전체 분류 목록 조회
    // 예: http://localhost:8080/api/categories
    @GetMapping
    fun getAllCategories(): List<Category> {
        return categoryRepository.findAll() // 모든 분류 엔티티 조회
    }
}
