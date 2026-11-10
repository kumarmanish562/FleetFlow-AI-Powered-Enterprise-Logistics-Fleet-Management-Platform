import { useCallback, useEffect, useState } from 'react';
import { api, apiMessage } from '../api/client.js';
import { PageHeader } from '../components/PageHeader.jsx';

function formatInline(text) {
  return text.split(/(\*\*[^*]+\*\*|`[^`]+`)/g).map((part, index) => {
    if (part.startsWith('**') && part.endsWith('**')) return <strong key={index}>{part.slice(2, -2)}</strong>;
    if (part.startsWith('`') && part.endsWith('`')) return <code key={index}>{part.slice(1, -1)}</code>;
    return part;
  });
}

function MarkdownAnswer({ text }) {
  return <div className="assistant-answer">{text.split('\n').map((line, index) => {
    const heading = line.match(/^#{1,3}\s+(.+)$/);
    const list = line.match(/^\s*(?:[-*]|\d+\.)\s+(.+)$/);
    if (heading) return <strong className="assistant-answer-heading" key={index}>{formatInline(heading[1])}</strong>;
    if (list) return <div className="assistant-answer-list-item" key={index}><span>•</span>{formatInline(list[1])}</div>;
    return line ? <div key={index}>{formatInline(line)}</div> : <br key={index} />;
  })}</div>;
}

export function AssistantPage() {
  const [question, setQuestion] = useState('');
  const [conversationId, setConversationId] = useState(null);
  const [conversations, setConversations] = useState([]);
  const [messages, setMessages] = useState([]);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [copiedMessageId, setCopiedMessageId] = useState(null);

  const refreshConversations = useCallback(async () => {
    try { setConversations((await api.get('/ai/conversations')).data); } catch { /* Chat remains usable if history is temporarily unavailable. */ }
  }, []);
  useEffect(() => { refreshConversations(); }, [refreshConversations]);

  async function openConversation(id) {
    setError(''); setBusy(true);
    try {
      const { data } = await api.get(`/ai/conversations/${id}`);
      setConversationId(id);
      const loaded = await Promise.all((data.messages || []).map(async m => {
        if (m.role !== 'ASSISTANT') return { id: m.id, role: 'assistant', text: m.content, sources: [] };
        try { return { id: m.id, role: 'assistant', text: m.content, sources: (await api.get(`/ai/messages/${m.id}/sources`)).data.sources || [] }; }
        catch { return { id: m.id, role: 'assistant', text: m.content, sources: [] }; }
      }));
      setMessages(loaded);
    } catch (cause) { setError(apiMessage(cause)); }
    finally { setBusy(false); }
  }

  async function removeConversation(id) {
    try {
      await api.delete(`/ai/conversations/${id}`);
      if (conversationId === id) { setConversationId(null); setMessages([]); }
      refreshConversations();
    } catch (cause) { setError(apiMessage(cause)); }
  }

  async function send(event) {
    event.preventDefault();
    const text = question.trim();
    if (!text || busy) return;
    setQuestion(''); setError(''); setBusy(true);
    setMessages(current => [...current, { role: 'user', text }]);
    try {
      const { data } = await api.post('/ai/chat', { message: text, conversationId }, { timeout: 90_000 });
      setConversationId(data.conversationId);
      setMessages(current => [...current, { id: data.messageId, role: 'assistant', text: data.answer, status: data.status, sources: data.sources || [] }]);
      refreshConversations();
    } catch (cause) { setError(apiMessage(cause)); }
    finally { setBusy(false); }
  }

  async function rate(messageId, type) {
    try { await api.post(`/ai/messages/${messageId}/feedback`, { type }); }
    catch (cause) { setError(apiMessage(cause)); }
  }

  async function openSource(documentId, pageNumber) {
    const preview = window.open('about:blank', '_blank');
    if (preview) preview.opener = null;
    try {
      const response = await api.get(`/ai/sources/${documentId}`, { responseType: 'blob' });
      const blobUrl = URL.createObjectURL(response.data);
      const url = `${blobUrl}${pageNumber ? `#page=${pageNumber}` : ''}`;
      if (preview) preview.location.href = url;
      else window.location.href = url;
      window.setTimeout(() => URL.revokeObjectURL(blobUrl), 60_000);
    } catch (cause) { preview?.close(); setError(apiMessage(cause)); }
  }

  async function copyAnswer(message) {
    try {
      await navigator.clipboard.writeText(message.text);
      setCopiedMessageId(message.id);
      window.setTimeout(() => setCopiedMessageId(null), 1600);
    } catch (cause) { setError('Could not copy the answer.'); }
  }

  return <div className="page-wrap assistant-page"><PageHeader eyebrow="AI ASSISTANT" title="Knowledge assistant" description="Ask questions about authorized company documents. Answers show their sources." action={<button className="button" onClick={() => { setConversationId(null); setMessages([]); setError(''); }}>New conversation</button>} />
    <div className="assistant-layout">
      <aside className="panel assistant-history"><strong>Recent conversations</strong>{conversations.length ? conversations.map(item => <div className={`assistant-history-item ${item.id === conversationId ? 'selected' : ''}`} key={item.id}><button onClick={() => openConversation(item.id)} title={item.title}>{item.title || 'Untitled conversation'}</button><button aria-label="Delete conversation" title="Delete conversation" onClick={() => removeConversation(item.id)}>×</button></div>) : <small>No saved conversations yet.</small>}</aside>
        <section className="panel assistant-panel"><div className="assistant-messages" aria-live="polite">{!messages.length && <div className="assistant-empty"><strong>Ask a question about a policy or manual</strong><span>For example: What should a driver do after a vehicle breakdown?</span><div className="assistant-suggestions">{['What is our vehicle inspection policy?', 'What is the delivery failure procedure?', 'What documents are required for insurance?'].map(q => <button className="button" key={q} onClick={() => setQuestion(q)}>{q}</button>)}</div></div>}{messages.map((message, index) => <article key={message.id || `${message.role}-${index}`} className={`assistant-message ${message.role}`}><div className="assistant-message-label">{message.role === 'user' ? 'You' : 'FleetFlow AI'}</div>{message.role === 'assistant' ? <MarkdownAnswer text={message.text} /> : <p>{message.text}</p>}{message.sources?.length > 0 && <div className="assistant-sources"><strong>Sources</strong>{message.sources.map((source, i) => <div key={`${source.documentId}-${source.chunkId || i}`}><button className="assistant-source-link" onClick={() => openSource(source.documentId, source.pageNumber)}>[{i + 1}] {source.filename} · v{source.version}{source.pageNumber ? ` · page ${source.pageNumber}` : ''}</button><small>{source.excerpt}</small></div>)}</div>}{message.role === 'assistant' && message.id && <div className="assistant-feedback"><button className="assistant-copy" onClick={() => copyAnswer(message)}>{copiedMessageId === message.id ? 'Copied' : 'Copy answer'}</button><span>Was this helpful?</span><button onClick={() => rate(message.id, 'POSITIVE')} aria-label="Helpful">👍</button><button onClick={() => rate(message.id, 'NEGATIVE')} aria-label="Not helpful">👎</button></div>}</article>)}{busy && <p className="assistant-busy">Searching authorized knowledge…</p>}</div>
        <form className="assistant-form" onSubmit={send}><label htmlFor="assistant-question">Your question</label><div><textarea id="assistant-question" value={question} onChange={event => setQuestion(event.target.value)} maxLength={2000} placeholder="Ask about a company document…" rows={3} /><button className="button button-primary" disabled={busy || !question.trim()} type="submit">Send</button></div>{error && <p role="alert" className="knowledge-error">{error}</p>}</form>
      </section>
    </div>
  </div>;
}
