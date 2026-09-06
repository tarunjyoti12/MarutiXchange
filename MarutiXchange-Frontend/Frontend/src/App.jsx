import { AppProvider, useApp } from './context/AppContext';
import SegmentBar from './components/layout/SegmentBar';
import Nav from './components/layout/Nav';
import Footer from './components/layout/Footer';
import HomePage from './pages/HomePage';
import BuyPage from './pages/BuyPage';
import BiddingPage from './pages/BiddingPage';
import DashboardPage from './pages/DashboardPage';
import ModalHost from './components/modals/ModalHost';
import Toasts from './components/Toasts';
import ChatBubble from './components/ChatBubble';
import './styles/global.css';
import './styles/app.css';

function NotFoundPage() {
  const { navigate } = useApp();
  return (
    <div style={{ maxWidth: 600, margin: '80px auto', textAlign: 'center', padding: '40px 24px' }}>
      <div style={{ fontSize: 80, fontWeight: 900, color: 'var(--ac)', lineHeight: 1, marginBottom: 8 }}>404</div>
      <h2 style={{ fontSize: 24, fontWeight: 800, color: 'var(--tx1)', marginBottom: 10 }}>Page not found</h2>
      <p style={{ fontSize: 14, color: 'var(--tx3)', marginBottom: 28 }}>
        The page you are looking for does not exist or has been moved.
      </p>
      <div style={{ display: 'flex', gap: 12, justifyContent: 'center' }}>
        <button onClick={() => navigate('home')}
          style={{ padding: '12px 24px', fontSize: 14, fontWeight: 700, background: 'var(--ac)', color: '#fff', border: 'none', borderRadius: 12, cursor: 'pointer' }}>
          Go to Home
        </button>
        <button onClick={() => navigate('buy')}
          style={{ padding: '12px 24px', fontSize: 14, fontWeight: 700, background: 'var(--bg2)', color: 'var(--tx1)', border: '1.5px solid var(--bd1)', borderRadius: 12, cursor: 'pointer' }}>
          Browse Cars
        </button>
      </div>
    </div>
  );
}

function Router() {
  const { page } = useApp();
  switch (page) {
    case 'home':      return <HomePage />;
    case 'buy':       return <BuyPage />;
    case 'bidding':   return <BiddingPage />;
    case 'dashboard': return <DashboardPage />;
    default:          return <NotFoundPage />;
  }
}

export default function App() {
  return (
    <AppProvider>
      <SegmentBar />
      <Nav />
      <main>
        <Router />
      </main>
      <Footer />
      <ChatBubble />
      <ModalHost />
      <Toasts />
    </AppProvider>
  );
}