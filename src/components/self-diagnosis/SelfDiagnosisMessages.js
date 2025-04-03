import React from 'react';

function SelfDiagnosisMessages({ messages }) {
  return (
    <div
      className="mx-auto p-3 rounded"
      style={{
        maxWidth: '960px',
        height: '500px',
        overflowY: 'scroll',
        paddingBottom: '80px',
        backgroundColor: 'rgba(13,110,253,0.2)',
      }}
    >
      {messages.map((msg, index) => (
        <div
          key={index}
          className={`mb-2 text-${msg.role === 'user' ? 'end' : 'start'}`}
        >
          <span
            className={`d-inline-block p-2 rounded ${msg.role === 'user' ? 'bg-white text-dark border' : 'bg-primary text-white'}`}
            style={{
              maxWidth: '70%',
              wordBreak: 'break-word',
              whiteSpace: 'pre-line', // 이 줄 추가하면 \n 이 줄바꿈으로 출력돼!
            }}
          >
            {msg.content}
          </span>
        </div>
      ))}
    </div>
  );
}

export default SelfDiagnosisMessages;
