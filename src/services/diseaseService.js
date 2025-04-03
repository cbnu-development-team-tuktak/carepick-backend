// src/services/diseaseService.js
import axios from 'axios';

/**
 * 가공된 질병 데이터를 페이지네이션으로 가져오는 함수
 * @param {number} page - 현재 페이지 번호 (0부터 시작)
 * @param {number} size - 페이지당 항목 수
 * @returns {Promise<{ content: DiseaseDetailsResponse[], totalPages: number, totalElements: number }>}
 */
export const fetchProcessedDiseases = async (page = 0, size = 20) => {
  try {
    const response = await axios.get('/api/diseases/processed', {
      params: { page, size }
    });
    return response.data;
  } catch (error) {
    console.error('질병 목록 조회 중 오류 발생:', error);
    throw error;
  }
};
