// src/pages/Doctor.js

// React 관련 import
import React, { useEffect, useState } from 'react'; // 컴포넌트 생명주기와 상태 관리를 위한 React 훅
import { useNavigate } from 'react-router-dom'; // 페이지 이동을 위한 useNavigate 훅

// 의사 서비스 함수 import
import { fetchDoctors } from '../../services/doctorService'; // 백엔드에서 가공된 의사 목록 데이터를 조회하는 함수

// DTO 변환 함수 import
import { fromDoctorApiResponse } from '../../dto/DoctorDetailsResponse'; // 백엔드 응답 데이터를 프론트에서 사용할 Doctor DTO 형태로 변환하는 함수

// 공통 컴포넌트 import
import PageHeader from '../../components/common/PageHeader'; // 페이지 제목 및 설명을 출력하는 상단 헤더 컴포넌트
import Pagination from '../../components/common/Pagination'; // 목록 하단에 위치한 페이지네이션 컴포넌트

// 의사 전용 컴포넌트 import
import DoctorCard from '../../components/doctor/DoctorCard'; // 의사 정보를 카드 형태로 보여주는 컴포넌트

// 버튼 컴포넌트 import
import FloatingButton from '../../components/common/button/FloatingButton'; // 화면 하단 우측에 고정된 플로팅 버튼 (추가/편집 가능용)

function Doctor() {
  // 의사 목록 상태 (DoctorCard로 렌더링될 데이터)
  const [doctors, setDoctors] = useState([]);

  // 현재 페이지 번호 상태 (0부터 시작)
  const [page, setPage] = useState(0);

  // 전체 페이지 수 상태 (Pagination을 위한 값)
  const [totalPages, setTotalPages] = useState(0);

  // 페이지 이동을 위한 훅
  const navigate = useNavigate();

  // 컴포넌트가 처음 마운팅되거나 page가 변경될때 의사 목록 가져오기
  useEffect(() => {
    fetchDoctors(page, 20) // 현재 페이지 번호와 페이지당 개수(20)를 서버에 요청
      .then((data) => {
        // 서버 응답 데이터를 프론트에서 사용하는 Doctor DTO로 변환
        const parsedDoctors = data.content.map(fromDoctorApiResponse); 

        // 의사 목록 상태에 저장
        setDoctors(parsedDoctors);

        // 전체 페이지 수 상태에 저장
        setTotalPages(data.totalPages);
      })
      .catch((err) => {
        // 에러 발생 시 콘솔 출력
        console.error('의사 데이터 로딩 실패', err);
      });
  }, [page]); // 페이지 번호가 변경될 때마다 재실행

  // 의사 추가 버튼 클릭 시 호출되는 함수
  const handleAddDoctor = () => {
    navigate('/doctor/add'); // 의사 추가 페이지로 이동
  };

  return (
    <div className="container mt-4">
      {/* 페이지 상단 제목 및 설명 */}
      <PageHeader
        title="의사 관리" // 페이지 제목
        description="의사 정보를 조회하고 수정할 수 있는 페이지입니다." // 페이지 설명
      />

      {/* 의사 카드 리스트 */}
      <div className="row">
        {doctors.map((doctor) => (
          <DoctorCard
            key={doctor.id} // 고유 key (React 최적화를 위한 변수값)
            id={doctor.id} // 의사 ID
            name={doctor.name} // 의사 이름 
            profileImage={doctor.profileImage} // 프로필 이미지 URL
            hospitalName={doctor.hospitalName} // 소속 병원 이름
            specialties={doctor.specialties} // 진료과 배열
          />
        ))}
      </div>

      {/* 페이지네이션 컴포넌트 */}
      <Pagination
        page={page} // 현재 페이지 번호
        totalPages={totalPages} // 전체 페이지 수
        onPageChange={(newPage) => setPage(newPage)} // 페이지 변경 시 실행할 함수
      />

      {/* 우측 하단 플로팅 버튼 (플러스 아이콘) */}
      <FloatingButton 
        mode="add" // 버튼 모드: 추가 아이콘
        onClick={handleAddDoctor} // 클릭 시 의사 추가 페이지로 이동
      />
    </div>
  );
}

export default Doctor;
