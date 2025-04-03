// src/components/doctor/DoctorImage.js

// React 라이브러리 임포트
import React from 'react'; // React 라이브러리 임포트

const DoctorImage = ({ 
  imageUrl, // 의사 이미지 URL (이미지가 없으면 기본 이미지 사용)
  isEditing, // 편집 가능 여부 (true이면 파일 업로드 가능)
  onFileChange // 파일 변경 처리 함수 (파일 업로드 시 사용)
}) => {
  const imageSrc = imageUrl || '/img/doctor/default-profile.jpeg'; // 기본 이미지를 여기서 처리

  const handleImageClick = () => {
    // 이미지 클릭 시 새 창에서 이미지를 열기
    const newWindow = window.open('', '_blank');
    newWindow.document.body.innerHTML = `<img src="${imageSrc}" style="max-width: 100%; height: auto; margin: 0; display: block;"/>`;
  };

  return (
    <div className="mb-4">
      <label className="form-label" style={{ fontWeight: 'bold', fontSize: '1.2rem' }}>
        의사 이미지
      </label>
      <div className="text-left">
        {/* 이미지 클릭 시 새 창에서 확대된 이미지를 볼 수 있도록 설정 */}
        <img
          src={imageSrc}
          alt="Doctor"
          className="doctor-image img-fluid rounded mb-3"
          onClick={handleImageClick} // 이미지 클릭 시 새 창 열기
        />
        {isEditing && (
          <input
            type="file"
            className="form-control"
            onChange={onFileChange} // 파일 변경 시 처리
          />
        )}
      </div>
    </div>
  );
};

export default DoctorImage;
