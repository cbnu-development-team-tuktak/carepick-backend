// src/dto/doctorDetailsResponse.js

/**
 * 의사 상세 정보 DTO (DoctorDetailsResponse)
 *
 * @typedef {Object} DoctorDetailsResponse
 * @property {string} id - 의사 ID
 * @property {string} name - 의사 이름
 * @property {string | null} profileImage - 프로필 이미지 URL (nullable)
 * @property {string[]} educationLicenses - 자격면허 이름 목록
 * @property {string[]} specialties - 진료과 이름 목록
 * @property {string[]} careers - 경력 이름 목록
 * @property {string | null} hospitalId - 병원 ID (nullable)
 * @property {string | null} hospitalName - 병원 이름 (nullable)
 */

/**
 * API 응답을 DoctorDetailsResponse 형태로 변환
 *
 * @param {any} apiResponse - 백엔드에서 받은 의사 정보 객체
 * @returns {DoctorDetailsResponse}
 */
export function fromDoctorApiResponse(apiResponse) {
  return {
    id: apiResponse.id,
    name: apiResponse.name,
    profileImage: apiResponse.profileImage || null,
    educationLicenses: Array.isArray(apiResponse.educationLicenses) ? apiResponse.educationLicenses : [],
    specialties: Array.isArray(apiResponse.specialties) ? apiResponse.specialties : [],
    careers: Array.isArray(apiResponse.careers) ? apiResponse.careers : [],
    hospitalId: apiResponse.hospitalId || null,
    hospitalName: apiResponse.hospitalName || null,
  };
}
