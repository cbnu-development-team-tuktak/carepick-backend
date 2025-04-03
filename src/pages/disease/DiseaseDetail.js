// src/pages/DiseaseDetail.jsx
import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { fromDiseaseApiResponse } from '../../dto/DiseaseDetailsResponse';
import { fetchSymptomById } from '../../services/symptomService'; // ⬅️ 증상 이름 조회 함수
import DiseaseForm from '../../components/disease/DiseaseForm';
import PageHeader from '../../components/common/PageHeader';
import FloatingButton from '../../components/common/button/FloatingButton';
import { Modal, Button } from 'react-bootstrap';

function DiseaseDetail() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [disease, setDisease] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [symptoms, setSymptoms] = useState([]); // 이름 리스트로 관리

  useEffect(() => {
    const loadData = async () => {
      try {
        const res = await fetch(`/api/diseases/processed/${id}`);
        const data = await res.json();
        const dto = fromDiseaseApiResponse(data);
        setDisease(dto);

        // ✅ 증상 ID → 이름 변환
        const symptomNames = await Promise.all(
          dto.symptoms.map(async (symptomId) => {
            const res = await fetch(`/api/symptoms/${symptomId}`);
            const symptom = await res.json();
            return symptom.name;
          })
        );

        setSymptoms(symptomNames);
      } catch (err) {
        console.error('질병 정보 불러오기 실패:', err);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [id]);

  const handleSave = () => setShowModal(true);
  const confirmSave = () => {
    console.log('저장 완료');
    setShowModal(false);
    navigate(-1);
  };
  const cancelSave = () => setShowModal(false);

  const handleSymptomChange = (e, index) => {
    const updated = [...symptoms];
    updated[index] = e.target.value;
    setSymptoms(updated);
  };

  const handleAddSymptom = (symptom) => {
    if (!symptoms.includes(symptom)) {
      setSymptoms([...symptoms, symptom]);
    }
  };

  const handleDeleteSymptom = (index) => {
    setSymptoms(symptoms.filter((_, i) => i !== index));
  };

  if (loading) return <div className="text-center">로딩 중...</div>;
  if (!disease) return <div className="text-center text-danger">질병 정보를 찾을 수 없습니다.</div>;

  return (
    <div className="container mt-4">
      <PageHeader
        title="질병 상세 정보"
        description="질병 정보 및 증상, 진료과 등을 수정할 수 있습니다."
      />

      <DiseaseForm
        disease={disease}
        isEditing={isEditing}
        symptoms={symptoms}
        handleSymptomChange={handleSymptomChange}
        handleAddSymptom={handleAddSymptom}
        handleDeleteSymptom={handleDeleteSymptom}
      />

      <FloatingButton mode="edit" onClick={() => setIsEditing(!isEditing)} />

      <Modal show={showModal} onHide={cancelSave}>
        <Modal.Header closeButton>
          <Modal.Title>변경 사항 저장</Modal.Title>
        </Modal.Header>
        <Modal.Body>정말로 변경 사항을 저장하시겠습니까?</Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={cancelSave}>취소</Button>
          <Button variant="primary" onClick={confirmSave}>저장</Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
}

export default DiseaseDetail;
