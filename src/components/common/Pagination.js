import React from 'react';

function Pagination({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null;

  const maxButtons = 5; // 최대 표시할 페이지 수
  const half = Math.floor(maxButtons / 2);

  let start = Math.max(0, page - half);
  let end = start + maxButtons;

  // 페이지 범위가 totalPages를 벗어나지 않도록 설정
  if (end > totalPages) {
    end = totalPages;
    start = Math.max(0, end - maxButtons);
  }

  const pages = [];
  for (let i = start; i < end; i++) {
    pages.push(i);
  }

  return (
    <nav aria-label="페이지네이션" className="mt-4">
      <ul className="pagination justify-content-center">
        {/* Previous 버튼 */}
        <li className={`page-item ${page === 0 ? 'disabled' : ''}`}>
          <button className="page-link" onClick={() => onPageChange(page - 1)} disabled={page === 0}>
            Previous
          </button>
        </li>

        {/* 페이지 번호들 */}
        {pages.map((p) => (
          <li
            key={p}
            className={`page-item ${p === page ? 'active' : ''}`}
            aria-current={p === page ? 'page' : undefined}
          >
            <button className="page-link" onClick={() => onPageChange(p)}>
              {p + 1}
            </button>
          </li>
        ))}

        {/* Next 버튼 */}
        <li className={`page-item ${page === totalPages - 1 ? 'disabled' : ''}`}>
          <button className="page-link" onClick={() => onPageChange(page + 1)} disabled={page === totalPages - 1}>
            Next
          </button>
        </li>
      </ul>
    </nav>
  );
}

export default Pagination;
