// src/pages/Home.js

// React 관련 import
import React, { useEffect } from 'react'; // React 라이브러리 및 useEffect 훅

// Redux 관련 import
import { 
  useDispatch, // Redux store에 dispatch 작업을 할 수 있도록 하는 훅
  useSelector // Redux store에서 상태를 읽어올 수 있도록 하는 훅
} from 'react-redux'; 

// 비동기 데이터 로드를 위한 액션 import
import { 
  loadHospitalsCount, // 병원 개수를 비동기로 로드하는 액션
  loadHospitalsPage // 병원 목록을 페이지 단위로 비동기로 로드하는 액션
} from '../store/hospitalSlice'; // 병원 관련 action
import { 
  loadDoctorsCount, // 의사 개수를 비동기로 로드하는 액션
  loadDoctorsPage // 의사 목록을 페이지 단위로 비동기로 로드하는 액션
} from '../store/doctorSlice'; // 의사 관련 action
import { 
  loadDiseasesCount, // 질병 개수를 비동기로 로드하는 액션
  loadDiseasesPage // 질병 목록을 페이지 단위로 비동기로 로드하는 액션
} from '../store/diseaseSlice'; // 질병 관련 action
import { 
  loadSymptomsCount, // 증상 개수를 비동기로 로드하는 액션
  loadSymptomsPage // 증상 목록을 페이지 단위로 비동기로 로드하는 액션
} from '../store/symptomSlice'; // 증상 관련 action

// 컴포넌트 import
import PageHeader from '../components/common/PageHeader'; // 페이지 제목과 설명을 표시하는 컴포넌트
import DataStatusList from '../components/home/DataStatusList'; // 데이터 항목(병원, 의사, 질병, 증상 등)의 로드 상태를 표시하는 컴포넌트

function Home() {
  // Redux store에 액션을 dispatch할 수 있도록 하는 훅
  const dispatch = useDispatch();

  // 병원 관련 변수
  const hospitalCount = useSelector((state) => state.hospitals.count); // 병원의 총 개수를 store에서 가져옴
  const hospitals = useSelector((state) => state.hospitals.hospitals); // 병원 목록을 store에서 가져옴

  // 의사 관련 변수
  const doctorCount = useSelector((state) => state.doctors.count); // 의사의 총 개수를 store에서 가져옴
  const doctors = useSelector((state) => state.doctors.doctors); // 의사 목록을 store에서 가져옴

  // 질병 관련 변수
  const diseaseCount = useSelector((state) => state.diseases.count); // 질병의 총 개수를 store에서 가져옴
  const diseases = useSelector((state) => state.diseases.diseases); // 질병 목록을 store에서 가져옴

  // 증상 관련 변수
  const symptomCount = useSelector((state) => state.symptoms.count); // 증상의 총 개수를 store에서 가져옴
  const symptoms = useSelector((state) => state.symptoms.symptoms); // 증상 목록을 store에서 가져옴

  // 처음에 증상 개수, 병원 개수, 의사 개수, 질병 개수 로드
  useEffect(() => {
    dispatch(loadHospitalsCount()); // 병원 개수 비동기 로드
    dispatch(loadDoctorsCount());  // 의사 개수 비동기 로드
    dispatch(loadDiseasesCount()); // 질병 개수 비동기 로드
    dispatch(loadSymptomsCount()); // 증상 개수 비동기 로드
  }, [dispatch]);

  // 병원 데이터를 한 번에 모두 로딩
  useEffect(() => {
    // 병원 개수가 0보다 크고 병원 데이터가 비어있는 경우
    if (hospitalCount > 0 && hospitals.length === 0) {
      dispatch(loadHospitalsPage({ page: 0, size: hospitalCount })); // 병원 목록을 로드
    }
  }, [dispatch, hospitalCount, hospitals.length]);

  // 의사 데이터를 한 번에 모두 로딩
  useEffect(() => {
    // 의사 개수가 0보다 크고 의사 데이터가 비어있는 경우
    if (doctorCount > 0 && doctors.length === 0) {
      dispatch(loadDoctorsPage({ page: 0, size: doctorCount })); // 의사 목록을 로드
    }
  }, [dispatch, doctorCount, doctors.length]);

  // 질병 데이터를 한 번에 모두 로딩
  useEffect(() => {
    // 질병 개수가 0보다 크고 질병 데이터가 비어있는 경우
    if (diseaseCount > 0 && diseases.length === 0) {
      dispatch(loadDiseasesPage({ page: 0, size: diseaseCount })); // 질병 목록을 로드
    }
  }, [dispatch, diseaseCount, diseases.length]);

  // 증상 데이터를 한 번에 모두 로딩
  useEffect(() => {
    // 증상 개수가 0보다 크고 증상 데이터가 비어있는 경우
    if (symptomCount > 0 && symptoms.length === 0) {
      dispatch(loadSymptomsPage({ page: 0, size: symptomCount })); // 증상 목록을 로드
    }
  }, [dispatch, symptomCount, symptoms.length]);

  return (
    <div className="container mt-4">
      {/* 페이지의 제목과 설명 표시 */}
      <PageHeader
        title="Carepick 관리자 홈"
        description="관리자 페이지에 오신 것을 환영합니다."
      />
      {/* 데이터 로드 상태를 표시 */}
      <DataStatusList />
    </div>
  );
}

export default Home;
