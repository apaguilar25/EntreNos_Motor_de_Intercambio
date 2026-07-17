import React from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

const Pagination = ({ currentPage, totalItems, pageSize = 5, onPageChange }) => {
  if (totalItems === 0) return null;

  const startItem = (currentPage - 1) * pageSize + 1;
  const endItem = Math.min(currentPage * pageSize, totalItems);
  const totalPages = Math.ceil(totalItems / pageSize);

  return (
    <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', gap: '1rem', padding: '0.5rem 0', color: 'var(--text-secondary)' }}>
      <span style={{ fontSize: '0.875rem' }}>
        {startItem}–{endItem} de {totalItems}
      </span>
      <div style={{ display: 'flex', gap: '0.25rem' }}>
        <button 
          onClick={() => onPageChange(currentPage - 1)}
          disabled={currentPage <= 1}
          style={{
            background: 'none', border: 'none', cursor: currentPage <= 1 ? 'default' : 'pointer',
            color: currentPage <= 1 ? 'var(--text-tertiary)' : 'var(--text-primary)',
            display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '0.25rem', borderRadius: '4px'
          }}
          onMouseEnter={e => !e.currentTarget.disabled && (e.currentTarget.style.backgroundColor = 'var(--bg-tertiary)')}
          onMouseLeave={e => (e.currentTarget.style.backgroundColor = 'transparent')}
        >
          <ChevronLeft size={18} />
        </button>
        <button 
          onClick={() => onPageChange(currentPage + 1)}
          disabled={currentPage >= totalPages}
          style={{
            background: 'none', border: 'none', cursor: currentPage >= totalPages ? 'default' : 'pointer',
            color: currentPage >= totalPages ? 'var(--text-tertiary)' : 'var(--text-primary)',
            display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '0.25rem', borderRadius: '4px'
          }}
          onMouseEnter={e => !e.currentTarget.disabled && (e.currentTarget.style.backgroundColor = 'var(--bg-tertiary)')}
          onMouseLeave={e => (e.currentTarget.style.backgroundColor = 'transparent')}
        >
          <ChevronRight size={18} />
        </button>
      </div>
    </div>
  );
};

export default Pagination;
