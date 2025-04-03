// src/services/specialtyService.js
import axios from 'axios';

/**
 * 전체 진료과 목록을 가져오는 함수
 * @returns {Promise<{ id: string, name: string }[]>} - 진료과 목록
 */
export const fetchSpecialties = async () => {
  try {
    const response = await axios.get('/api/specialties');
    return response.data; // 진료과 목록 반환
  } catch (error) {
    console.error('진료과 목록 조회 중 오류 발생:', error);
    throw error;
  }
};
