import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Spinner } from 'react-bootstrap';
import { FaArrowLeft } from 'react-icons/fa';

import { fromSymptomApiResponse } from '../../dto/SymptomDetailsResponse';
import { fromDiseaseApiResponse } from '../../dto/DiseaseDetailsResponse';
import PageHeader from '../../components/common/PageHeader';

function SymptomDetail() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [symptom, setSymptom] = useState(null);
  const [diseases, setDiseases] = useState([]);
  const [loading, setLoading] = useState(true);
  const [symptomMap, setSymptomMap] = useState({});
  const [symptomMapReady, setSymptomMapReady] = useState(false);

  useEffect(() => {
    let isCancelled = false;

    const fetchSymptomAndDiseases = async () => {
      try {
        setLoading(true); // ✅ 페이지 전환 시 로딩 표시
        const res = await fetch(`/api/symptoms/${id}`);
        const data = await res.json();
        if (isCancelled) return;

        const dto = fromSymptomApiResponse(data);
        setSymptom(dto);

        const diseaseData = await Promise.all(
          dto.diseaseIds.map(async (diseaseId) => {
            try {
              const res = await fetch(`/api/diseases/processed/${diseaseId}`);
              const json = await res.json();
              return fromDiseaseApiResponse(json);
            } catch {
              return null;
            }
          })
        );
        if (isCancelled) return;

        const validDiseases = diseaseData.filter((d) => d !== null);
        setDiseases(validDiseases);

        const uniqueSymptomIds = new Set(validDiseases.flatMap((d) => d.symptoms));
        const symptomResponses = await Promise.all(
          Array.from(uniqueSymptomIds).map(async (symptomId) => {
            try {
              const res = await fetch(`/api/symptoms/${symptomId}`);
              const json = await res.json();
              return [symptomId, json.name];
            } catch {
              return [symptomId, null];
            }
          })
        );

        if (!isCancelled) {
          setSymptomMap(Object.fromEntries(symptomResponses));
          setSymptomMapReady(true);
          setLoading(false);
        }
      } catch (err) {
        if (!isCancelled) {
          console.error('증상 정보 조회 실패:', err);
          setLoading(false);
        }
      }
    };

    fetchSymptomAndDiseases();

    return () => {
      isCancelled = true;
    };
  }, [id]);

  if (loading) {
    return (
      <div className="text-center mt-5">
        <Spinner animation="border" role="status" />
        <div className="mt-2">로딩 중입니다...</div>
      </div>
    );
  }

  if (!symptom) {
    return (
      <div className="text-center mt-4 text-danger">
        증상 정보를 찾을 수 없습니다.
      </div>
    );
  }

  return (
    <div className="container mt-4">
      <PageHeader
        title="증상 상세 정보"
        description="해당 증상과 연결된 질병 정보를 확인할 수 있습니다."
        rightButton={
          <button
            className="btn btn-outline-secondary d-flex align-items-center"
            onClick={() => navigate('/symptom')}
          >
            <FaArrowLeft className="me-2" />
            뒤로 가기
          </button>
        }
      />

      <div className="card shadow-sm p-4">
        <p className="fw-bold fs-4 mb-4">{symptom.name}</p>

        <h6 className="fw-bold">관련 질병</h6>
        <ul className="list-group mt-2">
          {diseases.map((disease) => {
            const hasAllNames = disease.symptoms.every((symptomId) => symptomMap[symptomId]);

            return (
              <li
                key={disease.id}
                className="list-group-item d-flex justify-content-between align-items-start" // ✅ mb-3 제거
              >
                <div className="ms-2 me-auto w-100">
                  <div className="fw-bold mb-2">{disease.name}</div>

                  {hasAllNames ? (
                    <div className="d-flex flex-wrap gap-2">
                      {disease.symptoms.map((symptomId) => {
                        const symptomName = symptomMap[symptomId];
                        const isCurrent = symptomId === Number(id);

                        return (
                          <span
                            key={symptomId}
                            className={`px-2 py-1 rounded ${
                              isCurrent ? 'bg-primary text-white fw-bold' : 'bg-light text-muted'
                            }`}
                            style={{
                              flex: '0 0 calc(20% - 0.5rem)',
                              minWidth: 'fit-content',
                              textAlign: 'center',
                              cursor: isCurrent ? 'default' : 'pointer',
                            }}
                            onClick={() => {
                              if (!isCurrent) {
                                navigate(`/symptom/${symptomId}`);
                              }
                            }}
                          >
                            {symptomName}
                          </span>
                        );
                      })}
                    </div>
                  ) : (
                    <div className="d-flex align-items-center py-2">
                      <Spinner animation="border" size="sm" className="me-2" />
                      <span className="text-muted">증상 정보를 불러오는 중...</span>
                    </div>
                  )}
                </div>

                <span className="badge text-bg-primary rounded-pill ms-3">
                  {disease.symptoms.length}
                </span>
              </li>
            );
          })}
        </ul>
      </div>
    </div>
  );
}

export default SymptomDetail;
