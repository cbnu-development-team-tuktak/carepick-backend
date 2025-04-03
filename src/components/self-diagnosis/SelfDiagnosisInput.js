import React, { useEffect, useState } from 'react';
import { fetchSymptoms } from '../../services/symptomService';
import { fromSymptomApiResponse } from '../../dto/SymptomDetailsResponse';

function SelfDiagnosisInput({ onSubmit, disabled, value = '', onChange }) {
  const [input, setInput] = useState(value);
  const [allSymptoms, setAllSymptoms] = useState([]);
  const [filteredSymptoms, setFilteredSymptoms] = useState([]);
  const [isLoadingSymptoms, setIsLoadingSymptoms] = useState(true);

  useEffect(() => {
    const loadSymptoms = async () => {
      try {
        const res = await fetchSymptoms(0, 1000);
        setAllSymptoms(res.content.map(fromSymptomApiResponse));
      } catch (e) {
        console.error('증상 불러오기 실패:', e);
      } finally {
        setIsLoadingSymptoms(false);
      }
    };
    loadSymptoms();
  }, []);

  // 현재 입력 중인 마지막 큰따옴표 안 키워드
  const getCurrentQuotedKeyword = (text) => {
    const match = text.match(/"([^"]*)$/); // 마지막 큰따옴표 열려있을 때만
    return match ? match[1] : '';
  };

  useEffect(() => {
    const currentKeyword = getCurrentQuotedKeyword(input);
    if (currentKeyword.length > 0 && !isLoadingSymptoms) {
      const filtered = allSymptoms
        .filter((s) => s.name.startsWith(currentKeyword))
        .slice(0, 5);
      setFilteredSymptoms(filtered);
    } else {
      setFilteredSymptoms([]);
    }
  }, [input, allSymptoms, isLoadingSymptoms]);

  const handleSend = () => {
    if (input.trim() === '' || disabled) return;
    onSubmit(input);
    setInput('');
    setFilteredSymptoms([]);
  };

  const handleInputChange = (e) => {
    setInput(e.target.value);
    onChange?.(e.target.value);
  };

  const handleSelectSuggestion = (name) => {
    const newText = input.replace(/"[^"]*$/, `"${name}" `);
    setInput(newText);
    setFilteredSymptoms([]);
    onChange?.(newText);
  };

  const currentKeyword = getCurrentQuotedKeyword(input);

  return (
    <div
      className="p-2 border-top shadow mx-auto"
      style={{
        position: 'fixed',
        bottom: 0,
        left: '50%',
        transform: 'translateX(-50%)',
        width: '100%',
        maxWidth: '960px',
        backgroundColor: '#fff',
        zIndex: 1000,
      }}
    >
      {/* 연관 검색어 추천 */}
      {currentKeyword && (
        <div className="mt-2 bg-light border rounded p-2">
          {isLoadingSymptoms ? (
            <div className="d-flex align-items-center gap-2">
              <div className="spinner-border spinner-border-sm text-secondary" role="status" />
              <span className="text-secondary">증상 정보 로드 중입니다...</span>
            </div>
          ) : filteredSymptoms.length > 0 ? (
            <>
              <strong>추천 증상:</strong>
              <div className="mt-1 d-flex flex-wrap gap-2">
                {filteredSymptoms.map((s) => (
                  <span
                    key={s.id}
                    className="badge bg-secondary"
                    style={{ cursor: 'pointer' }}
                    onClick={() => handleSelectSuggestion(s.name)}
                  >
                    "{s.name}"
                  </span>
                ))}
              </div>
            </>
          ) : (
            <div className="text-muted">일치하는 증상이 없습니다.</div>
          )}
        </div>
      )}

      {/* 입력창 */}
      <input
        type="text"
        className="form-control"
        placeholder={`현재 발생한 증상을 입력해주세요. 예: "두통" "눈이 침침함"`}
        value={input}
        onChange={handleInputChange}
        onKeyDown={(e) => e.key === 'Enter' && handleSend()}
        disabled={disabled}
      />
    </div>
  );
}

export default SelfDiagnosisInput;
