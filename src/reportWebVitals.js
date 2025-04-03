// 웹 성능 측정
const reportWebVitals = onPerfEntry => {
  // onPerfEntry가 함수일 경우에만 성능 측정 수행
  if (onPerfEntry && onPerfEntry instanceof Function) {
    // web-vitals 라이브러리 동적으로 import 후 성능 지표 수집 함수 호출
    import('web-vitals').then(({ getCLS, getFID, getFCP, getLCP, getTTFB }) => {
      // 각 성능 지표 함수에 onPerfEntry 콜백을 전달하여 성능 데이터를 수집
      getCLS(onPerfEntry); // Cumulative Layout Shift (CLS) 측정
      getFID(onPerfEntry); // First Input Delay (FID) 측정
      getFCP(onPerfEntry); // First Contentful Paint (FCP) 측정
      getLCP(onPerfEntry); // Largest Contentful Paint (LCP) 측정
      getTTFB(onPerfEntry); // Time to First Byte (TTFB) 측정
    });
  }
};

export default reportWebVitals;
