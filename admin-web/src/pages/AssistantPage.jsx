import { useState } from 'react';
import { api, apiMessage } from '../api/client.js';
import { PageHeader } from '../components/PageHeader.jsx';

export function AssistantPage() {
  const [question, setQuestion] = useState('');
  const [conversationId, setConversationId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  async function send(event) {
    event.preventDefault();
    const text = question.trim();
    if (!text || busy) return;
    setQuestion(''); setError(''); setBusy(true);
    setMessages(current => [...current, {role:'user', text}]);
    try {
      const {data} = await api.post('/ai/chat', {message:text, conversationId}, {timeout:90_000});
      setConversationId(data.conversationId);
      setMessages(current => [...current, {role:'assistant', text:data.answer, sources:data.sources || []}]);
    } catch (cause) { setError(apiMessage(cause)); }
    finally { setBusy(false); }
  }

  return <div className="page-wrap assistant-page"><PageHeader eyebrow="AI ASSISTANT" title="Knowledge assistant" description="Ask questions about authorized company documents. Answers show their sources." action={<button className="button" onClick={() => {setConversationId(null);setMessages([]);setError('');}}>New conversation</button>} />
    <section className="panel assistant-panel"><div className="assistant-messages" aria-live="polite">{!messages.length && <div className="assistant-empty"><strong>Ask a question about a policy or manual</strong><span>For example: What should a driver do after a vehicle breakdown?</span></div>}{messages.map((message,index) => <article key={index} className={`assistant-message ${message.role}`}><div className="assistant-message-label">{message.role === 'user' ? 'You' : 'FleetFlow AI'}</div><p>{message.text}</p>{message.sources?.length > 0 && <div className="assistant-sources"><strong>Sources</strong>{message.sources.map((source,i) => <div key={`${source.documentId}-${i}`}><span>[{i+1}] {source.filename} · v{source.version}{source.pageNumber ? ` · page ${source.pageNumber}` : ''}</span><small>{source.excerpt}</small></div>)}</div>}</article>)}{busy && <p className="assistant-busy">Searching authorized knowledge…</p>}</div>
      <form className="assistant-form" onSubmit={send}><label htmlFor="assistant-question">Your question</label><div><textarea id="assistant-question" value={question} onChange={event => setQuestion(event.target.value)} maxLength={2000} placeholder="Ask about a company document…" rows={3}/><button className="button button-primary" disabled={busy || !question.trim()} type="submit">Send</button></div>{error && <p role="alert" className="knowledge-error">{error}</p>}</form>
    </section>
  </div>;
}
