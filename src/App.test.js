// React Testing Library에서 제공하는 테스트 관련 함수 임포트
import { 
  render, // 화면을 렌더링하는 함수
  screen  // 렌더링된 DOM에서 요소를 찾는 함수
} from '@testing-library/react'; // 테스트용 라이브러리에서 화면 렌더링 및 DOM 요소 탐색 관련   

import App from './App'; // 테스트할 컴포넌트인 App 임포트

// 테스트 정의
test('renders learn react link', () => {
  // App 컴포넌트를 렌더링하여 DOM을 화면에 출력
  render(<App />);

  // 'learn react'라는 텍스트가 화면에 있는지 확인
  const linkElement = screen.getByText(/learn react/i);

  // 해당 요소가 화면에 존재하는지 확인
  // 'learn react' 링크가 화면에 있으면 성공
  expect(linkElement).toBeInTheDocument(); 
});
