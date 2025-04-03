// src/dto/DiseaseDetailsResponse.js

/**
 * @typedef {Object} DiseaseDetailsResponse
 * @property {number} id - 질병 ID
 * @property {string} name - 질병명
 * @property {string} bodySystem - 신체 계통
 * @property {number[]} symptoms - 연결된 증상 ID 목록
 * @property {string[]} specialties - 진료과 이름 목록
 * @property {string[]} doctorIds - 연결된 의사 ID 목록
 */

/**
 * 백엔드에서 받은 Disease 객체를 DiseaseDetailsResponse로 변환
 * @param {any} apiResponse
 * @returns {DiseaseDetailsResponse}
 */
export function fromDiseaseApiResponse(apiResponse) {
  return {
    id: apiResponse.id,
    name: apiResponse.name,
    bodySystem: apiResponse.bodySystem,
    symptoms: Array.isArray(apiResponse.symptoms) ? apiResponse.symptoms : [], 
    specialties: Array.isArray(apiResponse.specialties) ? apiResponse.specialties : [],
    doctorIds: Array.isArray(apiResponse.doctorIds) ? apiResponse.doctorIds : [],
  };
}
