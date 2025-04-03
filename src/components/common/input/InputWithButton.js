// src/components/common/input/InputWithButton.js

// React 관련 import
import React from 'react'; // React 라이브러리 임포트

function InputWithButton({ 
  placeholder, // 입력 필드의 placeholder 텍스트
  buttonText = '수정', // 버튼 텍스트 (기본값: '수정')
  onButtonClick // 버튼 클릭 시 호출되는 함수
}) {
  return (
    <div className="input-group mb-3"> {/* 입력 필드와 버튼을 묶은 그룹 */}
      <input
        type="text" // 텍스트 입력 타입
        className="form-control" // 부트스트랩 입력 필드 스타일
        placeholder={placeholder} // placeholder 텍스트 설정
        aria-label={placeholder} // 접근성용, 입력 필드의 레이블 설정
        aria-describedby="button-addon2" // 버튼과 입력 필드를 연결 (접근성)
      />
      <button
        className="btn btn-outline-secondary" // 부트스트랩 버튼 스타일 (외곽선)
        type="button" // 버튼 타입
        id="button-addon2" // 버튼 고유 ID
        onClick={onButtonClick} // 버튼 클릭 시 onButtonClick 함수 실행
      >
        {buttonText} {/* 버튼 텍스트 */}
      </button>
    </div>
  );
}

export default InputWithButton;
