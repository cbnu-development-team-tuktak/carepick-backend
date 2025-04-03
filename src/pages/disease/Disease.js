// src/pages/Disease.js

// React 및 Hook 관련 import
import React from 'react'; // React 컴포넌트 기본 기능 사용

// React Router 관련 import
import { useNavigate } from 'react-router-dom'; // 페이지 내 이동을 위한 useNavigate 훅

// Redux 관련 import
import { useDispatch, useSelector } from 'react-redux'; // Redux 상태를 가져오기 위한 훅

// 컴포넌트 관련 import
import PageHeader from '../../components/common/PageHeader'; // 페이지 헤더 컴포넌트
import Pagination from '../../components/common/Pagination'; // 페이지네이션 컴포넌트
import BodySystemFilter from '../../components/filter/BodySystemFilter'; // 신체계통 필터 컴포넌트
import { Spinner } from 'react-bootstrap'; // 로딩 스피너 컴포넌트

function Disease() {
  const navigate = useNavigate(); // 페이지 이동을 위한 훅

  // Redux store에서 질병 관련 데이터 가져오기
  const diseases = useSelector((state) => state.diseases.diseases); // 전역 상태에서 질병 목록
  const loading = useSelector((state) => state.diseases.loading); // 로딩 상태

  const [selectedBodySystem, setSelectedBodySystem] = React.useState(null); // 신체계통 필터 상태
  const [page, setPage] = React.useState(0); // 현재 페이지 상태

  // 필터링된 질병 리스트 (페이지 변경 시 필터와 페이지 번호에 맞는 데이터를 가져오도록)
  const filteredDiseases = selectedBodySystem
    ? diseases.filter((d) => d.bodySystem === selectedBodySystem)
    : diseases;

  // 페이지네이션을 위한 질병 목록
  const pagedDiseases = filteredDiseases.slice(page * 20, (page + 1) * 20); // 페이지별 질병 데이터

  // 필터링된 질병 리스트를 기반으로 totalPages 계산
  const totalPages = Math.ceil(filteredDiseases.length / 20);

  return (
    <div className="container mt-4">
      <PageHeader
        title="질병 관리"
        description="등록된 질병 정보를 조회하고 자세히 확인할 수 있는 페이지입니다."
      />

      {/* 신체계통 필터 */}
      <BodySystemFilter
        selectedCode={selectedBodySystem}
        onSelect={(code) => {
          setSelectedBodySystem((prev) => (prev === code ? null : code)); // 다시 클릭 시 해제
          setPage(0); // 필터 변경 시 페이지 번호를 0으로 리셋
        }}
      />

      {loading ? (
        <div className="text-center my-5">
          <Spinner animation="border" role="status" />
          <div className="mt-2">질병 정보를 불러오는 중입니다...</div>
        </div>
      ) : (
        <>
          {/* 질병 카드 리스트 (3칸 레이아웃) */}
          <div className="d-flex flex-wrap gap-2">
            {pagedDiseases.map((disease) => (
              <div
                key={disease.id}
                className="card p-2 d-flex flex-column justify-content-between"
                style={{
                  flex: '0 0 calc(33.333% - 0.5rem)',
                  minWidth: '200px',
                }}
              >
                <div>
                  <p className="fw-bold mb-1">{disease.name}</p>
                  <p className="text-muted mb-2">{disease.bodySystem}</p>
                </div>
                <button
                  className="btn btn-sm btn-outline-primary align-self-end"
                  onClick={() => navigate(`/disease/${disease.id}`)}
                >
                  자세히 보기
                </button>
              </div>
            ))}
          </div>

          <Pagination
            page={page}
            totalPages={totalPages}
            onPageChange={(newPage) => setPage(newPage)} // 페이지 변경 시 상태 업데이트
          />
        </>
      )}
    </div>
  );
}

export default Disease;
