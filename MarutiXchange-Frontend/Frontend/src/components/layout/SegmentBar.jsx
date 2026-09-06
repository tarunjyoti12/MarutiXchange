import { useApp } from '../../context/AppContext';

export default function SegmentBar() {
  const { segment, setSegment, navigate, openModal, user } = useApp();

  return (
    <div className="seg-bar">
      <a
        href="#"
        className="seg-logo"
        onClick={(e) => { e.preventDefault(); navigate('home'); }}
      >
        <div className="seg-mark">
          <svg width="18" height="18" viewBox="0 0 20 20" fill="white">
            <path d="M10 2L3 7v10l7 1 7-1V7L10 2z" fillOpacity=".95" />
            <path d="M10 2l7 5-7 3-7-3 7-5z" fillOpacity=".55" />
          </svg>
        </div>
        <span className="seg-word">
          Maruti<em>Xchange</em>
        </span>
      </a>

      <div className="seg-toggle">
        <button
          className={`seg-btn arena ${segment === 'arena' ? 'active' : ''}`}
          onClick={() => setSegment('arena')}
        >
          Arena
        </button>
        <button
          className={`seg-btn nexa ${segment === 'nexa' ? 'active' : ''}`}
          onClick={() => setSegment('nexa')}
        >
          Nexa
        </button>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        {user ? (
          <button
            className="btn-primary"
            style={{ fontSize: 12, padding: '7px 16px' }}
            onClick={() => navigate('dashboard')}
          >
            Hi, {user.name.split(' ')[0]}
          </button>
        ) : (
          <button
            className="btn-primary"
            style={{ fontSize: 12, padding: '7px 16px' }}
            onClick={() => openModal('login')}
          >
            Sign In
          </button>
        )}
      </div>
    </div>
  );
}
