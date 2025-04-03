// src/components/hospital/OperatingHoursField.jsx
import React, { useState } from 'react';
import TimePicker from 'react-time-picker'; // 시간 선택기
import 'react-time-picker/dist/TimePicker.css'; // 시간 선택기 CSS

const OperatingHoursField = ({ operatingHours = {}, onChange }) => {
  const [selectedDays, setSelectedDays] = useState([]); // 선택된 요일들
  const [selectedStartTime, setSelectedStartTime] = useState(''); // 시작 시간
  const [selectedEndTime, setSelectedEndTime] = useState(''); // 종료 시간

  // 요일 선택 처리 함수
  const handleDayChange = (e) => {
    const { value, checked } = e.target;
    setSelectedDays((prev) =>
      checked ? [...prev, value] : prev.filter((day) => day !== value)
    );
  };

  // 시작 시간 변경 처리 함수
  const handleStartTimeChange = (time) => {
    setSelectedStartTime(time);
  };

  // 종료 시간 변경 처리 함수
  const handleEndTimeChange = (time) => {
    setSelectedEndTime(time);
  };

  // 운영 시간 추가 처리 함수
  const handleAddOperatingHours = () => {
    if (selectedDays.length > 0 && selectedStartTime && selectedEndTime) {
      const newOperatingHours = { ...operatingHours };
      selectedDays.forEach((day) => {
        newOperatingHours[day] = `${selectedStartTime} ~ ${selectedEndTime}`;
      });
      onChange(newOperatingHours);  // 임시로 테이블에 표시되는 데이터를 업데이트
      setSelectedDays([]);
      setSelectedStartTime('');
      setSelectedEndTime('');
    }
  };

  return (
    <div>
      <label className="form-label" style={{ fontWeight: 'bold', fontSize: '1.2rem' }}>운영 시간</label>
      
      {/* 요일 선택 (수평 배치) */}
      <div className="d-flex gap-3 mb-3">
        {["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"].map((day) => (
          <div key={day} className="form-check">
            <input
              className="form-check-input"
              type="checkbox"
              id={day}
              value={day}
              onChange={handleDayChange}
            />
            <label className="form-check-label" htmlFor={day}>
              {day === "Monday" ? "월요일" : day === "Tuesday" ? "화요일" : day === "Wednesday" ? "수요일" : day === "Thursday" ? "목요일" : day === "Friday" ? "금요일" : day === "Saturday" ? "토요일" : "일요일"}
            </label>
          </div>
        ))}
      </div>

      {/* 시작 시간 및 종료 시간 선택 (요일 선택 시 활성화) */}
      {selectedDays.length > 0 && (
        <div className="d-flex gap-2">
          <TimePicker 
            value={selectedStartTime || '00:00'} // 기본값 설정
            onChange={handleStartTimeChange} 
            format="HH:mm" 
            disableClock
          />
          <span>-</span>
          <TimePicker 
            value={selectedEndTime || '00:00'} // 기본값 설정
            onChange={handleEndTimeChange} 
            format="HH:mm" 
            disableClock
          />
        </div>
      )}

      {/* 저장 버튼 (실제 폼 제출을 방지하는 type="button") */}
      <button className="btn btn-primary mt-3" type="button" onClick={handleAddOperatingHours}>저장</button>

      {/* 운영 시간 표시 */}
      <div className="mt-3">
        <table className="table">
          <thead>
            <tr>
              <th>요일</th>
              <th>운영 시간</th>
            </tr>
          </thead>
          <tbody>
            {["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"].map((day) => (
              <tr key={day}>
                <td>{day === "Monday" ? "월요일" : day === "Tuesday" ? "화요일" : day === "Wednesday" ? "수요일" : day === "Thursday" ? "목요일" : day === "Friday" ? "금요일" : day === "Saturday" ? "토요일" : "일요일"}</td>
                <td>{operatingHours[day] || "운영 시간 없음"}</td>  {/* 기본값으로 '운영 시간 없음' 처리 */}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default OperatingHoursField;
