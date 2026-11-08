import { AlertCircle, LoaderCircle, SearchX } from 'lucide-react';
import { apiMessage } from '../api/client.js';

export function LoadingState({ label = 'Loading data' }) { return <div className="state-card"><LoaderCircle className="spin" size={20}/><span>{label}…</span></div>; }
export function ErrorState({ error, retry }) { return <div className="state-card state-error"><AlertCircle size={20}/><span>{apiMessage(error)}</span>{retry && <button className="button button-small" onClick={retry}>Retry</button>}</div>; }
export function EmptyState({ label = 'No records found' }) { return <div className="state-card"><SearchX size={20}/><span>{label}</span></div>; }
