// src/components/common/Navbar.js

// React Router 관련 import 
import { Link } from 'react-router-dom'; // 페이지 간 네비게이션을 위한 Link 컴포넌트 임포트

function Navbar() {
  return (
    <nav className="navbar navbar-expand-lg navbar-light bg-light"> {/* 부트스트랩 내비게이션 바 */}
      <div className="container-fluid"> {/* 전체 내비게이션 바의 컨테이너 |*/}
        <Link className="navbar-brand" to="/">Carepick</Link> {/* 로고 (홈으로 이동하는 링크)  */}
        
        <button
          className="navbar-toggler" 
          type="button"
          data-bs-toggle="collapse" 
          data-bs-target="#navbarSupportedContent" 
          aria-controls="navbarSupportedContent"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <span className="navbar-toggler-icon"></span> {/* 토글 버튼 아이콘 */}
        </button>

        <div className="collapse navbar-collapse" id="navbarSupportedContent"> {/* 내비게이션 메뉴의 내용 */}
          <ul className="navbar-nav me-auto mb-2 mb-lg-0"> {/* 내비게이션 항목 */}
            {/* 홈 메뉴 */}
            <li className="nav-item"> 
              <Link className="nav-link active" to="/">홈</Link> {/* 홈으로 이동하는 링크 */}
            </li>

            {/* 관리 드롭다운 메뉴 */}
            <li className="nav-item dropdown"> 
              <a
                className="nav-link dropdown-toggle"
                href="#"
                role="button"
                data-bs-toggle="dropdown"
                aria-expanded="false"
              >
                관리
              </a>
              <ul className="dropdown-menu">  {/* 드롭다운 항목 리스트 */}
                {/* <li><Link className="dropdown-item" to="/hospital">병원 관리</Link></li>
                <li><Link className="dropdown-item" to="/doctor">의사 관리</Link></li>  */}
                <li><Link className="dropdown-item" to="/symptom">증상 관리</Link></li> {/* 증상 관리 링크 */}
                <li><Link className="dropdown-item" to="/disease">질병 관리</Link></li> {/* 질병 관리 링크 */}
              </ul>
            </li>

            {/* 자가진단 메뉴 */}
            <li className="nav-item"> 
              <Link className="nav-link active" to="/self-diagnosis">자가진단</Link> {/* 자가진단 링크 */}
            </li>
          </ul>
        </div>
      </div>
    </nav>
  );
}

export default Navbar;
