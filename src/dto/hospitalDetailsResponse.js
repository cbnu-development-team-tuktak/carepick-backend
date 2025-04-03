/**
 * 병원 상세 정보 DTO (HospitalDetailsResponse)
 *
 * @typedef {Object} HospitalDetailsResponse
 * @property {string} id - 병원 ID
 * @property {string} name - 병원 이름
 * @property {string|null} phoneNumber - 병원 전화번호 (nullable)
 * @property {string|null} homepage - 병원 홈페이지 주소 (nullable)
 * @property {string} address - 병원 주소
 * @property {string|null} operatingHours - 병원 운영시간 (nullable)
 * @property {string} url - 병원 상세 페이지 URL
 * @property {string[]} specialties - 병원 진료과 목록
 * @property {string[]} doctors - 소속 의사 ID 목록
 * @property {ImageResponse[]} images - 병원 이미지 리스트
 * @property {HospitalAdditionalInfo|null} additionalInfo - 병원 부가 정보 (nullable)
 * @property {LatLng|null} location - 병원 좌표 정보
 */

/**
 * @typedef {Object} ImageResponse
 * @property {number} id - 이미지 ID
 * @property {string} url - 이미지 URL
 * @property {string} alt - 이미지 대체 텍스트
 */

/**
 * @typedef {Object} LatLng
 * @property {number} latitude - 위도
 * @property {number} longitude - 경도
 */

/**
 * Hospital API 응답을 DTO로 변환
 * @param {any} apiResponse
 * @returns {HospitalDetailsResponse}
 */
export function fromHospitalApiResponse(apiResponse) {
  return {
    id: apiResponse.id,
    name: apiResponse.name,
    phoneNumber: apiResponse.phoneNumber || null,
    homepage: apiResponse.homepage || null,
    address: apiResponse.address || '',
    operatingHours: apiResponse.operatingHours || null,
    url: apiResponse.url || '',
    specialties: Array.isArray(apiResponse.specialties) ? apiResponse.specialties : [],
    doctors: Array.isArray(apiResponse.doctors) ? apiResponse.doctors : [],
    images: Array.isArray(apiResponse.images) ? apiResponse.images.map(fromImageResponse) : [],
    additionalInfo: apiResponse.additionalInfo || null,
    location: apiResponse.location ? {
      latitude: apiResponse.location.latitude,
      longitude: apiResponse.location.longitude
    } : null
  };
}

/**
 * 병원 이미지 응답을 DTO로 변환
 * @param {any} image
 * @returns {ImageResponse}
 */
function fromImageResponse(image) {
  return {
    id: image.id || 0,
    url: image.url,
    alt: image.alt
  };
}
