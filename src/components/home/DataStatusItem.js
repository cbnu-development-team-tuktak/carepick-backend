import React from 'react'; // React 라이브러리 (React 컴포넌트를 생성하기 위한 기본 라이브러리)
import { 
  FaCheckCircle, // 체크 마크 아이콘
  FaTimesCircle // 엑스 마크 아이콘
} from 'react-icons/fa'; // react-icons 라이브러리에서 FontAwesome 아이콘을 불러오기
import { Spinner } from 'react-bootstrap'; // 부트스트랩의 Spinner 컴포넌트 (로딩 인디케이터)

function DataStatusItem({ 
  label, // 항목 이름 (병원, 의사, 질병 등)
  loading, // 로딩 중 상태 (true일 때 로딩 중, false일 때 로딩 완료)
  error, // 에러 메시지 (로딩 실패 시 에러 메시지 표시)
  count, // 전체 데이터 개수 (예: 병원, 의사, 질병 등의 총 개수)
  loaded // 로드된 데이터 개수 (실제 로드된 데이터의 수)
}) {
  return (
    <li className="list-group-item d-flex justify-content-between align-items-center">
      <span><strong>{label}</strong></span>
      {loading ? (
        <span className="text-primary d-inline-flex align-items-center gap-2">
          <Spinner animation="border" size="sm" />
          <span>불러오는 중...</span>
        </span>
      ) : error ? (
        <span className="text-danger d-inline-flex align-items-center gap-2">
          <FaTimesCircle size={16} className="mr-2" />
          <span>{error}</span>
        </span>
      ) : (
        count > 0 && loaded === count ? (
          <span className="text-success d-inline-flex align-items-center gap-2">
            <FaCheckCircle size={16} className="mr-2" />
            <span>{`${loaded} 개 로드 완료`}</span>
          </span>
        ) : (
          <span className="text-danger d-inline-flex align-items-center gap-2">
            <FaTimesCircle size={16} className="mr-2" />
            <span>로드 실패</span>
          </span>
        )
      )}
    </li>
  );
}

export default DataStatusItem;
