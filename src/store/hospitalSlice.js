// src/store/hospitalSlice.js

// Redux 상태 관리 및 비동기 작업을 위한 관련 import
import { 
  createSlice, // 상태, 리듀서, 액션 등을 자동으로 생성
  createAsyncThunk // 비동기 작업을 처리하고 성공 및 실패 액션을 자동으로 생성
} from '@reduxjs/toolkit'; // Redux Toolkit 라이브러리
import axios from 'axios'; // HTTP 요청을 간편하게 처리

// 'hospitals/loadHospitalsCount' 액션을 비동기로 처리
export const loadHospitalsCount = createAsyncThunk('hospitals/loadHospitalsCount', async () => {
  try {
    // 백엔드 엔드포인트에서 병원 개수 요청
    const response = await axios.get('/api/hospitals/count');
    return response.data.count;  // API 응답에서 병원 개수 반환
  } catch (error) {
    // API 요청 실패 시 에러 메시지 출력
    console.error('병원 개수 로딩 실패:', error);

    // 에러 발생 시 에러를 throw하여 상태를 rejected로 변경
    throw error;
  }
});

// 'hospitals/loadHospitalsPage' 액션을 비동기로 처리
export const loadHospitalsPage = createAsyncThunk('hospitals/loadHospitalsPage', async ({ page, size }) => {
  try {
    // 백엔드 엔드포인트에서 페이지 단위로 병원 목록 요청
    const response = await axios.get('/api/hospitals', {
      params: { 
        page, // 페이지 수
        size // 데이터 개수 
      },
    });
    return response.data.content; // API 응답에서 병원 목록 반환
  } catch (error) {
    // API 요청 실패 시 에러 메시지 출력
    console.error('병원 목록 로딩 실패:', error);

    // 에러 발생 시 에러를 throw하여 상태를 rejected로 변경
    throw error;
  }
});

// 'hospitals' slice 생성
const hospitalSlice = createSlice({
  name: 'hospitals', // slice의 이름을 'hospitals'로 설정
  initialState: {
    hospitals: [], // 병원 목록을 저장할 배열
    count: 0, // 전체 병원 개수
    loading: false, // 로딩 상태 (로딩 중일 때 true, 아닐 때 false)
    error: null, // 에러 상태 (에러가 발생하면 해당 메시지 저장)
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      // loadHospitalsCount 액션이 성공적으로 완료된 경우
      .addCase(loadHospitalsCount.fulfilled, (state, action) => {
        // 받아온 병원 개수를 state.count에 저장
        state.count = action.payload;
      })
      // loadHospitalsPage 액션이 진행중인 경우
      .addCase(loadHospitalsPage.pending, (state) => {
        // 로딩 상태를 true로 설정
        state.loading = true;
      })
      // loadHospitalsPage 액션이 성공적으로 완료된 경우
      .addCase(loadHospitalsPage.fulfilled, (state, action) => {
        // 받아온 병원 목록을 state.hospitals에 저장
        state.hospitals = action.payload;
        // 로딩 상태를 false로 설정
        state.loading = false;
      })
      // loadHospitalsPage 액션이 실패한 경우
      .addCase(loadHospitalsPage.rejected, (state, action) => {
        // 로딩 상태를 false로 설정
        state.loading = false;
        // 에러 메시지를 state.error에 저장
        state.error = action.error.message;
      });
  },
});

export default hospitalSlice.reducer;
