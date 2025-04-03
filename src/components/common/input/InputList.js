// src/components/common/input/InputList.js
// React 관련 import
import React from 'react'; // React 라이브러리 임포트

const InputList = ({ 
  label, // 입력 필드의 레이블
  values, // 입력 값들의 배열
  onChange, // 값 변경 시 호출되는 함수
  placeholder // 값이 없을 때 표시되는 기본 텍스트
}) => (
  <div className="mb-4"> {/* 입력 리스트 컨테이너 */}
    <label className="form-label" style={{ fontWeight: 'bold', fontSize: '1.2rem' }}>
      {label} {/* 레이블 */}
    </label>
    {values.length > 0 ? ( // 값이 있으면 입력 필드 리스트 렌더링
      values.map((item, idx) => (
        <input
          key={idx} // 고유한 키 값
          type="text" // 텍스트 입력 타입
          className="form-control mb-2" // 부트스트랩 스타일 (입력 필드)
          value={item} // 각 항목의 값 설정
          onChange={(e) => onChange(e, idx)} // 입력 값 변경 시 호출되는 함수
        />
      ))
    ) : (
      <input
        type="text" // 텍스트 입력 타입
        className="form-control" // 부트스트랩 스타일 (입력 필드)
        value={placeholder || '정보 없음'} // placeholder 또는 '정보 없음' 기본값 표시
      />
    )}
  </div>
);

export default InputList;
