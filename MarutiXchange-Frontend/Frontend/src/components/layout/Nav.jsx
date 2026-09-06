import { useMemo, useState, useRef, useCallback } from 'react';
import { CARS } from '../../data/cars';
import { useApp } from '../../context/AppContext';

// ── Hindi/English Voice Search AI Translator ──────────────────────────────────
async function translateVoiceSearch(spokenText) {
  try {
    const response = await fetch('https://api.groq.com/openai/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${import.meta.env.VITE_GROQ_API_KEY}`,
      },
      body: JSON.stringify({
        model: 'llama-3.3-70b-versatile',
        max_tokens: 100,
        messages: [{
          role: 'user',
          content: `You are a car search assistant for MarutiXchange — India's Maruti Suzuki marketplace.
The user spoke in Hindi, English or Hinglish: "${spokenText}"
Extract car search keywords in English only.
Examples:
"mujhe swift chahiye" → "Swift"
"Delhi mein brezza dikhao" → "Brezza Delhi"
"aath lakh se kam car" → "under 8 lakhs"
"petrol wali gaadi" → "petrol"
"kam chalai hui SUV" → "low km SUV"
"family ke liye car" → "family car"
"sabse sasti Maruti" → "Alto"
"breeza dikhaao" → "Brezza"
"WagonR ka rate" → "WagonR"
Reply ONLY in JSON: {"searchQuery": "English keywords", "language": "Hindi or English or Hinglish"}`,
        }],
      }),
    });
    const data = await response.json();
    const text = data?.choices?.[0]?.message?.content || '';
    const clean = text.replace(/```json|```/g, '').trim();
    const jsonMatch = clean.match(/\{[\s\S]*?\}/);
    return JSON.parse(jsonMatch ? jsonMatch[0] : clean);
  } catch (err) {
    return { searchQuery: spokenText, language: 'English' };
  }
}

// ── AI Search Intent Parser ────────────────────────────────────────────────────
// Understands: "automatic Swift under 6L", "first owner CNG in Delhi" etc.
async function parseSearchIntent(query) {
  try {
    const response = await fetch('https://api.groq.com/openai/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${import.meta.env.VITE_GROQ_API_KEY}`,
      },
      body: JSON.stringify({
        model: 'llama-3.3-70b-versatile',
        max_tokens: 150,
        messages: [{
          role: 'user',
          content: `Extract car search filters from this query: "${query}"

Maruti Suzuki models: Swift, Brezza, Baleno, WagonR, Dzire, Fronx, Ertiga, XL6, Jimny, Alto K10, Celerio, S-Presso, Ignis, Ciaz, Eeco, Invicto, Grand Vitara

Reply ONLY in this exact JSON format, use null for unknown fields:
{
  "model": "Swift or Brezza etc or null",
  "maxPrice": 500000 or null,
  "minPrice": null,
  "transmission": "Manual or Automatic or null",
  "fuel": "Petrol or Diesel or CNG or null",
  "location": "city name or null",
  "owner": "1st or 2nd or null",
  "type": "suv or hatchback or sedan or mpv or null"
}

Examples:
"automatic Swift under 6L" → {"model":"Swift","maxPrice":600000,"transmission":"Automatic","fuel":null,"location":null,"owner":null,"type":null,"minPrice":null}
"first owner CNG in Delhi" → {"model":null,"maxPrice":null,"transmission":null,"fuel":"CNG","location":"Delhi","owner":"1st","type":null,"minPrice":null}
"SUV under 10 lakhs" → {"model":null,"maxPrice":1000000,"transmission":null,"fuel":null,"location":null,"owner":null,"type":"suv","minPrice":null}
"Brezza Bangalore" → {"model":"Brezza","maxPrice":null,"transmission":null,"fuel":null,"location":"Bangalore","owner":null,"type":null,"minPrice":null}`,
        }],
      }),
    });
    const data = await response.json();
    const text = data?.choices?.[0]?.message?.content || '';
    const clean = text.replace(/```json|```/g, '').trim();
    const jsonMatch = clean.match(/\{[\s\S]*?\}/);
    return JSON.parse(jsonMatch ? jsonMatch[0] : clean);
  } catch (err) {
    return null;
  }
}

