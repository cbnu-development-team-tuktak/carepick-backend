// src/components/common/input/InputGroup.jsx

// React 관련 import
import React from 'react'; // React 라이브러리 임포트

// 부트스트랩 관련 import
import { Form } from 'react-bootstrap'; // 부트스트랩 Form 컴포넌트 임포트 (폼 필드 및 레이아웃 구성에 사용)

const InputGroupField = ({
  label, // 전체 필드의 레이블
  leftLabel, // 첫 번째 필드의 이름 (예: 위도)
  leftValue, // 첫 번째 필드 값 
  rightLabel, // 두 번째 필드의 이름 (예: 경도)
  rightValue, // 두 번째 필드 값
  readOnly = true, // 읽기 전용 여부 
}) => {
  return (
    <Form.Group className="mb-4">
      <Form.Label style={{ fontWeight: 'bold', fontSize: '1.1rem' }}>{label}</Form.Label>
      <div className="input-group">
        {/* 첫 번째 필드 레이블 */}
        <span className="input-group-text">{leftLabel}</span>
        <input
          type="text"
          className="form-control"
          value={leftValue} // 첫 번째 필드 값
          readOnly={readOnly} // 읽기 전용 여부
        />
        {/* 두 번째 필드 레이블 */}
        <span className="input-group-text">{rightLabel}</span>
        <input
          type="text"
          className="form-control"
          value={rightValue} // 두 번째 필드 값
          readOnly={readOnly} // 읽기 전용 여부
        />
      </div>
    </Form.Group>
  );
};

export default InputGroupField;
