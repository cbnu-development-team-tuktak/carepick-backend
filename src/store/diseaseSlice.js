// src/store/diseaseSlice.js

// Redux 상태 관리 및 비동기 작업 처리를 위한 관련 import
import { 
  createSlice, // 상태, 리듀서, 액션 등을 자동으로 생성
  createAsyncThunk // 비동기 작업을 처리하고 성공 및 실패 액션을 자동으로 생성
} from '@reduxjs/toolkit'; // Redux Toolkit 라이브러리
import axios from 'axios'; // HTTP 요청을 간편하게 처리

// 'disease/loadDiseasesCount' 액션을 비동기로 처리
export const loadDiseasesCount = createAsyncThunk('diseases/loadDiseasesCount', async () => {
  try {
    // 백엔드 엔드포인트에서 질병 개수 요청
    const response = await axios.get('/api/diseases/processed/count');
    return response.data.count; // API 응답에서 질병 개수를 반환
  } catch (error) {
    // API 요청 실패 시 에러 메시지 출력
    console.error('질병 개수 로딩 실패:', error);

    // 에러 발생 시 에러를 throw하여 상태를 rejected로 변경
    throw error;
  }
});

// 'diseases/loadDiseasesPage' 액션을 비동기로 처리
export const loadDiseasesPage = createAsyncThunk('diseases/loadDiseasesPage', async ({ page, size }) => {
  try {
    // 백엔드 엔드포인트에서 페이지 단위로 질병 목록 요청
    const response = await axios.get('/api/diseases/processed', {
      params: { 
        page, // 페이지 수
        size // 데이터 개수 
      } 
    });
    return response.data.content; // API 응답에서 질병 목록 반환
  } catch (error) {
    // API 요청 실패 시 에러 메시지 출력
    console.error('질병 목록 로딩 실패:', error);

    // 에러 발생 시 에러를 throw하여 상태를 rejected로 변경
    throw error;
  }
});

// 'diseases' slice 생성
const diseaseSlice = createSlice({
  name: 'diseases', // slice의 이름을 'diseases'로 설정
  initialState: { 
    diseases: [], // 질병 목록을 저장할 배열
    count: 0, // 전체 질병 개수
    loading: false, // 로딩 상태 (로딩 중일 때 true, 아닐 때 false)
    error: null, // 에러 상태 (에러가 발생하면 해당 메시지 저장)
  },
  reducers: {}, 
  extraReducers: (builder) => {
    builder
      // loadDiseasesCount 액션이 성공적으로 완료된 경우
      .addCase(loadDiseasesCount.fulfilled, (state, action) => {
        // 받아온 질병 개수를 state.count에 저장
        state.count = action.payload;
      })
      // loadDiseasesPage 액션이 진행중인 경우
      .addCase(loadDiseasesPage.pending, (state) => {
        // 로딩 상태를 true로 설정
        state.loading = true;
      })
      // loadDiseasesPage 액션이 성공적으로 완료된 경우
      .addCase(loadDiseasesPage.fulfilled, (state, action) => {
        // 받아온 질병 목록을 state.diseases에 저장
        state.diseases = action.payload;
        // 로딩 상태를 false로 설정
        state.loading = false;
      })
      // loadDiseasesPage 액션이 실패한 경우
      .addCase(loadDiseasesPage.rejected, (state, action) => {
        // 로딩 상태를 false로 설정
        state.loading = false;
        // 에러 메시지를 state.error에 저장
        state.error = action.error.message;
      });
  },
});

export default diseaseSlice.reducer;
