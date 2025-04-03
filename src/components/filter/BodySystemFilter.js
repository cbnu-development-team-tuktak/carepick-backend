// React 관련 import
import React from 'react';

// 신체계통 코드, 라벨, 이미지 정보 리스트 정의
const BODY_SYSTEMS = [
  { code: '뇌신경', label: '뇌신경', image: '/img/body-system/icon_01.png' },
  { code: '정신건강', label: '정신건강', image: '/img/body-system/icon_02.png' },
  { code: '눈', label: '눈', image: '/img/body-system/icon_03.png' },
  { code: '귀코목', label: '귀코목', image: '/img/body-system/icon_04.png' },
  { code: '구강', label: '구강', image: '/img/body-system/icon_05.png' },
  { code: '뼈근육', label: '뼈근육', image: '/img/body-system/icon_06.png' },
  { code: '피부', label: '피부', image: '/img/body-system/icon_07.png' },
  { code: '내분비', label: '내분비', image: '/img/body-system/icon_08.png' },
  { code: '호흡기', label: '호흡기', image: '/img/body-system/icon_09.png' },
  { code: '순환기', label: '순환기', image: '/img/body-system/icon_10.png' },
];

// 필터 컴포넌트
function BodySystemFilter({ selectedCode, onSelect }) {
  return (
    <div className="d-flex flex-wrap gap-3 mb-4 justify-content-center">
      {BODY_SYSTEMS.map(({ code, label, image }) => (
        <div
          key={code}
          className="d-flex flex-column align-items-center"
          style={{ cursor: 'pointer' }}
          onClick={() => onSelect(code)}
        >
          <img
            src={image}
            alt={label}
            style={{
              width: '48px',
              height: '48px',
              opacity: selectedCode === code ? 1 : 0.6,
              transition: 'opacity 0.3s',
            }}
          />
          <div className="mt-2" style={{ fontSize: '0.9rem' }}>
            {label}
          </div>
        </div>
      ))}
    </div>
  );
}

export default BodySystemFilter;
