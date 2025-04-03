// symptomSlice.js

// Redux 상태 관리 및 비동기 작업 처리를 위한 관련 import
import { 
  createSlice, // 상태, 리듀서, 액션 등을 자동으로 생성
  createAsyncThunk // 비동기 작업을 처리하고 성공 및 실패 액션을 자동으로 생성
} from '@reduxjs/toolkit'; // Redux Toolkit 라이브러리
import axios from 'axios'; // HTTP 요청을 간편하게 처리

// 'symptoms/loadSymptomsCount' 액션을 비동기로 처리
export const loadSymptomsCount = createAsyncThunk(
  'symptoms/loadSymptomsCount', // 액션 이름
  async (_, { rejectWithValue }) => {
    try {
      // 백엔드 엔드포인트에서 증상 개수 요청
      const response = await axios.get('/api/symptoms/count');
      return response.data.count; // API 응답에서 증상 개수 반환
    } catch (error) {
      // 에러 발생 시 "로드 실패" 메시지 반환
      console.error('증상 개수 로드 실패:', error);
      return rejectWithValue('로드 실패'); // 실패 시 rejectWithValue로 에러 처리
    }
  }
);

// 'symptoms/loadSymptomsPage' 액션을 비동기로 처리
export const loadSymptomsPage = createAsyncThunk(
  'symptoms/loadSymptomsPage', // 액션 이름
  async ({ page, size }, { rejectWithValue }) => {
    try {
      // 백엔드 엔드포인트에서 페이지 단위로 증상 목록 요청
      const response = await axios.get('/api/symptoms', { params: { page, size } });
      return response.data.content;  
    } catch (error) {
      return rejectWithValue('로드 실패');
    }
  }
);

// 'symptoms' slice 생성
const symptomSlice = createSlice({
  name: 'symptoms', // slice의 이름을 'symptoms'로 설정
  initialState: {
    symptoms: [], // 증상 목록을 저장할 배열
    count: 0, // 전체 증상 개수
    page: 0, // 현재 페이지 번호
    loading: false, // 로딩 상태 (로딩 중일 때 true, 아닐 때 false)
    error: null, // 에러 상태 (에러가 발생하면 해당 메시지 저장)
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      // loadSymptomsCount 액션이 성공적으로 완료된 경우
      .addCase(loadSymptomsCount.fulfilled, (state, action) => {
        // 받아온 증상 개수를 state.count에 저장
        state.count = action.payload;
      })
      // loadSymptomsPage 액션이 진행중인 경우
      .addCase(loadSymptomsPage.pending, (state) => {
        // 로딩 상태를 true로 설정
        state.loading = true;
      })
      // loadSymptomsPage 액션이 성공적으로 완료된 경우
      .addCase(loadSymptomsPage.fulfilled, (state, action) => {
        // 받아온 증상 목록을 state.symptoms에 저장
        state.symptoms = action.payload;
        // 로딩 상태를 false로 설정
        state.loading = false;
      })
      // loadSymptomsPage 액션이 실패한 경우
      .addCase(loadSymptomsPage.rejected, (state, action) => {
        // 로딩 상태를 false로 설정
        state.loading = false;
        // 에러 메시지를 state.error에 저장
        state.error = action.payload || '로드 실패';  
      });
  },
});

export default symptomSlice.reducer;
