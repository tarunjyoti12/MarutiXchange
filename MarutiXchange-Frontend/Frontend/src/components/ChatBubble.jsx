import { useEffect, useRef, useState } from 'react';

const SYSTEM_PROMPT = `You are a helpful support agent for MarutiXchange — India's only Maruti Suzuki exclusive resale marketplace.

You help users with:
- Finding the right Maruti car to buy
- Understanding how to sell their Maruti car
- Live auction / bidding questions
- RC transfer process
- EMI calculations and loan queries
- Test drive bookings
- Price guidance for Maruti models
- Verified seller process
- Payment and escrow questions

Keep answers concise and helpful.
Do NOT answer unrelated questions.`;

const GROQ_API_KEY = import.meta.env.VITE_GROQ_API_KEY || '';

function BotIcon({ size = 18 }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <rect x="3" y="11" width="18" height="10" rx="2" />
      <path d="M12 11V7" />
      <circle cx="12" cy="5" r="2" />
      <path d="M8 15h.01M16 15h.01" />
    </svg>
  );
}

function TypingDots() {
  return (
    <div
      style={{
        display: 'flex',
        gap: 4,
        padding: '4px 0',
      }}
    >
      {[0, 1, 2].map((i) => (
        <div
          key={i}
          style={{
            width: 7,
            height: 7,
            borderRadius: '50%',
            background: 'var(--ac)',
            opacity: 0.4,
            animation: `typingBounce .9s ${i * 0.2}s ease-in-out infinite`,
          }}
        />
      ))}
    </div>
  );
}

