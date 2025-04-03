/**
 * 증상 상세 정보 DTO (SymptomDetailsResponse)
 *
 * @typedef {Object} SymptomDetailsResponse
 * @property {number} id - 증상 ID
 * @property {string} name - 증상 이름
 * @property {number[]} diseaseIds - 연결된 질병 ID 목록
 */

/**
 * Symptom API 응답을 DTO로 변환
 * @param {any} apiResponse
 * @returns {SymptomDetailsResponse}
 */
export function fromSymptomApiResponse(apiResponse) {
    return {
      id: apiResponse.id,
      name: apiResponse.name,
      diseaseIds: Array.isArray(apiResponse.diseaseIds) ? apiResponse.diseaseIds : []
    };
  }
  