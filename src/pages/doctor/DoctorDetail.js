// React 관련 import
import React, { useEffect, useState } from 'react'; // 상태 관리 및 생명주기 처리용 React 훅
import { useParams, useNavigate } from 'react-router-dom'; // 라우터에서 URL 파라미터 추출 및 페이지 이동 처리 

// DTO 변환 함수 import
import { fromDoctorApiResponse } from '../../dto/DoctorDetailsResponse'; // 백엔드 응답 데이터를 Doctor DTO 형태로 변환하는 함수

// UI 컴포넌트 import
import { Modal, Button } from 'react-bootstrap'; // 부트스트랩 기반 모달 및 버튼 컴포넌트
import FloatingButton from '../../components/common/button/FloatingButton'; // 화면 우측 하단에 위치한 플로팅 버튼
import DoctorForm from '../../components/doctor/DoctorForm'; // 의사 상세 정보를 입력/표시하는 재사용 가능한 폼 컴포넌트
import PageHeader from '../../components/common/PageHeader'; // 페이지 상단의 제목 및 설명 영역

// 서비스 import
import { fetchSpecialties } from '../../services/specialtyService'; // 진료과 목록을 백엔드로붜 가져오는 서비스 함수

function DoctorDetail() {
  // URL 파라미터에서 의사 ID 추출
  const { id } = useParams();

  // 의사 상세 정보 상태 (백엔드로부터 불러온 데이터를 저장)
  const [doctor, setDoctor] = useState(null);

  // 데이터 로딩 여부 상태
  const [loading, setLoading] = useState(true);

  // 저장 확인 모달 표시 여부 상태
  const [showModal, setShowModal] = useState(false); 

  // 현재 페이지가 편집 모드인지 여부 (false면 읽기 전용)
  const [isEditing, setIsEditing] = useState(false); 

  // 업로드한 이미지 미리보기 URL 상태
  const [imagePreview, setImagePreview] = useState(null); 

  // 전체 진료과 목록 상태 (select 옵션에 사용)
  const [specialties, setSpecialties] = useState([]); 

  // 현재 의사에게 연결된 진료과 목록 상태
  const [selectedSpecialties, setSelectedSpecialties] = useState([]); 

  // 페이지 이동을 위한 훅 (예: 이전 페이지로 이동 등)
  const navigate = useNavigate(); 

  // 컴포넌트 마운트 또는 의사 ID가 변경될 때 실행되는 효과
  useEffect(() => {
    // 진료과 목록을 백엔드에서 불러오기
    fetchSpecialties()
      .then((data) => {
        setSpecialties(data); // 전체 진료과 목록 상태에 저장
      })
      .catch((err) => {
        // 에러 발생 시 로그 출력
        console.error('진료과 목록 불러오기 실패:', err);
      });

    // 특정 의사 상세 정보 조회
    fetch(`/api/doctors/${id}`)
      .then((res) => res.json()) // 응답을 JSON 형태로 파싱
      .then((data) => {
        const doctorData = fromDoctorApiResponse(data); // DTO 형식으로 변환
        console.log('받아온 의사 정보:', doctorData); // 받은 데이터 콘솔 출력
        setDoctor(doctorData); // 의사 상태에 저장
        setLoading(false); // 로딩 완료
        setSelectedSpecialties(doctorData.specialties); // 의사에게 연결된 진료과 설정
      })
      .catch((err) => {
        console.error('의사 정보 불러오기 실패:', err); // 에러 로그 출력
        setLoading(false); // 로딩 상태 종료
      });
  }, [id]); // 의사 ID가 변경될 때마다 다시 실행됨

  // 로딩 중일 때 표시되는 메시지
  if (loading) return <div className="text-center">로딩 중...</div>;

  // 의사 정보를 찾지 못했을 경우 표시
  if (!doctor) return <div className="text-center text-danger">의사 정보를 찾을 수 없습니다.</div>;

  // 저장 버튼 클릭 시 모달 창을 표시
  const handleSave = () => {
    setShowModal(true); 
  };

  // 저장 확인 시 처리되는 동작
  const confirmSave = () => {
    console.log('저장 완료'); // 추후 서버 저장 로직 대체 예쩡
    setShowModal(false); // 모달 닫기
    navigate(-1); // 이전 페이지로 이동
  };

  // 저장 취소 시 모달 닫기
  const cancelSave = () => {
    setShowModal(false); 
  };

  // 단일 필드 입력값 변경 처리 (예: 이름, 전화번호 등)
  const handleInputChange = (e, field) => {
    setDoctor((prevDoctor) => ({
      ...prevDoctor,
      [field]: e.target.value, // 해당 필드 값만 업데이트
    }));
  };

  // 리스트 형태 필드 입력값 변경 처리 (예: 자격면허, 경력 등)
  const handleListChange = (e, idx, field) => {
    const updatedList = [...doctor[field]]; // 기존 배열 복사
    updatedList[idx] = e.target.value; // 해당 인덱스 값 변경
    setDoctor((prevDoctor) => ({
      ...prevDoctor,
      [field]: updatedList, // 필드 전체를 교체
    }));
  };

  // 파일 업로드(프로필 이미지 등) 처리
  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      // 파일 미리보기 URL 생성
      const imageUrl = URL.createObjectURL(file);
      setImagePreview(imageUrl); // 미리보기 상태로 설정
    }
  };

  return (
    <div className="container mt-4">
      {/* 페이지 상단 제목과 설명 영역 */}
      <PageHeader title="의사 상세 정보" description="의사 정보 및 진료과, 소속 병원 등을 수정할 수 있습니다." />
      {/* 의사 상세 정보 입력/표시 폼 */}
      <DoctorForm
        doctor={doctor} // 의사 객체 (전체 데이터)
        isEditing={isEditing} // 현재 편집 가능 여부
        handleInputChange={handleInputChange} // 단일 필드 값 변경 핸들러
        handleListChange={handleListChange} // 리스트 형태 필드 변경 핸들러
        handleFileChange={handleFileChange} // 프로필 이미지 업로드 핸들러
        imagePreview={imagePreview} // 이미지 미리보기 URL
        specialties={specialties} // 전체 진료과 목록
        selectedSpecialties={selectedSpecialties} // 선택된 진료과 목록
        setSelectedSpecialties={setSelectedSpecialties} // 진료과 선택 변경 핸들러
      />

      {/* 하단 플로팅 버튼 (편집 모드 진입 또는 저장 트리거) */}
      <FloatingButton 
        mode="edit" // '편집' 모드 아이콘으로 표시
        onClick={handleSave} // 클릭 시 저장 모달 열림
      />

      {/* 저장 확인 모달 */}
      <Modal show={showModal} onHide={cancelSave}>
        <Modal.Header closeButton>
          <Modal.Title>변경 사항 저장</Modal.Title> {/* 모달 제목 */}
        </Modal.Header>
        <Modal.Body>정말로 변경 사항을 저장하시겠습니까?</Modal.Body> {/* 확인 메시지 */}
        <Modal.Footer>
          <Button variant="secondary" onClick={cancelSave}>취소</Button> {/* 취소 버튼 */}
          <Button variant="primary" onClick={confirmSave}>저장</Button> {/* 저장 확정 버튼 */}
        </Modal.Footer>
      </Modal>
    </div>
  );
}

export default DoctorDetail;
