// src/pages/Symptom.js

// React 및 Hook 관련 import
import React from 'react'; // React 라이브러리
import { useNavigate } from 'react-router-dom'; // 페이지 내 이동을 위한 useNavigate 훅

// Redux 관련 import
import { useDispatch, useSelector } from 'react-redux'; // Redux 상태를 가져오기 위한 훅

// 컴포넌트 관련 import
import PageHeader from '../../components/common/PageHeader'; // 페이지 헤더 컴포넌트
import Pagination from '../../components/common/Pagination'; // 페이지네이션 컴포넌트
import SymptomCard from '../../components/symptom/SymptomCard'; // 증상 정보를 카드 형태로 출력하는 컴포넌트
import InitialFilter from '../../components/filter/InitialFilter'; // 초성 필터 버튼 리스트 컴포넌트
import LengthFilter from '../../components/filter/LengthFilter'; // 글자 수 필터 컴포넌트
import { Spinner } from 'react-bootstrap'; // 로딩 스피너 컴포넌트

function Symptom() {
  const navigate = useNavigate(); // 페이지 이동을 위한 훅
  const dispatch = useDispatch(); // Redux에서 액션을 dispatch하기 위한 훅

  // Redux store에서 증상 관련 데이터 가져오기
  const symptoms = useSelector((state) => state.symptoms.symptoms); // 증상 목록
  const loading = useSelector((state) => state.symptoms.loading); // 로딩 상태
  const totalPages = useSelector((state) => state.symptoms.count); // 증상 총 개수

  const [selectedInitial, setSelectedInitial] = React.useState('ㄱ'); // 초성 필터 상태
  const [selectedLength, setSelectedLength] = React.useState('전체'); // 글자 수 필터 상태
  const [page, setPage] = React.useState(0); // 현재 페이지 상태

  // 초성 범위를 유니코드로 계산
  const getHangulRange = (start, end) => {
    const startCode = start.charCodeAt(0);
    const endCode = end.charCodeAt(0);
    return [startCode, endCode];
  };

  // 초성 범위 설정 (가~나, etc.)
  const INITIAL_RANGES = {
    'ㄱ': getHangulRange('가', '나'), // '가'에서 '나'까지
    'ㄲ': getHangulRange('까', '나'), // '까'에서 '나'까지
    'ㄴ': getHangulRange('나', '다'), // '나'에서 '다'까지
    'ㄷ': getHangulRange('다', '라'), // '다'에서 '라'까지
    'ㄸ': getHangulRange('따', '라'), // '따'에서 '라'까지
    'ㄹ': getHangulRange('라', '마'), // '라'에서 '마'까지
    'ㅁ': getHangulRange('마', '바'), // '마'에서 '바'까지
    'ㅂ': getHangulRange('바', '사'), // '바'에서 '사'까지
    'ㅃ': getHangulRange('빠', '사'), // '빠'에서 '사'까지
    'ㅅ': getHangulRange('사', '아'), // '사'에서 '아'까지
    'ㅆ': getHangulRange('싸', '아'), // '싸'에서 '아'까지
    'ㅇ': getHangulRange('아', '자'), // '아'에서 '자'까지
    'ㅈ': getHangulRange('자', '차'), // '자'에서 '차'까지
    'ㅉ': getHangulRange('짜', '차'), // '짜'에서 '차'까지
    'ㅊ': getHangulRange('차', '카'), // '차'에서 '카'까지
    'ㅋ': getHangulRange('카', '타'), // '카'에서 '타'까지
    'ㅌ': getHangulRange('타', '파'), // '타'에서 '파'까지
    'ㅍ': getHangulRange('파', '하'), // '파'에서 '하'까지
    'ㅎ': getHangulRange('하', '힣'), // '하'에서 '힣'까지
    '전체': getHangulRange('가', '힣'), // 모든 음절 범위
    '기타': getHangulRange('기타', '기타'), // 기타 범위 (예시로 설정, 필요에 따라 수정)
  };

  // 필터링된 증상 리스트
  const filteredSymptoms = symptoms.filter((s) => {
    const symptomCode = s.name.charCodeAt(0);

    const matchesInitial = symptomCode >= INITIAL_RANGES[selectedInitial][0] && symptomCode < INITIAL_RANGES[selectedInitial][1];

    const matchesLength =
      selectedLength === '전체' ||
      (selectedLength === '1글자' && s.name.length === 1) ||
      (selectedLength === '2글자' && s.name.length === 2) ||
      (selectedLength === '3글자 이상' && s.name.length >= 3);

    return matchesInitial && matchesLength;
  });

  // 페이지네이션을 위한 증상 목록
  const pagedSymptoms = filteredSymptoms.slice(page * 20, (page + 1) * 20);
  const filteredTotalPages = Math.ceil(filteredSymptoms.length / 20);

  return (
    <div className="container px-0">
      <PageHeader
        title="증상 관리"
        description="등록된 증상 정보를 조회하고 자세히 확인할 수 있는 페이지입니다."
      />

      {/* 초성 필터 */}
      <InitialFilter
        initials={['ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ', '전체', '기타']}
        selected={selectedInitial}
        onSelect={(initial) => {
          setSelectedInitial(initial);
          setPage(0);
        }}
      />

      {/* 글자 수 필터 */}
      <LengthFilter
        selectedLength={selectedLength}
        onSelectLength={(length) => {
          setSelectedLength(length);
          setPage(0);
        }}
      />

      {loading ? (
        <div className="text-center my-4">증상 데이터를 불러오는 중입니다...</div>
      ) : (
        <>
          <div className="d-flex flex-wrap gap-2">
            {pagedSymptoms.map((symptom) => (
              <SymptomCard
                key={symptom.id}
                symptom={symptom}
                onClickSymptom={() => navigate(`/symptom/${symptom.id}`)}
              />
            ))}
          </div>

          <Pagination
            page={page}
            totalPages={filteredTotalPages}
            onPageChange={(newPage) => setPage(newPage)} // 페이지 변경 시 상태 업데이트
          />
        </>
      )}
    </div>
  );
}

export default Symptom;
