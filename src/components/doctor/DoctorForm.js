// React 라이브러리 임포트
import React from 'react'; // React 라이브러리 임포트

// 커스텀 입력 필드 컴포넌트들
import InputField from '../common/input/InputField'; // 텍스트 입력 필드 컴포넌트
import InputList from '../common/input/InputList'; // 리스트 항목 입력 필드 컴포넌트
import DoctorImage from './DoctorImage'; // 의사 프로필 이미지 컴포넌트

const DoctorForm = ({
  doctor, // 의사 정보 DTO
  isEditing, // 편집 가능 여부
  handleInputChange, // 입력값 변경 처리 함수
  handleListChange, // 리스트 항목 입력값 변경 처리 함수
  handleFileChange, // 이미지 파일 변경 처리 함수
  imagePreview, // 이미지 미리보기 URL
  specialties = [], // 진료과 목록
  selectedSpecialties = [], // 선택된 진료과 목록
  setSelectedSpecialties = () => {}, // 선택된 진료과 목록 업데이트 함수
}) => {
  // 체크박스 클릭 시 진료과 추가/제거
  const handleCheckboxChange = (specialtyName) => {
    // 이미 선택된 진료과인 경우 목록에서 제거, 그렇지 않으면 목록에 추가
    if (selectedSpecialties.includes(specialtyName)) {
      setSelectedSpecialties(selectedSpecialties.filter(name => name !== specialtyName));
    } else {
      setSelectedSpecialties([...selectedSpecialties, specialtyName]);
    }
  };

  return (
    <form>
      {/* 프로필 이미지 */}
      <DoctorImage
        imageUrl={imagePreview || doctor?.profileImage} // 의사 미리보기 URL
        isEditing={isEditing} // 편집 가능 여부
        onFileChange={handleFileChange} // 파일 변경 시 호출될 함수
      />

      {/* 이름 필드 */}
      <InputField
        label="이름" // 입력 필드 라벨
        value={doctor?.name || ''} // 의사 이름
        onChange={(e) => handleInputChange(e, 'name')} // 입력값 변경 시 호출될 함수
      />

      {/* 진료과 체크박스 */}
      <div className="mb-4">
        <label className="form-label" style={{ fontWeight: 'bold', fontSize: '1.2rem' }}>
          진료과
        </label>
        <div className="d-flex flex-wrap gap-2">
          {specialties.map((specialty, idx) => (
            <div key={idx} className="form-check">
              <input
                className="form-check-input"
                type="checkbox"
                id={`specialty-${specialty.id}`} // 진료과 고유 ID로 checkbox id 설정
                disabled={!isEditing} // 수정 가능 여부에 따라 비활성화 여부 결정
                checked={selectedSpecialties.includes(specialty.name)} // 선택된 진료과 표시
                onChange={() => handleCheckboxChange(specialty.name)} // 체크박스 클릭 시 선택/제거
              />
              <label className="form-check-label" htmlFor={`specialty-${specialty.id}`}>
                {specialty.name} {/* 진료과 이름 */}
              </label>
            </div>
          ))}
        </div>
      </div>

      {/* 소속 병원 */}
      <InputField
        label="소속 병원" // 입력 필드 라벨
        value={doctor?.hospitalName || '정보 없음'} // 병원 이름
        onChange={(e) => handleInputChange(e, 'hospitalName')} // 입력값 변경 시 호출될 함수
      />

      {/* 자격면허 목록 */}
      <InputList
        label="자격면허" // 리스트 필드 라벨 
        values={doctor?.educationLicenses || []} // 자격면허 목록
        onChange={(e, idx) => handleListChange(e, idx, 'educationLicenses')} // 리스트 항목 변경 시 호출될 함수
        placeholder="정보 없음" // 리스트 항목이 없을 경우 기본값
      />

      {/* 경력 목록 */}
      <InputList
        label="경력" // 리스트 필드 라벨 
        values={doctor?.careers || []} // 경력 목록
        onChange={(e, idx) => handleListChange(e, idx, 'careers')} // 리스트 항목 변경 시 호출될 함수
        placeholder="정보 없음" // 리스트 항목이 없을 경우 기본값
      />
    </form>
  );
};

export default DoctorForm;