export default function ChatBubble() {
  const [open, setOpen] = useState(false);

  const [messages, setMessages] = useState([
    {
      from: 'bot',
      text: 'Hello! I am your MarutiXchange assistant. Ask me about buying, selling, EMI, bidding, RC transfer or any Maruti model.',
    },
  ]);

  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);

  const messagesEndRef = useRef(null);

  useEffect(() => {
    if (open) {
      messagesEndRef.current?.scrollIntoView({
        behavior: 'smooth',
      });
    }
  }, [messages, open]);

  async function send() {
    const text = input.trim();

    if (!text || loading) return;

    if (!GROQ_API_KEY) {
      setMessages((m) => [
        ...m,
        {
          from: 'bot',
          text: 'Groq API key is missing. Please configure your .env file.',
        },
      ]);
      return;
    }

    const userMessage = {
      from: 'me',
      text,
    };

    setMessages((m) => [...m, userMessage]);

    setInput('');
    setLoading(true);

    setMessages((m) => [
      ...m,
      {
        from: 'bot',
        text: '...',
        isTyping: true,
      },
    ]);

    try {
      const history = [...messages, userMessage]
        .filter((m) => !m.isTyping)
        .slice(-10)
        .map((m) => ({
          role: m.from === 'me' ? 'user' : 'assistant',
          content: m.text,
        }));

      console.log('Using API KEY:', GROQ_API_KEY);

      const response = await fetch(
        'https://api.groq.com/openai/v1/chat/completions',
        {
          method: 'POST',

          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${GROQ_API_KEY}`,
          },

          body: JSON.stringify({
            model: 'llama-3.1-8b-instant',

            messages: [
              {
                role: 'system',
                content: SYSTEM_PROMPT,
              },
              ...history,
            ],

            temperature: 0.5,
            max_tokens: 200,
          }),
        }
      );

      const data = await response.json();

      console.log('FULL RESPONSE:', data);

      if (!response.ok) {
        throw new Error(
          data?.error?.message || 'Groq API request failed'
        );
      }

      const reply =
        data?.choices?.[0]?.message?.content;

      if (!reply) {
        throw new Error('No reply received from AI');
      }

      setMessages((m) => [
        ...m.filter((msg) => !msg.isTyping),
        {
          from: 'bot',
          text: reply,
        },
      ]);
    } catch (err) {
      console.error('CHATBOT ERROR:', err);

      setMessages((m) => [
        ...m.filter((msg) => !msg.isTyping),
        {
          from: 'bot',
          text:
            err.message ||
            'Unable to connect to AI assistant.',
        },
      ]);
    } finally {
      setLoading(false);
    }
  }

  function handleKey(e) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      send();
    }
  }

  return (
    <>
      <style>{`
        @keyframes typingBounce {
          0%, 100% {
            transform: translateY(0);
            opacity: .4;
          }

          50% {
            transform: translateY(-4px);
            opacity: 1;
          }
        }

        @keyframes chatSlideUp {
          from {
            opacity: 0;
            transform: translateY(12px) scale(.97);
          }

          to {
            opacity: 1;
            transform: translateY(0) scale(1);
          }
        }
      `}</style>

      {/* Floating Button */}
      <button
        onClick={() => setOpen((v) => !v)}
        style={{
          position: 'fixed',
          bottom: 28,
          right: 28,
          zIndex: 9000,
          width: 52,
          height: 52,
          borderRadius: '50%',
          background: 'var(--ac)',
          color: '#fff',
          border: 'none',
          cursor: 'pointer',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          boxShadow: '0 4px 12px rgba(0,0,0,0.2)',
        }}
      >
        {open ? '✕' : <BotIcon size={20} />}
      </button>

      {/* Chat Window */}
      {open && (
        <div
          style={{
            position: 'fixed',
            bottom: 90,
            right: 28,
            zIndex: 9001,
            width: 340,
            maxHeight: 500,
            background: '#fff',
            border: '1px solid #ddd',
            borderRadius: 18,
            boxShadow: '0 8px 24px rgba(0,0,0,0.15)',
            display: 'flex',
            flexDirection: 'column',
            overflow: 'hidden',
            animation: 'chatSlideUp .22s ease',
          }}
        >
          {/* Header */}
          <div
            style={{
              padding: '12px 16px',
              background: '#2563eb',
              color: '#fff',
              display: 'flex',
              alignItems: 'center',
              gap: 10,
            }}
          >
            <BotIcon size={18} />

            <div>
              <div
                style={{
                  fontSize: 13,
                  fontWeight: 700,
                }}
              >
                MarutiXchange Assistant
              </div>

              <div
                style={{
                  fontSize: 11,
                  opacity: 0.8,
                }}
              >
                Typically replies instantly
              </div>
            </div>
          </div>

          {/* Messages */}
          <div
            style={{
              flex: 1,
              overflowY: 'auto',
              padding: 14,
              display: 'flex',
              flexDirection: 'column',
              gap: 10,
            }}
          >
            {messages.map((m, i) => (
              <div
                key={i}
                style={{
                  display: 'flex',
                  justifyContent:
                    m.from === 'me'
                      ? 'flex-end'
                      : 'flex-start',
                }}
              >
                <div
                  style={{
                    maxWidth: '80%',
                    padding: '10px 14px',
                    borderRadius:
                      m.from === 'me'
                        ? '16px 16px 4px 16px'
                        : '16px 16px 16px 4px',
                    background:
                      m.from === 'me'
                        ? '#2563eb'
                        : '#f1f5f9',
                    color:
                      m.from === 'me'
                        ? '#fff'
                        : '#111827',
                    fontSize: 13,
                    lineHeight: 1.5,
                  }}
                >
                  {m.isTyping ? <TypingDots /> : m.text}
                </div>
              </div>
            ))}

            <div ref={messagesEndRef} />
          </div>

          {/* Input */}
          <div
            style={{
              padding: 12,
              borderTop: '1px solid #eee',
              display: 'flex',
              gap: 8,
            }}
          >
            <input
              type="text"
              placeholder="Ask about any Maruti model..."
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKey}
              style={{
                flex: 1,
                padding: '10px 12px',
                border: '1px solid #ccc',
                borderRadius: 10,
                fontSize: 13,
                outline: 'none',
              }}
            />

            <button
              onClick={send}
              disabled={loading || !input.trim()}
              style={{
                padding: '10px 14px',
                border: 'none',
                borderRadius: 10,
                background:
                  loading || !input.trim()
                    ? '#cbd5e1'
                    : '#2563eb',
                color: '#fff',
                cursor:
                  loading || !input.trim()
                    ? 'not-allowed'
                    : 'pointer',
              }}
            >
              Send
            </button>
          </div>
        </div>
      )}
    </>
  );
}