// src/components/common/LengthFilter.js

// React 관련 import
import React from 'react'; // React 기본 컴포넌트 사용

/**
 * 글자 수 필터링용 드롭다운 컴포넌트
 *
 * @param {string} selectedLength 현재 선택된 옵션 (ex: '전체', '1글자', ...)
 * @param {function} onSelectLength 선택 시 호출되는 콜백 함수
 */

function LengthFilter({ selectedLength, onSelectLength }) {
  return (
    <div className="mb-3">
      <select
        className="form-select form-select-sm"
        aria-label="글자 수 필터"
        value={selectedLength}
        onChange={(e) => onSelectLength(e.target.value)}
      >
        <option value="전체">전체</option>
        <option value="1글자">1글자</option>
        <option value="2글자">2글자</option>
        <option value="3글자 이상">3글자 이상</option>
      </select>
    </div>
  );
}

export default LengthFilter;
