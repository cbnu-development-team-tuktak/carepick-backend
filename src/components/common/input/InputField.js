// src/components/common/InputField.js

// React 관련 임포트
import React from 'react'; // React 라이브러리 

const InputField = ({ label, value, disabled, onChange }) => (
  <div className="mb-3">
    <label className="form-label" style={{ fontWeight: 'bold', fontSize: '1.2rem' }}>
      {label} {/* 입력 필드 레이블 */}
    </label>
    <input
      type="text" // 텍스트 입력 타입
      className="form-control" // 부트스트랩 스타일 적용
      value={value} // 입력값을 상태와 연결
      disabled={disabled} // disabled 속성으로 필드 비활성화
      onChange={onChange} // 입력값이 변경될 때 onChange 이벤트 처리
    />
  </div>
);

export default InputField;
