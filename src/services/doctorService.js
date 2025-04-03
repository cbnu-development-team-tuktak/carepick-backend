// src/services/doctorService.js
import axios from 'axios';

/**
 * 가공된 의사 데이터를 페이지네이션으로 가져오는 함수
 * @param {number} page - 현재 페이지 번호 (0부터 시작)
 * @param {number} size - 페이지당 항목 수
 * @returns {Promise<{ content: DoctorDetailsResponse[], totalPages: number, totalElements: number }>}
 */
export const fetchDoctors = async (page = 0, size = 20) => {
  try {
    const response = await axios.get('/api/doctors', {
      params: { page, size }
    });
    return response.data;
  } catch (error) {
    console.error('의사 목록 조회 중 오류 발생:', error);
    throw error;
  }
};
