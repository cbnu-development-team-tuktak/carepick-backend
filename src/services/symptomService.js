// src/services/symptomService.js
import axios from 'axios';

/**
 * 증상 목록(페이지 단위)을 가져오는 함수
 * @param {number} page - 페이지 번호 (0부터 시작)
 * @param {number} size - 페이지당 항목 수
 * @returns {Promise<{ content: object[], totalElements: number, totalPages: number }>} - 증상 페이지 결과
 */
export const fetchSymptoms = async (page = 0, size = 10) => {
  try {
    const response = await axios.get('/api/symptoms', {
      params: { page, size }
    });
    return response.data; // Page<SymptomDetailsResponse> 형식의 응답 반환
  } catch (error) {
    console.error('증상 목록 조회 중 오류 발생:', error);
    throw error;
  }
};
