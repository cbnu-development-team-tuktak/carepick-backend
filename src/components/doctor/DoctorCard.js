// src/components/doctor/DoctorCard.js

// React 및 라우팅 관련 라이브러리 임포트
import React from 'react'; // React 라이브러리 임포트
import { useNavigate } from 'react-router-dom'; // 페이지 이동을 위한 useNavigate 훅

function DoctorCard({ 
  id, // 의사 ID
  name, // 의사명
  profileImage, // 의사 프로필 이미지
  hospitalName, // 소속 병원 이름
  specialties // 진료과 목록
}) {
  const navigate = useNavigate(); // 네비게이터 훅

  const nameWithoutTag = name.replace('[전문의]', '').trim(); // '[전문의]' 태그 제거 

  return (
    <div
      className="card doctor-card" // 의사 카드 스타일
      onClick={() => navigate(`/doctor/${id}`)} // 의사 카드 클릭 시 상세 페이지로 이동
      style={{ cursor: 'pointer' }} // 마우스 커서가 손가락 모양으로 변경
    >
      <img
        src={profileImage || '/default-doctor.png'} // 프로필 이미지 설정
        alt="Doctor" 
        className="card-img-top doctor-card-image" // 의사 이미지 클래스
      />

      <div className="card-body doctor-body">
        {/* 이름과 진료과 한 줄에 배치 */}
        <div className="d-flex justify-content-between align-items-center mb-2">
          <h6 className="fw-bold mb-0">{nameWithoutTag}</h6> {/* 의사 이름 */}
          {/* 첫 번째 진료과 (없으면 '정보 없음' 표시) */}
          <span className="text-muted small">{specialties?.[0] || '정보 없음'}</span>
        </div>

        {/* 병원명 */}
        <p className="text-muted small mb-0">
          {hospitalName || '정보 없음'} {/* 없으면 '정보 없음' 표시 */}
        </p>
      </div>
    </div>
  );
}

export default DoctorCard;
