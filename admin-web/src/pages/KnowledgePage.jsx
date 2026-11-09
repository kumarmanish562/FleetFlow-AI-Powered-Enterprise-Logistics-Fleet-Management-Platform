import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { api, apiMessage } from '../api/client.js';
import { PageHeader } from '../components/PageHeader.jsx';
import { EmptyState, ErrorState, LoadingState } from '../components/States.jsx';

const types = ['DRIVER_HANDBOOK','FLEET_POLICY','MAINTENANCE_MANUAL','SAFETY_GUIDELINES','DELIVERY_SOP','INSURANCE_POLICY','FINANCE_POLICY','GENERAL'];
const roles = ['SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER','DRIVER','CUSTOMER','FINANCE_MANAGER'];

export function KnowledgePage() {
  const client = useQueryClient();
  const [file, setFile] = useState(null);
  const [type, setType] = useState('DRIVER_HANDBOOK');
  const [version, setVersion] = useState('1.0');
  const [allowed, setAllowed] = useState(['ADMIN','DRIVER']);
  const [replacesId, setReplacesId] = useState('');
  const [editing, setEditing] = useState(null);
  const [page, setPage] = useState(0);
  const list = useQuery({ queryKey:['knowledge-documents',page], queryFn:() => api.get('/ai/knowledge/documents', {params:{page,size:20}}).then(r => r.data), refetchInterval:5000, retry:false });
  const refresh = () => client.invalidateQueries({ queryKey:['knowledge-documents'] });
  const upload = useMutation({
    mutationFn:() => { const body = new FormData(); body.append('file', file); body.append('documentType', type); body.append('version', version); allowed.forEach(role => body.append('accessRoles', role)); if(replacesId) body.append('replacesDocumentId', replacesId); return api.post('/ai/knowledge/documents', body); },
    onSuccess:() => { toast.success('Document queued for indexing'); setFile(null); setReplacesId(''); refresh(); },
    onError:(error) => toast.error(apiMessage(error)),
  });
  const action = useMutation({
    mutationFn:({id, method, path, body}) => api({ method, url:`/ai/knowledge/documents/${id}${path}`, data:body }),
    onSuccess:() => { toast.success('Document updated'); setEditing(null); refresh(); },
    onError:(error) => toast.error(apiMessage(error)),
  });
  const toggle = (role) => setAllowed(current => current.includes(role) ? current.filter(item => item !== role) : [...current,role]);
  const rows = list.data?.content || [];

  return <div className="page-wrap knowledge-page">
    <PageHeader eyebrow="AI KNOWLEDGE" title="Company knowledge" description="Upload policies and manuals for authorized, cited answers." />
    <section className="panel knowledge-upload"><div className="panel-heading"><div><h2>Upload document</h2><p>PDF, DOCX, or UTF-8 TXT up to 20 MB</p></div></div>
      <form onSubmit={event => { event.preventDefault(); if(file && allowed.length) upload.mutate(); }}>
        <div className="knowledge-fields"><label>File<input aria-label="Knowledge document file" type="file" accept=".pdf,.docx,.txt" required onChange={event => setFile(event.target.files?.[0] || null)} /></label>
          <label>Type<select value={type} onChange={event => setType(event.target.value)}>{types.map(item => <option key={item}>{item}</option>)}</select></label>
          <label>Version<input value={version} maxLength={50} required onChange={event => setVersion(event.target.value)} /></label>
          <label>Replace indexed document<select value={replacesId} onChange={event => setReplacesId(event.target.value)}><option value="">New document</option>{rows.filter(row => row.status === 'INDEXED' && row.documentType === type).map(row => <option key={row.id} value={row.id}>{row.filename} · {row.version}</option>)}</select></label></div>
        <fieldset><legend>Who may use this document in AI answers?</legend><div className="knowledge-role-list">{roles.map(role => <label key={role}><input type="checkbox" checked={allowed.includes(role)} onChange={() => toggle(role)} />{role.replaceAll('_',' ')}</label>)}</div></fieldset>
        <button className="button button-primary" type="submit" disabled={!file || !allowed.length || upload.isPending}>{upload.isPending ? 'Uploading…' : 'Upload and index'}</button>
      </form>
    </section>
    <section className="panel knowledge-list"><div className="panel-heading"><div><h2>Documents</h2><p>Processing status refreshes automatically.</p></div></div>
      {list.isLoading ? <LoadingState label="Loading documents" /> : list.isError ? <ErrorState error={list.error} retry={() => list.refetch()} /> : !rows.length ? <EmptyState label="No documents uploaded. Upload a policy or manual to start the knowledge index." /> : <div className="knowledge-table-wrap"><table><thead><tr><th>Document</th><th>Type / version</th><th>Status</th><th>Access</th><th>Chunks</th><th>Actions</th></tr></thead><tbody>{rows.map(row => <tr key={row.id}><td><strong>{row.filename}</strong>{row.errorMessage && <small className="knowledge-error">{row.errorMessage}</small>}</td><td>{row.documentType.replaceAll('_',' ')}<small>v{row.version}</small></td><td><span className={`knowledge-status status-${row.status.toLowerCase()}`}>{row.status}</span></td><td>{row.accessRoles?.join(', ')}</td><td>{row.chunkCount}</td><td><div className="knowledge-actions"><button className="button button-small" onClick={() => setEditing(row)}>Access</button>{row.status === 'FAILED' && <button className="button button-small" disabled={action.isPending} onClick={() => action.mutate({id:row.id,method:'post',path:'/retry'})}>Retry</button>}{row.status === 'INDEXED' && <button className="button button-small" disabled={action.isPending} onClick={() => action.mutate({id:row.id,method:'post',path:'/reindex'})}>Reindex</button>}<button className="button button-small button-danger" disabled={action.isPending || row.status === 'DELETING'} onClick={() => { if(window.confirm(`Delete ${row.filename}?`)) action.mutate({id:row.id,method:'delete',path:''}); }}>Delete</button></div></td></tr>)}</tbody></table></div>}
      {list.data?.totalPages > 1 && <div className="knowledge-pagination"><button className="button button-small" disabled={page === 0} onClick={() => setPage(value => value - 1)}>Previous</button><span>Page {page + 1} of {list.data.totalPages}</span><button className="button button-small" disabled={page + 1 >= list.data.totalPages} onClick={() => setPage(value => value + 1)}>Next</button></div>}
    </section>
    {editing && <AccessEditor key={editing.id} document={editing} busy={action.isPending} onClose={() => setEditing(null)} onSave={(accessRoles) => action.mutate({id:editing.id,method:'put',path:'/access',body:{accessRoles}})} />}
  </div>;
}

function AccessEditor({document, busy, onClose, onSave}) {
  const [selected, setSelected] = useState(document.accessRoles || []);
  return <div className="knowledge-dialog-backdrop" role="presentation" onMouseDown={onClose}><div className="knowledge-dialog" role="dialog" aria-modal="true" aria-label="Edit document access" onMouseDown={event => event.stopPropagation()}><h2>Access to {document.filename}</h2><p>Only selected roles can retrieve this document.</p><div className="knowledge-role-list">{roles.map(role => <label key={role}><input type="checkbox" checked={selected.includes(role)} onChange={() => setSelected(current => current.includes(role) ? current.filter(item => item !== role) : [...current,role])}/>{role.replaceAll('_',' ')}</label>)}</div><div className="knowledge-dialog-actions"><button className="button" onClick={onClose}>Cancel</button><button className="button button-primary" disabled={!selected.length || busy} onClick={() => onSave(selected)}>Save access</button></div></div></div>;
}
