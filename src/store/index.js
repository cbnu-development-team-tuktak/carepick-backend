// Redux store 관련 import
import { configureStore } from '@reduxjs/toolkit'; // Redux의 store를 설정하는 함수
 
// Reducer 관련 import
import hospitalsReducer from './hospitalSlice'; // 병원 관련 데이터를 관리하는 리듀서
import doctorsReducer from './doctorSlice'; // 의사 관련 데이터를 관리하는 리듀서
import diseasesReducer from './diseaseSlice'; // 질병 관련 데이터를 관리하는 리듀서
import symptomsReducer from './symptomSlice'; // 증상 관련 데이터를 관리하는 리듀서

// store 설정
const store = configureStore({
  reducer: {
    hospitals: hospitalsReducer, // 병원 관련 데이터를 관리하는 리듀서 
    doctors: doctorsReducer, // 의사 관련 데이터를 관리하는 리듀서
    diseases: diseasesReducer, // 질병 관련 데이터를 관리하는 리듀서
    symptoms: symptomsReducer // 증상 관련 데이터를 관리하는 리듀서
  },
});

export { store };
