import { useApp } from '../../context/AppContext';
import LoginModal from './LoginModal';
import CarDetailModal from './CarDetailModal';
import SellModal from './SellModal';
import PaymentModal from './PaymentModal';
import AuctionCreateModal from './AuctionCreateModal';
import ProfileModal from './Profilemodel';

export default function ModalHost() {
  const { modal, closeModal } = useApp();
  if (!modal) return null;

  switch (modal.type) {
    case 'login':          return <LoginModal />;
    case 'car':            return <CarDetailModal carId={modal.payload.carId} />;
    case 'sell':           return <SellModal />;
    case 'payment':        return <PaymentModal car={modal.payload?.car} onClose={closeModal} />;
    case 'createAuction':  return <AuctionCreateModal onClose={closeModal} />;
    case 'profile':        return <ProfileModal />;
    default:               return null;
  }
}