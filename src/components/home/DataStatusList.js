// React 관련 import
import React from 'react'; // React 라이브러리 (React 컴포넌트를 생성하기 위한 기본 라이브러리)

// Redux 관련 import
import { useSelector } from 'react-redux';  // Redux 상태를 가져오기 위한 useSelector 훅

// 컴포넌트 import
import DataStatusItem from './DataStatusItem'; // 각 데이터 항목의 로딩 상태 및 결과를 표시

function DataStatusList() {
  // 병원 관련 상태
  const hospitalLoading = useSelector((state) => state.hospitals.loading); // 병원 데이터 로딩 상태
  const hospitalCount = useSelector((state) => state.hospitals.count); // 병원 데이터 개수
  const hospitals = useSelector((state) => state.hospitals.hospitals); // 병원 데이터 목록
  const hospitalError = useSelector((state) => state.hospitals.error); // 병원 데이터 로딩 시 발생하는 에러

  // 의사 관련 상태
  const doctorLoading = useSelector((state) => state.doctors.loading); // 의사 데이터 로딩 상태
  const doctorCount = useSelector((state) => state.doctors.count); // 의사 데이터 개수
  const doctors = useSelector((state) => state.doctors.doctors); // 의사 데이터 목록
  const doctorError = useSelector((state) => state.doctors.error); // 의사 데이터 로딩 시 발생하는 에러

  // 질병 관련 상태
  const diseaseLoading = useSelector((state) => state.diseases.loading); // 질병 데이터 로딩 상태
  const diseaseCount = useSelector((state) => state.diseases.count); // 질병 데이터 개수
  const diseases = useSelector((state) => state.diseases.diseases); // 질병 데이터 목록
  const diseaseError = useSelector((state) => state.diseases.error); // 질병 데이터 로딩 시 발생하는 에러

  // 증상 관련 상태
  const symptomLoading = useSelector((state) => state.symptoms.loading); // 증상 데이터 로딩 상태
  const symptomCount = useSelector((state) => state.symptoms.count); // 증상 데이터 개수
  const symptoms = useSelector((state) => state.symptoms.symptoms); // 증상 데이터 목록
  const symptomError = useSelector((state) => state.symptoms.error); // 증상 데이터 로딩 시 발생하는 에러

  return (
    <div className="mt-4">
      <h5 className="fw-bold">데이터 로드 상태</h5>
      <ul className="list-group">
        {/* 병원 정보 상태 */}
        <DataStatusItem 
          label="병원 정보" 
          loading={hospitalLoading} 
          error={hospitalError} 
          count={hospitalCount} 
          loaded={hospitals.length} 
        />
        
        {/* 의사 정보 상태 */}
        <DataStatusItem 
          label="의사 정보" 
          loading={doctorLoading} 
          error={doctorError} 
          count={doctorCount} 
          loaded={doctors.length} 
        />

        {/* 질병 정보 상태 */}
        <DataStatusItem 
          label="질병 정보" 
          loading={diseaseLoading} 
          error={diseaseError} 
          count={diseaseCount} 
          loaded={diseases.length} 
        />

        {/* 증상 정보 상태 */}
        <DataStatusItem 
          label="증상 정보" 
          loading={symptomLoading} 
          error={symptomError} 
          count={symptomCount} 
          loaded={symptoms.length} 
        />
      </ul>
    </div>
  );
}

export default DataStatusList;
