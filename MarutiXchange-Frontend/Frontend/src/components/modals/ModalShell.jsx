import { useEffect } from 'react';

// Shared backdrop + dialog shell used by every modal.
export default function ModalShell({ onClose, children, maxWidth = 680 }) {
  useEffect(() => {
    function onKey(e) { if (e.key === 'Escape') onClose(); }
    window.addEventListener('keydown', onKey);
    const prevOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => {
      window.removeEventListener('keydown', onKey);
      document.body.style.overflow = prevOverflow;
    };
  }, [onClose]);

  return (
    <div
      onClick={onClose}
      style={{
        position: 'fixed', inset: 0, zIndex: 9999,
        background: 'rgba(5, 10, 25, .56)', backdropFilter: 'blur(6px)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        padding: 20, animation: 'fadeIn .18s ease',
      }}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        style={{
          background: 'var(--bg1)', color: 'var(--tx1)',
          borderRadius: 24, border: '1px solid var(--bd1)',
          boxShadow: 'var(--chsh)',
          width: '100%', maxWidth,
          maxHeight: '90vh', overflowY: 'auto',
          position: 'relative',
          animation: 'scaleIn .2s cubic-bezier(.22,1,.36,1)',
        }}
      >
        <button
          onClick={onClose}
          aria-label="Close"
          style={{
            position: 'absolute', top: 14, right: 14, zIndex: 2,
            width: 36, height: 36, borderRadius: '50%',
            border: 'none', background: 'var(--bg2)', color: 'var(--tx2)',
            fontSize: 18, cursor: 'pointer', display: 'flex',
            alignItems: 'center', justifyContent: 'center',
          }}
        >
          ×
        </button>
        {children}
      </div>
    </div>
  );
}
