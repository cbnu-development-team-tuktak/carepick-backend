// src/components/hospital/HospitalForm.jsx
// React 관련 import
import React from 'react'; // React 라이브러리 임포트

// 커스텀 컴포넌트 관련 import
import InputField from '../common/input/InputField'; // 기본 입력 필드 컴포넌트
import InputList from '../common/input/InputList'; // 리스트 입력 필드 컴포넌트
import OperatingHoursField from './OperatingHoursField'; // 운영시간 필드
import InputGroupField from '../common/input/InputGroup'; // 좌우 입력 필드 컴포넌트

const HospitalForm = ({
  hospital, // 병원 정보 객체 (HospitalDetailsResponse DTO)
  isEditing, // 편집 가능 여부
  handleInputChange, // 입력값 변경 처리 함수 (예: 병원명, 전화번호)
  handleFileChange, // 이미지 파일 변경 처리 함수
  imagePreview, // 이미지 미리보기 URL 
  specialties = [], // 진료과 목록
  selectedSpecialties = [], // 선택된 진료과 목록
  setSelectedSpecialties = () => {}, // 선택된 진료과 목록 업데이트 함수 
}) => {
  // 썸네일 이미지 URL
  const thumbnailUrl = imagePreview || hospital?.images?.[0]?.url || '/img/hospital/default-thumbnail.jpg';

  // 진료과 체크박스 클릭 시 선택/해제 처리
  const handleCheckboxChange = (specialtyName) => {
    if (selectedSpecialties.includes(specialtyName)) {
      setSelectedSpecialties(selectedSpecialties.filter(name => name !== specialtyName));
    } else {
      setSelectedSpecialties([...selectedSpecialties, specialtyName]);
    }
  };

  return (
    <form>
      {/* 병원 썸네일 이미지 */}
      <div className="mb-4 text-center">
        <img
          src={thumbnailUrl} // 썸네일 이미지 URL
          alt="병원 썸네일"
          className="hospital-thumbnail" 
        />
        {isEditing && ( // 편집 가능 여부 확인 후 이미지 업로드 입력 필드 표시
          <div className="mt-2">
            <input type="file" accept="image/*" onChange={handleFileChange} />
          </div>
        )}
      </div>

      {/* 기본 정보 입력 필드*/}
      <InputField label="병원명" value={hospital?.name || ''} onChange={(e) => handleInputChange(e, 'name')} />
      <InputField label="전화번호" value={hospital?.phoneNumber || ''} onChange={(e) => handleInputChange(e, 'phoneNumber')} />
      <InputField label="홈페이지" value={hospital?.homepage || ''} onChange={(e) => handleInputChange(e, 'homepage')} />
      <InputField label="주소" value={hospital?.address || ''} onChange={(e) => handleInputChange(e, 'address')} />

      {/* 운영 시간 */}
      <OperatingHoursField label="운영 시간" value={hospital?.operatingHours || ''} onChange={(e) => handleInputChange(e, 'operatingHours')} />

      <InputField label="외부 URL" value={hospital?.url || ''} onChange={(e) => handleInputChange(e, 'url')} />

      {/* 진료과 체크박스 */}
      <div className="mb-4">
        <label className="form-label fw-bold" style={{ fontSize: '1.2rem' }}>진료과</label>
        <div className="d-flex flex-wrap gap-2">
          {specialties.map((specialty, idx) => (
            <div key={idx} className="form-check">
              <input
                className="form-check-input"
                type="checkbox"
                id={`specialty-${specialty.id}`}
                disabled={!isEditing}
                checked={selectedSpecialties.includes(specialty.name)}
                onChange={() => handleCheckboxChange(specialty.name)}
              />
              <label className="form-check-label" htmlFor={`specialty-${specialty.id}`}>
                {specialty.name}
              </label>
            </div>
          ))}
        </div>
      </div>

      {/* 소속 의사 ID 입력 리스트 */}
      <InputList
        label="소속 의사 ID"
        values={hospital?.doctors || []}
        onChange={(e, idx) => handleInputChange(e, `doctors[${idx}]`)}
        placeholder="예: U000012345"
      />

      {/* 위치 정보 */}
      <InputGroupField
        label="위치 (Latitude / Longitude)"
        leftLabel="위도"
        leftValue={hospital?.location?.latitude || ''}
        rightLabel="경도"
        rightValue={hospital?.location?.longitude || ''}
      />

      {/* 추가 정보 */}
      <InputField
        label="추가 정보 (JSON)"
        value={hospital?.additionalInfo ? JSON.stringify(hospital.additionalInfo, null, 2) : ''}
        onChange={(e) => handleInputChange(e, 'additionalInfo')}
      />
    </form>
  );
};

export default HospitalForm;