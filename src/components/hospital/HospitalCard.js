// src/components/hospital/HospitalCard.js
import React from 'react'; // React 라이브러리 임포트
import { useNavigate } from 'react-router-dom'; // 페이지 이동을 위한 useNavigate 훅 임포트

function HospitalCard({ 
  hospital // 병원 정보 객체
}) { 
  const navigate = useNavigate(); // 페이지 이동을 위한 navigate 훅

  const {
    id, // 병원 ID
    name, // 병원명
    address, // 병원 주소
    specialties, // 병원 진료과 목록
    images, // 병원 이미지 목록
  } = hospital; 

  // 병원 썸네일 이미지
  const thumbnailUrl = images?.[0]?.url || '/img/hospital/default-thumbnail.jpg';

  return (
    <div
      className="card hospital-card shadow-sm"
      onClick={() => navigate(`/hospital/${id}`)} // 카드 클릭 시 병원 상세 페이지로 이동
      style={{ cursor: 'pointer', borderRadius: '12px' }}
    >
      <img
        src={thumbnailUrl} // 썸네일 이미지 URL
        alt="병원 썸네일"
        className="card-img-top hospital-image"
      />

      <div className="card-body hospital-body">
        <h6 className="fw-bold mb-1 truncate-text">{name}</h6> {/* 병원 이름 */}
        <p className="text-muted small mb-1 truncate-text">
          {specialties?.length > 0 ? specialties.join(' | ') : '진료과 정보 없음'} {/* 진료과 */}
        </p>
        <p className="text-muted small mb-0 truncate-text">
          {address || '주소 정보 없음'} {/* 병원 주소 */}
        </p>
      </div>
    </div>
  );
}

export default HospitalCard;
