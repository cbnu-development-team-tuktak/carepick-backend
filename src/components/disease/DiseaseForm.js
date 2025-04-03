import React, { useState, useEffect } from 'react'; // React 라이브러리 및 useEffect, useState 훅
import { useSelector } from 'react-redux'; // Redux에서 상태 가져오기
import { FaEdit, FaTrashAlt, FaSave } from 'react-icons/fa'; // 아이콘들

// 컴포넌트 import
import InputField from '../common/input/InputField'; // 공통 입력 필드 컴포넌트

const DiseaseForm = ({
  disease,
  isEditing,
  symptoms = [],
  handleSymptomChange,
  handleAddSymptom,
  handleDeleteSymptom,
}) => {
  const [newSymptom, setNewSymptom] = useState('');
  const [filteredSymptoms, setFilteredSymptoms] = useState([]);
  const [loadingSymptoms, setLoadingSymptoms] = useState(false);

  // Redux에서 모든 증상 목록을 가져옵니다.
  const allSymptoms = useSelector((state) => state.symptoms.symptoms);

  useEffect(() => {
    // 증상 목록을 비동기적으로 가져오는 부분
    setFilteredSymptoms(allSymptoms);  // 기본적으로 전역 상태의 모든 증상을 표시
  }, [allSymptoms]);

  // 증상 검색 처리
  const handleSearchSymptom = (e) => {
    const keyword = e.target.value;
    setNewSymptom(keyword);

    // 검색어가 비어있을 경우 필터링을 초기화합니다.
    if (keyword.trim() === '') {
      setFilteredSymptoms(allSymptoms);
      return;
    }

    // 정규 표현식을 이용해 증상 필터링 (대소문자 구분 없이 검색)
    const regex = new RegExp(keyword, 'i');
    const filtered = allSymptoms.filter((s) => regex.test(s.name)); // 이름에 검색어가 포함된 증상만 필터링

    setFilteredSymptoms(filtered.slice(0, 5)); // 최대 5개까지 표시
  };

  const handleAddNewSymptom = (name) => {
    if (name && !symptoms.includes(name)) {
      handleAddSymptom(name);
      setNewSymptom('');
      setFilteredSymptoms([]); // 필터링된 증상 초기화
    }
  };

  return (
    <form>
      <InputField label="질병명" value={disease?.name || ''} disabled />
      <InputField label="진료과" value={disease?.specialties.join(', ') || ''} disabled />
      <h5>증상 관리</h5>

      <div className="d-flex mb-2">
        <input
          type="text"
          className="form-control me-2"
          placeholder="증상 검색"
          value={newSymptom}
          onChange={handleSearchSymptom} // 검색 시 증상 필터링
        />
      </div>

      {loadingSymptoms && (
        <div className="d-flex align-items-center mb-3">
          <div className="spinner-grow text-primary me-2" role="status" style={{ width: '1rem', height: '1rem' }}>
            <span className="visually-hidden">Loading...</span>
          </div>
          <span className="text-muted">증상 데이터를 불러오는 중입니다...</span>
        </div>
      )}

      <div className="mb-4">
        {!loadingSymptoms && newSymptom.trim() !== '' && filteredSymptoms.length > 0 && (
          <ul className="list-group">
            {filteredSymptoms.map((symptom) => (
              <li key={symptom.id} className="list-group-item d-flex justify-content-between align-items-center">
                {symptom.name}
                <button
                  type="button"
                  className="btn btn-sm btn-outline-primary"
                  onClick={() => handleAddNewSymptom(symptom.name)}
                >
                  추가
                </button>
              </li>
            ))}
          </ul>
        )}

        {!loadingSymptoms && newSymptom.trim() !== '' && filteredSymptoms.length === 0 && (
          <p>일치하는 증상이 없습니다.</p>
        )}
      </div>

      <div className="mb-4">
        {symptoms.map((symptom, index) => (
          <div key={index} className="d-flex align-items-center mb-2">
            {isEditing ? (
              <>
                <input
                  type="text"
                  className="form-control me-2"
                  value={symptom}
                  onChange={(e) => handleSymptomChange(e, index)}
                />
                <button className="btn btn-sm btn-success me-2" type="button">
                  <FaSave />
                </button>
                <button
                  className="btn btn-sm btn-secondary"
                  type="button"
                  onClick={() => handleDeleteSymptom(index)}
                >
                  <FaTrashAlt />
                </button>
              </>
            ) : (
              <>
                <span className="me-2">{symptom}</span>
                <button
                  className="btn btn-sm btn-outline-primary me-2"
                  type="button"
                  onClick={() => handleSymptomChange({ target: { value: symptom } }, index)}
                >
                  <FaEdit />
                </button>
                <button
                  className="btn btn-sm btn-outline-danger"
                  type="button"
                  onClick={() => handleDeleteSymptom(index)}
                >
                  <FaTrashAlt />
                </button>
              </>
            )}
          </div>
        ))}
      </div>
    </form>
  );
};

export default DiseaseForm;
