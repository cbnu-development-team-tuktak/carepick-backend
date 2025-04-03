// src/App.js

// React 및 라우팅 관련 라이브러리 임포트
import React from 'react'; // React 라이브러리 
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'; // React Router 라이브러리 (라우팅 기능 제공)

// 공통 컴포넌트 import
import Navbar from './components/common/Navbar'; // 상단 네비게이션 바 컴포넌트 (앱 전체에서 공통으로 사용)

// 페이지 컴포넌트 import
import Home from './pages/Home'; // 홈 페이지 컴포넌트

import Hospital from './pages/hospital/Hospital'; // 병원 목록 페이지 컴포넌트
import HospitalDetail from './pages/hospital/HospitalDetail'; // 병원 상세 페이지 컴포넌트

import Doctor from './pages/doctor/Doctor'; // 의사 목록 페이지 컴포넌트
import DoctorDetail from './pages/doctor/DoctorDetail'; // 의사 상세 페이지 컴포넌트
import DoctorAdd from './pages/doctor/DoctorAdd'; // 의사 추가 페이지 컴포넌트

import Disease from './pages/disease/Disease'; // 질병 목록 페이지 컴포넌트
import DiseaseDetail from './pages/disease/DiseaseDetail'; // 질병 상세 페이지 컴포넌트

import Symptom from './pages/symptom/Symptom'; // 증상 목록 페이지 컴포넌트
import SymptomDetail from './pages/symptom/SymptomDetail'; // 증상 상세 페이지 컴포넌트

import SelfDiagnosis from './pages/self-diagnosis/SelfDiagnosis'; // 자가진단 테스트 컴포넌트

function App() {
  return (
    <Router>
      {/* 상단 내비게이션 컴포넌트 */}
      <Navbar />

      {/* 본문 영역 */}
      <div className="container py-4">
        <Routes>
          {/* 홈 페이지 */}
          <Route path="/" element={<Home />} />

          {/* 병원 목록 페이지 */}
          <Route path="/hospital" element={<Hospital />} />

          {/* 병원 상세 페이지 (병원 ID를 이용해 해당 병원 정보 표시) */}
          <Route path="/hospital/:id" element={<HospitalDetail />} />

          {/* 의사 목록 페이지 */}
          <Route path="/doctor" element={<Doctor />} />

          {/* 의사 상세 페이지 (의사 ID를 이용해 해당 의사 정보 표시) */}
          <Route path="/doctor/:id" element={<DoctorDetail />} /> 

          {/* 의사 추가 페이지 */}
          <Route path="/doctor/add" element={<DoctorAdd />} /> 

          {/* 질병 목록 페이지 */}
          <Route path="/disease" element={<Disease />} />

          {/* 질병 상세 페이지 (질병 ID를 이용해 해당 질병 정보 표시) */}
          <Route path="/disease/:id" element={<DiseaseDetail />} />

          {/* 증상 목록 페이지 */}
          <Route path="/symptom" element={<Symptom />} />

          {/* 증상 상세 페이지 */}
          <Route path="/symptom/:id" element={<SymptomDetail />} />

          {/* 자가진단 페이지 */}
          <Route path="/self-diagnosis" element={<SelfDiagnosis />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
