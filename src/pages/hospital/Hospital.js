// src/pages/HospitalPage.jsx

// React 관련 import
import React, { useState, useEffect } from 'react'; // 컴포넌트 상태 관리와 생명주기 처리용 훅
import { useNavigate } from 'react-router-dom'; // 페이지 이동 처리를 위한 훅

// 공통 컴포넌트 import
import PageHeader from '../../components/common/PageHeader'; // 상단 제목 및 설명 표시용 공통 컴포넌트
import FloatingButton from '../../components/common/button/FloatingButton'; // 오른쪽 하단 고정 버튼 (추가/수정 등)
import HospitalCard from '../../components/hospital/HospitalCard'; // 병원 정보를 카드 형태로 보여주는 컴포넌트
import Pagination from '../../components/common/Pagination'; // 병원 목록을 페이지 단위로 나눠 보여주는 컴포넌트

// 병원 API 서비스 함수 import
import { fetchHospitals } from '../../services/hospitalService'; // 서버에서 병원 목록을 가져오는 함수

// 병원 API 응답을 DTO 형식으로 변환하는함수
import { fromHospitalApiResponse } from '../../dto/HospitalDetailsResponse'; // API 응답을 HospitalDetailsResponse 형태로 변환

function Hospital() {
  // 병원 목록 상태 (HospitalCard에 전달할 데이터)
  const [hospitals, setHospitals] = useState([]);

  // 현재 페이지 상태 (0부터 시작)
  const [page, setPage] = useState(0); 

  // 전체 페이지 수
  const [totalPages, setTotalPages] = useState(0); 

  // 페이지 이동을 위한 네비게이터 훅
  const navigate = useNavigate();

  // 컴포넌트 마운트 시 또는 page가 변경될 때 병원 목록을 서버에서 다시 불러옴
  useEffect(() => {
    // 현재 페이지와 페이지 크기(20개)를 서버에 전달
    fetchHospitals(page, 20) 
      .then((data) => {
        console.log('📦 받아온 병원 데이터:', data);
        
        // 서버에서 받아온 병원 데이터를 DTO 형식으로 변환
        const processed = data?.content?.map(fromHospitalApiResponse) || [];

        setHospitals(processed); // 병원 목록 상태에 저장
        setTotalPages(data.totalPages); // 전체 페이지 수 상태 업데이트
      })
      .catch((err) => {
        console.error('병원 목록 불러오기 실패:', err); // 예외 처리
      });
  }, [page]); // 페이지 번호가 바뀔 때마다 다시 실행됨

  // 병원 추가 버튼 클릭 시 실행
  const handleAddHospital = () => {
    navigate('/hospital/add'); 
  };

  return (
    <div className="container mt-4">
      {/* 상단 페이지 제목 및 설명 출력 */}
      <PageHeader
        title="병원 관리" // 페이지 제목
        description="병원 정보를 조회하고 수정할 수 있는 페이지입니다." // 페이지 설명
      />

      {/* 병원 카드 리스트 영역 */}
      <div className="row">
        {hospitals.map((hospital) => (
          <div key={hospital.id} className="col-md-4 mb-4">
            {/* 개별 병원 정보를 카드 형태로 렌더링 */}
            <HospitalCard hospital={hospital} />
          </div>
        ))}
      </div>

      {/* 페이지네이션 컴포넌트 */}
      <Pagination
        page={page} // 전체 페이지 번호
        totalPages={totalPages} // 전체 페이지 수
        onPageChange={(newPage) => setPage(newPage)} // 페이지 변경 핸들러
      />

      {/* 병원 추가 버튼 (화면 우측 하단에 고정) */}
      <FloatingButton 
        mode="add" // 아이콘: + 모양
        onClick={handleAddHospital} // 클릭 시 병원 등록 페이지로 이동
      />
    </div>
  );
}

export default Hospital;
