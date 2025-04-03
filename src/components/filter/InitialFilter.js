// src/components/common/InitialFilter.js

// React 관련 import
import React from 'react'; // React 라이브러리에서 기본 컴포넌트 사용

// 초성 리스트를 버튼으로 렌더링하여, 필터 적용 기능을 제공
function InitialFilter({ 
  initials, // 출력할 초성 리스트
  selected, // 현재 선택된 초성
  onSelect // 초성 버튼 클릭 시 실행한 콜백
}) {
  return (
    <div className="mb-3 d-flex flex-wrap gap-2">
      {/* 각 초성에 대해 버튼 생성 */}
      {initials.map((char) => (
        <button
          key={char} // 각 초성을 식별하기 위한 고유 key
          className={`btn btn-sm ${selected === char ? 'btn-dark text-white' : 'btn-light'}`}
          onClick={() => onSelect(char)} // 클릭 시 해당 초성을 선택 콜백으로 전달
        >
          {char} {/* 버튼에 표시될 초성 문자 */}
        </button>
      ))}
    </div>
  );
}

export default InitialFilter;
