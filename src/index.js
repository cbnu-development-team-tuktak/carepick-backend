// React 라이브러리 import
import React from 'react';

// ReactDOM 관련 import
import ReactDOM from 'react-dom/client'; // ReactDOM을 사용해 렌더링 수행

// 스타일 관련 import
import './index.css'; // 전체 애플리케이션의 기본 CSS 스타일
import 'bootstrap/dist/css/bootstrap.min.css'; // 부트스트랩 CSS 스타일
import 'bootstrap/dist/js/bootstrap.bundle.min.js'; // 부트스트랩 JS 기능 (예: 모달, 툴팁 등)

// 앱 컴포넌트 import
import App from './App'; // 루트 앱 컴포넌트

// Redux 관련 import
import { 
  Provider // React 컴포넌트에서 Redux store를 사용할 수 있도록 제공하는 컴포넌트
} from 'react-redux';
import { 
  store // 애플리케이션의 중앙 상태 관리 시스템
} from './store';

// ReactDOM을 사용하여 'root' 요소에 React 컴포넌트를 렌더링
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <Provider store={store}>
    <App />
  </Provider>
);
