import { useState } from 'react';
import { useApp } from '../../context/AppContext';
import * as paymentService from '../../services/paymentService';

// ── Load Razorpay SDK dynamically ─────────────────────────────────────────────
function loadRazorpay() {
  return new Promise((resolve) => {
    if (window.Razorpay) { resolve(true); return; }
    const script = document.createElement('script');
    script.src = 'https://checkout.razorpay.com/v1/checkout.js';
    script.onload  = () => resolve(true);
    script.onerror = () => resolve(false);
    document.body.appendChild(script);
  });
}

export default function PaymentModal({ car }) {
  const { closeModal, user, pushToast } = useApp();

  const [step,         setStep]         = useState(1);
  const [loading,      setLoading]      = useState(false);
  const [error,        setError]        = useState('');
  const [bookingRef,   setBookingRef]   = useState('');

  const token = 9999; // ₹9,999 booking token

  const inputStyle = {
    width: '100%',
    boxSizing: 'border-box',
    padding: '12px 14px',
    borderRadius: 10,
    border: '1.5px solid var(--bd1)',
    background: 'var(--bg1)',
    color: 'var(--tx1)',
    fontSize: 14,
    outline: 'none',
  };

  // ── Main Razorpay Payment Handler ─────────────────────────────────────────
  async function handleRazorpayPayment() {
    setError('');

    if (!user?.id) { setError('Please login to continue'); return; }
    if (!car?.id)  { setError('Invalid car listing');       return; }

    setLoading(true);

    try {
      // Step 1: Load Razorpay SDK
      const sdkLoaded = await loadRazorpay();
      if (!sdkLoaded) {
        setError('Failed to load payment gateway. Check your internet connection.');
        setLoading(false);
        return;
      }

      // Step 2: Create order from backend
      const orderRes = await paymentService.createRazorpayOrder({
        carListingId: car.id,
        buyerId:      user.id,
        sellerId:     car?.sellerId || 1,
        amount:       token * 100, // convert to paise
        currency:     'INR',
      });

      const { orderId, keyId } = orderRes.data;

      // Step 3: Open Razorpay checkout popup
      const options = {
        key:         keyId,
        amount:      token * 100,
        currency:    'INR',
        name:        'MarutiXchange',
        description: `Booking Token — ${car?.name || 'Car'}`,
        order_id:    orderId,
        prefill: {
          name:    user?.name  || '',
          email:   user?.email || '',
          contact: user?.phoneNumber || '',
        },
        theme: { color: '#1449C0' },
        modal: {
          ondismiss: () => {
            setLoading(false);
            setError('Payment cancelled. Please try again.');
          },
        },

        // ── Payment Success Handler ──────────────────────────────────────────
        handler: async function (response) {
          try {
            // Step 4: Verify payment signature in backend
            const verifyRes = await paymentService.verifyRazorpayPayment({
              razorpayOrderId:   response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
              carListingId:      car.id,
              buyerId:           user.id,
              sellerId:          car?.sellerId || 1,
              amount:            token,
            });

            setBookingRef(verifyRes.data?.transactionId || response.razorpay_payment_id);
            setStep(3);
            pushToast('Booking confirmed successfully!', 'ok');

          } catch (verifyErr) {
            setError('Payment received but verification failed. Contact support with Payment ID: ' + response.razorpay_payment_id);
            setLoading(false);
          }
        },
      };

      const rzp = new window.Razorpay(options);

      // Handle payment failure
      rzp.on('payment.failed', function (response) {
        setError('Payment failed: ' + (response.error?.description || 'Unknown error'));
        setLoading(false);

        // Notify backend of failure
        paymentService.razorpayPaymentFailed({
          razorpayOrderId: orderId,
          razorpayPaymentId: response.error?.metadata?.payment_id || '',
          razorpaySignature: '',
          carListingId: car.id,
          buyerId: user.id,
        }).catch(() => {});
      });

      rzp.open();
      setLoading(false);

    } catch (err) {
      console.error('Razorpay Error:', err);
      setError(err?.response?.data?.message || err?.message || 'Payment initiation failed. Try again.');
      setLoading(false);
    }
  }

  return (
    <>
      {/* OVERLAY */}
      <div
        onClick={closeModal}
        style={{ position: 'fixed', inset: 0, zIndex: 99998, background: 'rgba(0,0,0,.65)', backdropFilter: 'blur(5px)' }}
      />

      {/* MODAL */}
      <div style={{ position: 'fixed', inset: 0, zIndex: 99999, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px 16px' }}>
        <div style={{ width: '100%', maxWidth: 460, background: 'var(--cbg)', borderRadius: 22, overflow: 'hidden', boxShadow: 'var(--chsh)' }}>

          {/* HEADER */}
          <div style={{ padding: '18px 20px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid var(--bd0)' }}>
            <div>
              <div style={{ fontSize: 16, fontWeight: 800, color: 'var(--tx1)' }}>Secure Booking</div>
              <div style={{ fontSize: 12, color: 'var(--tx3)', marginTop: 2 }}>100% refundable booking token</div>
            </div>
            <button onClick={closeModal} style={{ width: 34, height: 34, borderRadius: '50%', border: 'none', background: 'var(--bg2)', cursor: 'pointer', color: 'var(--tx2)', fontSize: 18, fontWeight: 700 }}>×</button>
          </div>

          <div style={{ padding: 20 }}>

            {/* STEP 1 — Payment */}
            {step === 1 && (
              <>
                {/* Car Info */}
                <div style={{ display: 'flex', gap: 14, padding: 14, borderRadius: 14, background: 'var(--bg2)', marginBottom: 20 }}>
                  {car?.img && (
                    <img src={car.img} alt={car?.name} referrerPolicy="no-referrer" crossOrigin="anonymous"
                      style={{ width: 80, height: 56, borderRadius: 8, objectFit: 'contain', background: '#f0f2f5' }} />
                  )}
                  <div>
                    <div style={{ fontWeight: 700, fontSize: 14, color: 'var(--tx1)' }}>{car?.name}</div>
                    <div style={{ fontSize: 12, color: 'var(--tx3)', marginTop: 3 }}>{car?.year} · {car?.loc}</div>
                    <div style={{ fontSize: 13, color: 'var(--ac)', fontWeight: 700, marginTop: 3 }}>{car?.price}</div>
                  </div>
                </div>

                {/* Token Amount */}
                <div style={{ background: 'var(--acd)', border: '1px solid var(--acd2)', borderRadius: 12, padding: '14px 16px', marginBottom: 20 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <div style={{ fontSize: 13, fontWeight: 700, color: 'var(--tx1)' }}>Booking Token Amount</div>
                      <div style={{ fontSize: 11, color: 'var(--tx3)', marginTop: 2 }}>Fully refundable · No hidden charges</div>
                    </div>
                    <div style={{ fontSize: 22, fontWeight: 900, color: 'var(--ac)' }}>₹{token.toLocaleString('en-IN')}</div>
                  </div>
                </div>

                {/* Razorpay Info */}
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '12px 14px', background: '#f0fdf4', border: '1px solid #d1fae5', borderRadius: 10, marginBottom: 20 }}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#059669" strokeWidth="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
                  <div style={{ fontSize: 12, color: '#065f46' }}>
                    <strong>Secured by Razorpay</strong> — Pay via UPI, Card, Net Banking or Wallet
                  </div>
                </div>

                {/* Error */}
                {error && (
                  <div style={{ marginBottom: 16, padding: '10px 12px', borderRadius: 10, background: '#fef2f2', color: '#dc2626', fontSize: 12, fontWeight: 700 }}>
                    {error}
                  </div>
                )}

                {/* Pay Button */}
                <button
                  onClick={handleRazorpayPayment}
                  disabled={loading}
                  style={{
                    width: '100%', padding: 14, borderRadius: 12, border: 'none',
                    background: loading ? '#cbd5e1' : 'var(--ac)',
                    color: '#fff', fontSize: 14, fontWeight: 800,
                    cursor: loading ? 'not-allowed' : 'pointer',
                    display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
                  }}
                >
                  {loading ? (
                    'Opening Payment Gateway...'
                  ) : (
                    <>
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2"><rect x="1" y="4" width="22" height="16" rx="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg>
                      Pay ₹{token.toLocaleString('en-IN')} via Razorpay
                    </>
                  )}
                </button>

                <div style={{ textAlign: 'center', fontSize: 11, color: 'var(--tx3)', marginTop: 10 }}>
                  Supports UPI · Debit/Credit Card · Net Banking · Wallets
                </div>
              </>
            )}

            {/* STEP 3 — Success */}
            {step === 3 && (
              <div style={{ textAlign: 'center', padding: '12px 0' }}>
                <div style={{ width: 72, height: 72, borderRadius: '50%', background: '#dcfce7', color: '#16a34a', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 18px' }}>
                  <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#16a34a" strokeWidth="2.5" strokeLinecap="round">
                    <polyline points="20 6 9 17 4 12"/>
                  </svg>
                </div>
                <div style={{ fontSize: 20, fontWeight: 900, color: 'var(--tx1)', marginBottom: 10 }}>Booking Confirmed!</div>
                {bookingRef && (
                  <div style={{ fontSize: 12, color: 'var(--tx3)', marginBottom: 8 }}>
                    Booking Ref: <strong style={{ color: 'var(--tx1)' }}>{bookingRef}</strong>
                  </div>
                )}
                <div style={{ fontSize: 13, color: 'var(--tx3)', lineHeight: 1.7, marginBottom: 24 }}>
                  Your refundable booking token has been received successfully.<br />
                  Our team will contact you shortly.
                </div>
                <button onClick={closeModal} style={{ padding: '12px 28px', borderRadius: 12, border: 'none', background: 'var(--ac)', color: '#fff', fontSize: 14, fontWeight: 800, cursor: 'pointer' }}>
                  Done
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  );
}