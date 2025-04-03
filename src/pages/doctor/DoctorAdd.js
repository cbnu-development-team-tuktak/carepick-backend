// src/pages/DoctorAdd.js
import React, { useState, useEffect } from 'react'; // Hook 사용
import { useNavigate } from 'react-router-dom'; // 페이지 이동을 위한 useNavigate 훅 사용
import { Modal, Button } from 'react-bootstrap'; // 모달과 버튼을 react-bootstrap에서 가져오기
import { FaRandom } from 'react-icons/fa'; // 셔플 버튼 아이콘

// 컴포넌트
import DoctorForm from '../../components/doctor/DoctorForm'; // 의사 정보 폼
import FloatingButton from '../../components/common/button/FloatingButton'; // 플로팅 버튼
import PageHeader from '../../components/common/PageHeader'; // 상단 페이지 헤더

// 서비스
import { fetchSpecialties } from '../../services/specialtyService'; // 진료과 목록 API

function DoctorAdd() {
  const navigate = useNavigate();

  // 상태 초기화
  const [doctor, setDoctor] = useState({
    name: '',
    specialties: [],
    hospitalName: '',
    educationLicenses: [],
    careers: [],
    profileImage: '',
  });

  const [imagePreview, setImagePreview] = useState(null);
  const [showModal, setShowModal] = useState(false);

  // 진료과 관련 상태
  const [specialties, setSpecialties] = useState([]); // 전체 진료과 목록
  const [selectedSpecialties, setSelectedSpecialties] = useState([]); // 선택된 진료과

  // 진료과 목록 불러오기
  useEffect(() => {
    fetchSpecialties()
      .then((data) => {
        setSpecialties(data);
      })
      .catch((err) => {
        console.error('진료과 정보 로딩 실패:', err);
      });
  }, []);

  // 저장 버튼 클릭 시 모달 띄우기
  const handleSave = () => {
    setShowModal(true);
  };

  // 모달 저장 확인 → 저장 로직 실행
  const confirmSave = () => {
    console.log('의사 정보가 저장되었습니다.');
    console.log('저장할 진료과:', selectedSpecialties);

    // 선택된 진료과 정보를 doctor 객체에 반영
    const finalDoctor = {
      ...doctor,
      specialties: selectedSpecialties,
    };

    // 여기서 백엔드 API 호출로 전송 가능
    console.log('최종 저장 데이터:', finalDoctor);

    setShowModal(false);
    navigate('/doctor'); // 저장 후 목록 페이지로 이동
  };

  // 모달 취소
  const cancelSave = () => {
    setShowModal(false);
  };

  // 일반 필드 변경 처리
  const handleInputChange = (e, field) => {
    setDoctor((prevDoctor) => ({
      ...prevDoctor,
      [field]: e.target.value,
    }));
  };

  // 리스트형 필드 (자격면허, 경력) 변경 처리
  const handleListChange = (e, idx, field) => {
    const updatedList = [...doctor[field]];
    updatedList[idx] = e.target.value;
    setDoctor((prevDoctor) => ({
      ...prevDoctor,
      [field]: updatedList,
    }));
  };

  // 파일 업로드 → 이미지 미리보기
  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      const imageUrl = URL.createObjectURL(file);
      setImagePreview(imageUrl);
    }
  };

  // 셔플 기능 (선택적)
  const handleShuffleClick = () => {
    console.log('셔플 버튼 클릭됨');
    // 랜덤 데이터 생성 로직을 여기 추가할 수 있음
  };

  return (
    <div className="container mt-4">
      <PageHeader
        title="의사 추가"
        description="새로운 의사 정보를 추가하는 페이지입니다."
        rightButton={
          <Button variant="outline-secondary" onClick={handleShuffleClick}>
            <FaRandom />
          </Button>
        }
      />

      {/* 의사 정보 입력 폼 */}
      <DoctorForm
        doctor={doctor}
        isEditing={true}
        handleInputChange={handleInputChange}
        handleListChange={handleListChange}
        handleFileChange={handleFileChange}
        imagePreview={imagePreview}
        specialties={specialties} // ← 진료과 목록 전달
        selectedSpecialties={selectedSpecialties} // ← 선택된 진료과들
        setSelectedSpecialties={setSelectedSpecialties} // ← 업데이트 함수
      />

      {/* 플로팅 저장 버튼 */}
      <FloatingButton mode="save" onClick={handleSave} />

      {/* 저장 확인 모달 */}
      <Modal show={showModal} onHide={cancelSave}>
        <Modal.Header closeButton>
          <Modal.Title>변경 사항 저장</Modal.Title>
        </Modal.Header>
        <Modal.Body>정말로 변경 사항을 저장하시겠습니까?</Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={cancelSave}>
            취소
          </Button>
          <Button variant="primary" onClick={confirmSave}>
            저장
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
}

export default DoctorAdd;
