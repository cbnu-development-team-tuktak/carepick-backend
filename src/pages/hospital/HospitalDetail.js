// src/pages/HospitalDetail.js

// React 관련 import
import React, { useEffect, useState } from 'react'; // 상태 관리(useState), 생명주기 처리(useEffect) 훅
import { useParams, useNavigate } from 'react-router-dom'; // URL 파라미터 접근 및 페이지 이동 처리 훅

// DTO 변환 함수 import
import { fromHospitalApiResponse } from '../../dto/HospitalDetailsResponse'; // 서버 응답을 HospitalDetailsResponse 형식으로 변환하는 유틸

// 병원 진료과 조회 서비스 함수 import
import { fetchSpecialties } from '../../services/specialtyService'; // 서버에서 전체 진료과 목록을 가져오는 함수

// 병원 폼 컴포넌트 import
import HospitalForm from '../../components/hospital/HospitalForm'; // 병원 정보 입력 및 수정 UI를 담당하는 폼 컴포넌트

// 공통 컴포넌트 import
import PageHeader from '../../components/common/PageHeader'; // 페이지 상단 제목 및 설명 표시 컴포넌트
import FloatingButton from '../../components/common/button/FloatingButton'; // 우측 하단 고정 버튼 컴포넌트 (편집/저장)

// 부트스트랩 컴포넌트 import
import { Modal, Button } from 'react-bootstrap'; // 모달 다이얼로그 및 버튼 UI 구성 요소

function HospitalDetail() {
  const { id } = useParams(); // URL에서 병원 ID 추출 (예: /hospital/123 → 123)
  const navigate = useNavigate(); // 페이지 아동을 위한 훅

  // 병원 상세 정보 상태 (불러온 병원 정보를 저장)
  const [hospital, setHospital] = useState(null);

  // 로딩 여부 상태 (데이터 불러오는 중 여부)
  const [loading, setLoading] = useState(true);

  // 편집 모드 여부 상태 (폼 컴포넌트에서 읽기/쓰기 구분)
  const [isEditing, setIsEditing] = useState(false);

  // 저장 확인 모달 표시 여부
  const [showModal, setShowModal] = useState(false);

  // 업로드한 이미지 미리보기 URL
  const [imagePreview, setImagePreview] = useState(null);

  // 전체 진료과 목록 (드롭다운에 사용)
  const [specialties, setSpecialties] = useState([]);

  // 현재 병원에 선택된 진료과 목록
  const [selectedSpecialties, setSelectedSpecialties] = useState([]);

  // 컴포넌트가 처음 마운트되거나 ID가 변경될 때 병원 정보 및 진료과 정보 로드
  useEffect(() => {
    // 전체 진료과 목록 불러오기 (드롭다운용)
    fetchSpecialties().then(setSpecialties);

    // 병원 ID를 기반으로 상세 정보 요청
    fetch(`/api/hospitals/${id}`) // JSON 요청
      .then(res => res.json())
      .then(data => {
        const dto = fromHospitalApiResponse(data); // 응답 데이터를 DTO 형식으로 변환
        setHospital(dto); // 병원 상태 저장
        setSelectedSpecialties(dto.specialties); // 진료과 선택 상태 설정
        setLoading(false); // 로딩 완료
      })
      .catch(err => {
        console.error('병원 정보 불러오기 실패:', err); // 에러 로그 출력
        setLoading(false); // 에러 발생 시에도 로딩 중지
      });
  }, [id]); // 병원 ID가 바뀌면 재실행

  // 로딩 중일 때 표시할 화면
  if (loading) return <div className="text-center">로딩 중...</div>;

  // 병원 정보를 찾을 수 없을 때 표시할 화면
  if (!hospital) return <div className="text-center text-danger">병원 정보를 찾을 수 없습니다.</div>;

  // 저장 버튼 클릭 시 모달 창 열기
  const handleSave = () => setShowModal(true);

  // 모달 내 '저장' 버튼 클릭 시 실행되는 함수
  const confirmSave = () => {
    console.log('저장 완료'); // 실제 저장 로직은 아직 구현하지 않음
    setShowModal(false); // 모달 닫기
    navigate(-1); // 이전 페이지로 이동
  };

  // 모달 내 '취소' 버튼 클릭 시 모달 닫기
  const cancelSave = () => setShowModal(false);

  // 병원 입력 필드 변경 시 상태 업데이트
  // field 매개변수를 통해 어떤 필드를 수정할지 동적으로 처리
  const handleInputChange = (e, field) => {
    setHospital(prev => ({
      ...prev,
      [field]: e.target.value
    }));
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      const url = URL.createObjectURL(file);
      setImagePreview(url);
    }
  };

  return (
    <div className="container mt-4">
      {/* 상단 제목 및 설명 컴포넌트 */}
      <PageHeader 
        title="병원 상세 정보" // 페이지 제목
        description="병원 정보 및 진료과, 위치, 이미지 등을 수정할 수 있습니다." // 페이지 설명
      />

      {/* 병원 입력/수정 폼 */}
      <HospitalForm
        hospital={hospital} // 병원 상태 데이터
        isEditing={isEditing} // 편집 모드 여부 (현재는 false → 읽기 전용)
        handleInputChange={handleInputChange} // 입력 필드 값 변경 핸들러
        handleFileChange={handleFileChange} // 이미지 파일 업로드 핸들러
        imagePreview={imagePreview} // 이미지 미리보기 URL
        specialties={specialties} // 전체 진료과 목록
        selectedSpecialties={selectedSpecialties} // 선택된 진료과
        setSelectedSpecialties={setSelectedSpecialties} // 진료과 선택 상태 업데이트 함수
      />

      {/* 우측 하단 고정형 버튼 (편집 버튼으로 사용) */}
      <FloatingButton 
        mode="edit" // 연필 아이콘 표시
        onClick={handleSave} // 클릭 시 저장 확인 모달 표시
      />

      {/* 저장 확인용 모달 다이얼로그 */}
      <Modal show={showModal} onHide={cancelSave}>
        <Modal.Header closeButton>
          <Modal.Title>변경 사항 저장</Modal.Title> {/* 모달 제목 */}
        </Modal.Header>
        <Modal.Body>
          정말로 변경 사항을 저장하시겠습니까? {/* 안내 메시지 */}
        </Modal.Body>
        <Modal.Footer>
          {/* 취소 버튼: 모달 닫기 */}
          <Button variant="secondary" onClick={cancelSave}>취소</Button>
          {/* 저장 버튼: 저장 로직 실행 후 페이지 이동 */}
          <Button variant="primary" onClick={confirmSave}>저장</Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
}

export default HospitalDetail;