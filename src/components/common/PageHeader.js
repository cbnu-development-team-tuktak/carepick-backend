// src/components/common/PageHeader.js

// React 관련 import
import React from 'react'; // React 라이브러리 임포트 

function PageHeader({ 
  title, // 페이지 제목
  description, // 페이지 설명 문구
  rightButton // 우측에 표시할 버튼 컴포넌트
}) {
  return (
    <div className="mb-4 d-flex justify-content-between align-items-center"> {/* 페이지 헤더 컨테이너 (상단 여백, 양쪽 정렬) */}
      <div> 
        <h2 className="fw-bold fs-2">{title}</h2> {/* 페이지 제목 */}
        <p className="text-muted">{description}</p> {/* 페이지 설명 */}
      </div>
      <div>
        {rightButton} {/* 버튼이 있으면 표시 */}
      </div>
    </div>
  );
}

export default PageHeader;