export default function Nav() {
  const { navigate, openModal, user, logoutUser, setSearchQuery, setSearchIntent } = useApp();
  const [query,        setQuery]        = useState('');
  const [focused,      setFocused]      = useState(false);
  const [showMenu,     setShowMenu]     = useState(false);
  const [isListening,  setIsListening]  = useState(false);
  const [voiceStatus,  setVoiceStatus]  = useState('');
  const [voiceText,    setVoiceText]    = useState('');
  const [voiceLang,    setVoiceLang]    = useState('');
  const [intentBadges, setIntentBadges] = useState([]);
  const recognitionRef = useRef(null);

  const corrections = {
    'breeza':'brezza','breza':'brezza','brezaa':'brezza',
    'swif':'swift','switf':'swift',
    'balano':'baleno','belano':'baleno',
    'vitaara':'vitara','vitarra':'vitara',
    'desire':'dzire','frox':'fronx',
    'artiga':'ertiga','celario':'celerio',
    'invicta':'invicto','alti':'alto','jimmy':'jimny',
  };

  const suggestions = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return [];
    const corrected = corrections[q] || q;
    const qClean = corrected.replace('maruti ','').replace('maruti','').trim();
    return CARS.filter((c) => {
      const name = c.name.toLowerCase();
      const loc  = c.loc.toLowerCase();
      const type = c.type?.toLowerCase() || '';
      return (
        name.includes(corrected) || name.includes(qClean) ||
        loc.includes(corrected)  || type.includes(corrected) ||
        corrected.split(' ').every(w => w.length > 1 && name.includes(w))
      );
    }).slice(0, 6);
  }, [query]);

  async function applySearch(text) {
    setQuery(text);
    setIntentBadges([]);
    if (text.trim().length > 1) {
      setSearchQuery(text.trim());

      // ── AI Intent Parsing — understand filters from natural language ──
      const intent = await parseSearchIntent(text);
      if (intent) {
        // Pass intent to AppContext so BuyPage can apply filters
        if (setSearchIntent) setSearchIntent(intent);

        // Build badge labels to show user what was understood
        const badges = [];
        if (intent.model)        badges.push(intent.model);
        if (intent.transmission) badges.push(intent.transmission);
        if (intent.fuel)         badges.push(intent.fuel);
        if (intent.owner)        badges.push(intent.owner + ' Owner');
        if (intent.type)         badges.push(intent.type.toUpperCase());
        if (intent.location)     badges.push(intent.location);
        if (intent.maxPrice)     badges.push('Under ₹' + (intent.maxPrice/100000).toFixed(0) + 'L');
        if (intent.minPrice)     badges.push('Above ₹' + (intent.minPrice/100000).toFixed(0) + 'L');
        setIntentBadges(badges);
      }

      navigate('buy');
    } else {
      setSearchQuery('');
      if (setSearchIntent) setSearchIntent(null);
    }
  }

  const startVoiceSearch = useCallback(async () => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SpeechRecognition) {
      setVoiceStatus('error');
      setVoiceText('Voice not supported. Please use Chrome browser.');
      setTimeout(() => { setVoiceStatus(''); setVoiceText(''); }, 3000);
      return;
    }
    if (isListening) {
      recognitionRef.current?.stop();
      setIsListening(false);
      setVoiceStatus('');
      return;
    }
    const recognition = new SpeechRecognition();
    recognitionRef.current = recognition;
    recognition.lang = 'hi-IN';
    recognition.continuous = false;
    recognition.interimResults = true;
    recognition.maxAlternatives = 3;

    recognition.onstart = () => {
      setIsListening(true);
      setVoiceStatus('listening');
      setVoiceText('');
      setVoiceLang('');
    };

    recognition.onresult = async (event) => {
      const transcript = Array.from(event.results).map(r => r[0].transcript).join('');
      setVoiceText(transcript);
      if (event.results[0].isFinal) {
        setIsListening(false);
        setVoiceStatus('processing');
        const result = await translateVoiceSearch(transcript);
        setVoiceLang(result.language || '');
        setVoiceStatus('done');
        applySearch(result.searchQuery || transcript);
        setTimeout(() => { setVoiceStatus(''); setVoiceText(''); setVoiceLang(''); }, 3000);
      }
    };

    recognition.onerror = (e) => {
      setIsListening(false);
      setVoiceStatus('error');
      setVoiceText(e.error === 'no-speech' ? 'No speech heard. Try again.' : e.error === 'not-allowed' ? 'Allow microphone access first.' : 'Error: ' + e.error);
      setTimeout(() => { setVoiceStatus(''); setVoiceText(''); }, 3000);
    };

    recognition.onend = () => setIsListening(false);
    recognition.start();
  }, [isListening]);

  const micBg    = isListening ? '#fee2e2' : voiceStatus === 'done' ? '#d1fae5' : voiceStatus === 'processing' ? '#fef3c7' : 'transparent';
  const micColor = isListening ? '#ef4444' : voiceStatus === 'done' ? '#059669' : voiceStatus === 'processing' ? '#f59e0b' : voiceStatus === 'error' ? '#dc2626' : 'var(--tx3)';

  return (
    <nav className="nav" id="siteNav">
      <div className="nav-search" style={{ position: 'relative' }}>
        <div className="nav-si">
          <svg width="14" height="14" fill="none" stroke="var(--tx3)" strokeWidth="1.8" viewBox="0 0 24 24" style={{ flexShrink: 0 }}>
            <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input
            type="text"
            placeholder={isListening ? '🎤 Bol ke search karo...' : 'Search ya Hindi mein bolo — Brezza, Swift...'}
            value={isListening ? voiceText : query}
            onChange={(e) => { if (!isListening) applySearch(e.target.value); }}
            onFocus={() => setFocused(true)}
            onBlur={() => setTimeout(() => setFocused(false), 150)}
            autoComplete="off"
            style={{ flex: 1, color: isListening ? '#ef4444' : 'inherit' }}
          />

          {/* Mic Button */}
          <button onClick={startVoiceSearch}
            title="Hindi ya English mein bolo"
            style={{ background: micBg, border: 'none', cursor: 'pointer', padding: '4px 6px', borderRadius: 6, display: 'flex', alignItems: 'center', flexShrink: 0, transition: 'all .2s' }}>
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke={micColor} strokeWidth="2" strokeLinecap="round"
              style={{ animation: isListening ? 'micPulse 1s infinite' : 'none' }}>
              <path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/>
              <path d="M19 10v2a7 7 0 0 1-14 0v-2"/>
              <line x1="12" y1="19" x2="12" y2="23"/>
              <line x1="8" y1="23" x2="16" y2="23"/>
            </svg>
          </button>
        </div>

        {/* ── AI Intent Badges — shows what AI understood ── */}
        {intentBadges.length > 0 && (
          <div style={{ position: 'absolute', top: '110%', left: 0, right: 0, zIndex: 55, background: '#eff6ff', border: '1px solid #bfdbfe', borderRadius: 10, padding: '8px 12px', display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
            <span style={{ fontSize: 11, color: '#1449C0', fontWeight: 700 }}>AI understood:</span>
            {intentBadges.map((b, i) => (
              <span key={i} style={{ background: '#1449C0', color: '#fff', fontSize: 11, fontWeight: 700, padding: '2px 10px', borderRadius: 999 }}>{b}</span>
            ))}
            <button onClick={() => { setIntentBadges([]); setQuery(''); setSearchQuery(''); if (setSearchIntent) setSearchIntent(null); }}
              style={{ marginLeft: 'auto', background: 'none', border: 'none', color: '#6b7280', fontSize: 12, cursor: 'pointer', fontWeight: 600 }}>
              Clear
            </button>
          </div>
        )}

        {/* Voice status bar */}
        {voiceStatus && (
          <div style={{
            position: 'absolute', top: '110%', left: 0, right: 0, zIndex: 60,
            background: voiceStatus==='error'?'#fee2e2':voiceStatus==='done'?'#d1fae5':voiceStatus==='processing'?'#fef3c7':'#eff6ff',
            border: `1px solid ${voiceStatus==='error'?'#fecaca':voiceStatus==='done'?'#a7f3d0':voiceStatus==='processing'?'#fde68a':'#bfdbfe'}`,
            borderRadius: 10, padding: '8px 14px', display: 'flex', alignItems: 'center', gap: 10,
          }}>
            {voiceStatus === 'listening' && (
              <div style={{ display: 'flex', gap: 2, alignItems: 'center', height: 18 }}>
                {[1,2,3,4,3,2,1].map((h,i) => (
                  <div key={i} style={{ width: 3, height: h*4, background: '#ef4444', borderRadius: 2, animation: `waveBar ${0.4+i*0.1}s ease infinite alternate` }}/>
                ))}
              </div>
            )}
            {voiceStatus === 'processing' && <span style={{ fontSize: 14 }}>⚡</span>}
            {voiceStatus === 'done'       && <span style={{ fontSize: 14 }}>✅</span>}
            {voiceStatus === 'error'      && <span style={{ fontSize: 14 }}>❌</span>}
            <div>
              <div style={{ fontSize: 12, fontWeight: 700, color: voiceStatus==='error'?'#dc2626':voiceStatus==='done'?'#059669':voiceStatus==='processing'?'#92400e':'#1449C0' }}>
                {voiceStatus==='listening'  && 'Listening... Bolo abhi'}
                {voiceStatus==='processing' && 'AI translate kar raha hai...'}
                {voiceStatus==='done'       && `Search ho gaya! "${query}" ${voiceLang ? `(${voiceLang} detected)` : ''}`}
                {voiceStatus==='error'      && 'Voice search failed'}
              </div>
              {voiceText && (
                <div style={{ fontSize: 11, color: '#666', marginTop: 2 }}>"{voiceText}"</div>
              )}
            </div>
          </div>
        )}

        {/* Search dropdown */}
        {focused && !voiceStatus && suggestions.length > 0 && (
          <div className="sdd" style={{ position: 'absolute', top: '105%', left: 0, right: 0, background: 'var(--bg1)', border: '1px solid var(--bd1)', borderRadius: 14, boxShadow: '0 12px 40px rgba(0,0,0,.12)', padding: 6, zIndex: 50, maxHeight: 360, overflowY: 'auto' }}>
            {suggestions.map((c) => (
              <button key={c.id} onMouseDown={(e) => { e.preventDefault(); openModal('car', { carId: c.id }); setQuery(''); }}
                style={{ display: 'flex', alignItems: 'center', gap: 10, width: '100%', padding: '8px 10px', border: 'none', background: 'transparent', color: 'var(--tx1)', fontSize: 13, cursor: 'pointer', borderRadius: 8, textAlign: 'left' }}
                onMouseEnter={(e) => e.currentTarget.style.background='var(--bg2)'}
                onMouseLeave={(e) => e.currentTarget.style.background='transparent'}
              >
                <img src={c.img} alt="" style={{ width: 44, height: 32, borderRadius: 6, objectFit: 'cover' }} />
                <div style={{ flex: 1 }}>
                  <div style={{ fontWeight: 600 }}>{c.name}</div>
                  <div style={{ fontSize: 11, color: 'var(--tx3)' }}>{c.year} · {c.loc} · {c.price}</div>
                </div>
              </button>
            ))}
          </div>
        )}

        <style>{`
          @keyframes micPulse { 0%,100%{opacity:1;transform:scale(1)} 50%{opacity:.5;transform:scale(1.3)} }
          @keyframes waveBar  { from{transform:scaleY(.5)} to{transform:scaleY(1.5)} }
        `}</style>
      </div>

      <div className="nav-links">
        <a className="nav-lnk" href="#" onClick={(e) => { e.preventDefault(); navigate('buy'); }}>Buy Cars</a>
        <a className="nav-lnk" href="#" onClick={(e) => { e.preventDefault(); user ? openModal('sell') : openModal('login'); }}>Sell Car</a>
        <a className="nav-lnk" href="#" onClick={(e) => { e.preventDefault(); navigate('home'); }}>Home</a>
        <a className="nav-lnk nav-bid-lnk" href="#" onClick={(e) => { e.preventDefault(); navigate('bidding'); }}>
          Live Bids <span className="bid-live-dot"/>
        </a>
        <a className="nav-lnk" href="#" onClick={(e) => { e.preventDefault(); user ? navigate('dashboard') : openModal('login'); }}>Dashboard</a>
      </div>

      {user ? (
        <div style={{ position: 'relative' }}>
          <button onClick={() => setShowMenu(v => !v)}
            style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '8px 14px', background: 'var(--acd)', border: '1px solid var(--acd2)', borderRadius: 10, cursor: 'pointer', color: 'var(--ac)', fontWeight: 700, fontSize: 13 }}>
            <div style={{ width: 26, height: 26, borderRadius: '50%', background: 'var(--ac)', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 12, fontWeight: 800 }}>
              {user.name?.[0]?.toUpperCase() || 'U'}
            </div>
            {user.name?.split(' ')[0]}
          </button>
          {showMenu && (
            <div style={{ position: 'absolute', top: '110%', right: 0, background: 'var(--bg1)', border: '1px solid var(--bd1)', borderRadius: 12, boxShadow: '0 8px 32px rgba(0,0,0,.12)', padding: 6, zIndex: 100, minWidth: 160 }}>
              <button onClick={() => { navigate('dashboard'); setShowMenu(false); }} style={{ display: 'block', width: '100%', padding: '10px 14px', background: 'none', border: 'none', color: 'var(--tx1)', fontSize: 13, cursor: 'pointer', textAlign: 'left', borderRadius: 8, fontWeight: 600 }}>My Dashboard</button>
              <button onClick={() => { openModal('profile'); setShowMenu(false); }} style={{ display: 'block', width: '100%', padding: '10px 14px', background: 'none', border: 'none', color: 'var(--tx1)', fontSize: 13, cursor: 'pointer', textAlign: 'left', borderRadius: 8, fontWeight: 600 }}>Edit Profile</button>
              <button onClick={() => { openModal('sell'); setShowMenu(false); }} style={{ display: 'block', width: '100%', padding: '10px 14px', background: 'none', border: 'none', color: 'var(--tx1)', fontSize: 13, cursor: 'pointer', textAlign: 'left', borderRadius: 8, fontWeight: 600 }}>List My Car</button>
              <div style={{ height: 1, background: 'var(--bd1)', margin: '4px 8px' }}/>
              <button onClick={() => { logoutUser(); setShowMenu(false); }} style={{ display: 'block', width: '100%', padding: '10px 14px', background: 'none', border: 'none', color: '#ef4444', fontSize: 13, cursor: 'pointer', textAlign: 'left', borderRadius: 8, fontWeight: 600 }}>Logout</button>
            </div>
          )}
        </div>
      ) : (
        <button className="btn-primary" onClick={() => openModal('login')}>Sign In</button>
      )}
    </nav>
  );
}