// React 관련 import
import React, { useState } from 'react';

// 공통 컴포넌트 import
import PageHeader from '../../components/common/PageHeader';
import SelfDiagnosisInput from '../../components/self-diagnosis/SelfDiagnosisInput';
import SelfDiagnosisMessages from '../../components/self-diagnosis/SelfDiagnosisMessages';

// 큰따옴표로부터 증상 키워드 추출 (띄어쓰기 허용)
const extractQuotedSymptoms = (text) => {
  const matches = text.match(/"([^\"]+)"/g);
  return matches ? matches.map(tag => tag.replace(/"/g, '').trim()) : [];
};

// 자연어 문장에서 증상 키워드 후보 추출 (예: DB에 등록된 증상 리스트와 비교)
const extractSymptomsFromText = (text, knownSymptoms) => {
  return knownSymptoms.filter(symptom => text.includes(symptom));
};

function SelfDiagnosis() {
  const [messages, setMessages] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [inputValue, setInputValue] = useState('');

  // 예시: 시스템에 등록된 증상 목록 (실제로는 DB에서 받아오거나 캐싱된 목록 사용)
  const symptomDictionary = ['기침', '두통', '가래', '복통', '콧물', '열'];

  const handleSubmit = (text) => {
    console.log('사용자 입력:', text);

    const newUserMessage = {
      id: Date.now(),
      role: 'user',
      content: text,
    };
    setMessages((prev) => [...prev, newUserMessage]);
    setIsLoading(true);

    // 1. 큰따옴표에서 증상 키워드 추출
    const keywordsFromQuotes = extractQuotedSymptoms(text);

    // 2. 일반 문장에서 증상 키워드 추출
    const keywordsFromText = extractSymptomsFromText(text, symptomDictionary);

    // 3. 중복 제거 및 병합
    const allSymptoms = Array.from(new Set([...keywordsFromQuotes, ...keywordsFromText]));

    console.log('최종 증상 키워드:', allSymptoms);

    // 응답 메시지 출력 로직 (애니메이션 포함)
    const loadingId = Date.now() + 1;
    const response = `입력한 증상 키워드: ${allSymptoms.join(', ')}\n이 증상을 잘 진료할 수 있는 병원을 안내합니다.`;

    setMessages((prev) => [...prev, { id: loadingId, role: 'gpt', content: '' }]);

    let currentChar = 0;
    const typingInterval = setInterval(() => {
      currentChar++;
      const animatedText = response.slice(0, currentChar);

      setMessages((prev) =>
        prev.map((msg) =>
          msg.id === loadingId ? { ...msg, content: animatedText } : msg
        )
      );

      if (currentChar >= response.length) {
        clearInterval(typingInterval);
        setIsLoading(false);
      }
    }, 40);
  };

  return (
    <>
      <div className="container mt-4 mb-5">
        <PageHeader
          title="자가진단"
          description="자가진단 기능을 테스트할 수 있는 화면입니다."
        />

        <SelfDiagnosisMessages messages={messages} />
      </div>

      <SelfDiagnosisInput
        onSubmit={handleSubmit}
        disabled={isLoading}
        value={inputValue}
        onChange={(val) => setInputValue(val)}
      />
    </>
  );
}

export default SelfDiagnosis;