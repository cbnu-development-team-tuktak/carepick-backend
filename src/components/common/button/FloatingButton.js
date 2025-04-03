// src/components/common/button/FloatingButton.js

// React 관련 임포트
import React from 'react'; // React 라이브러리 

// 아이콘 관련 import
import { 
  FaPen, // FaPen (편집 아이콘)
  FaSave, // FaSave (저장 아이콘)
  FaPlus // FaPlus (추가 아이콘)
} from 'react-icons/fa'; 

// 고정 버튼
const FloatingButton = ({ mode, onClick }) => {
  let icon;

  // mode에 따라 다른 아이콘을 렌더링
  switch (mode) {
    case 'edit': // 수정 모드
      icon = <FaPen size={24} />;
      break;
    case 'save': // 저장 모드
      icon = <FaSave size={24} />;
      break;
    case 'add': // 추가 모드
      icon = <FaPlus size={24} />;
      break;
    default: // 기본 모드
      icon = <FaPlus size={24} />;
  }

  return (
    <button
      className="floating-btn btn btn-primary"
      onClick={onClick}
    >
      {icon}
    </button>
  );
};

export default FloatingButton;
