import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Bell, FileQuestion, MapPin, Search, Wrench } from 'lucide-react';
import { useSearchParams } from 'react-router-dom';
import { api, analyticsApi, resourceApi } from '../api/client.js';
import { useAuth } from '../auth/AuthProvider.jsx';
import { PageHeader } from '../components/PageHeader.jsx';
import { EmptyState, ErrorState, LoadingState } from '../components/States.jsx';

const config = {
  vehicles: { title:'Fleet management', description:'Vehicles registered in the FleetFlow platform.', endpoint:'vehicles', query:resourceApi.vehicles, columns:['registrationNumber','manufacturer','model','status','fuelType'], roles:['SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER','DRIVER'] },
  drivers: { title:'Driver management', description:'Driver records and availability from the operations API.', endpoint:'drivers', query:resourceApi.drivers, columns:['firstName','lastName','email','employmentStatus','availability'], roles:['SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER'] },
  customers: { title:'Customers', description:'Customer accounts managed by authorized administrators.', endpoint:'customers', query:resourceApi.customers, columns:['name','email','phone','status'], roles:['SUPER_ADMIN','ADMIN'] },
  shipments: { title:'Shipments', description:'Shipment lifecycle and assignment information.', endpoint:'shipments', query:resourceApi.shipments, columns:['shipmentNumber','status','priority','originAddress','destinationAddress','expectedDeliveryDate'], roles:['SUPER_ADMIN','ADMIN','DISPATCHER'] },
  routes: { title:'Routes', description:'Route records currently available to your account.', endpoint:'routes', query:resourceApi.routes, columns:['routeNumber','status','originAddress','destinationAddress','distanceKm'], roles:['SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER'] },
};

const titles = { tracking:'Live tracking', maintenance:'Maintenance', billing:'Billing & payments', notifications:'Notifications', analytics:'Analytics & reports', users:'User management', settings:'Settings' };

export function ResourcePage({ kind }) {
  if (config[kind]) return <DataResource kind={kind}/>;
  if (kind === 'notifications') return <NotificationsPage/>;
  if (kind === 'tracking') return <TrackingPage/>;
  if (kind === 'maintenance') return <MaintenancePage/>;
  if (kind === 'analytics') return <AnalyticsPage/>;
  if (kind === 'settings') return <SettingsPage/>;
  if (kind === 'billing') return <DependencyPage title={titles[kind]} text="The backend supports invoice creation and lookup by ID, payment creation, and payment lookup by ID. It does not expose an invoice or payment list endpoint, so this page cannot show a complete billing register yet." endpoint="GET /api/v1/invoices/{invoiceId} · POST /api/v1/invoices · GET /api/v1/payments/{paymentId} · POST /api/v1/payments"/>;
  return <DependencyPage title={titles[kind]} text="The backend has no user administration endpoints for listing users, managing roles, or deactivating accounts. Authentication only exposes the current account." endpoint="GET /api/v1/auth/me"/>;
}

function DataResource({ kind }) {
  const item = config[kind];
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [params, setParams] = useSearchParams();
  const query = useQuery({ queryKey:[item.endpoint, page, params.get('status')], queryFn:() => item.query({page,size:20, ...(params.get('status') ? {status:params.get('status')} : {})}) });
  const content = Array.isArray(query.data) ? query.data : query.data?.content || [];
  const keys = useMemo(() => item.columns.filter((key) => content.some((row) => key in row)), [content,item.columns]);
  const filtered = content.filter((row) => !search || JSON.stringify(row).toLowerCase().includes(search.toLowerCase()));
  return <><PageHeader eyebrow="OPERATIONS" title={item.title} description={item.description} action={<div className="search-box"><Search size={16}/><input aria-label="Search current page" placeholder="Search this page" value={search} onChange={(e)=>setSearch(e.target.value)}/></div>}/><div className="section-intro"><span>{query.data?.totalElements ?? content.length} records</span><span>Page {page + 1}{query.data?.totalPages ? ` of ${query.data.totalPages}` : ''}</span></div>{query.isLoading ? <LoadingState label={`Loading ${item.title.toLowerCase()}`}/> : query.isError ? <ErrorState error={query.error} retry={()=>query.refetch()}/> : filtered.length === 0 ? <EmptyState label={content.length ? 'No records match this search' : `No ${item.title.toLowerCase()} returned by the API`}/> : <div className="table-panel"><div className="table-scroll"><table><thead><tr>{(keys.length ? keys : item.columns).map((key)=><th key={key}>{humanize(key)}</th>)}</tr></thead><tbody>{filtered.map((row,index)=><tr key={row.id || row.uuid || index}>{(keys.length ? keys : item.columns).map((key)=><td key={key}>{formatCell(row[key])}</td>)}</tr>)}</tbody></table></div><div className="table-pagination"><span>{query.data?.totalElements ?? content.length} total records</span><div><button className="button button-small" disabled={page===0} onClick={()=>setPage((p)=>p-1)}>Previous</button><button className="button button-small" disabled={query.data?.last ?? content.length<20} onClick={()=>setPage((p)=>p+1)}>Next</button></div></div></div>}</>;
}

function NotificationsPage() {
  const query = useQuery({queryKey:['notifications','list'],queryFn:()=>resourceApi.notifications({page:0,size:50})});
  const page = query.data;
  const notifications = page?.content || [];
  return <><PageHeader eyebrow="PERSONAL INBOX" title="Notifications" description="Operational updates delivered to your account." action={notifications.some((n)=>!n.readAt) && <button className="button" onClick={()=>resourceApi.readAllNotifications().then(()=>query.refetch())}>Mark all read</button>}/>{query.isLoading ? <LoadingState/> : query.isError ? <ErrorState error={query.error} retry={()=>query.refetch()}/> : !notifications.length ? <EmptyState label="You’re all caught up"/> : <div className="notification-list">{notifications.map((n)=><article key={n.id} className={`notification-card ${n.readAt ? '' : 'unread'}`}><span className="notification-icon"><Bell size={17}/></span><div><div className="notification-title">{n.title || n.type || 'FleetFlow update'}</div><p>{n.message || n.body || 'Open this notification to review the update.'}</p><time>{n.createdAt ? new Date(n.createdAt).toLocaleString() : ''}</time></div>{!n.readAt && <button className="button button-small" onClick={()=>resourceApi.readNotification(n.id).then(()=>query.refetch())}>Mark read</button>}</article>)}</div>}</>;
}

function TrackingPage() {
  const [shipmentId, setShipmentId] = useState('');
  const [selected, setSelected] = useState('');
  const shipments = useQuery({queryKey:['shipments','tracking-picker'],queryFn:()=>resourceApi.shipments({page:0,size:100})});
  const latest = useQuery({queryKey:['tracking',selected],queryFn:()=>resourceApi.latestTracking(selected),enabled:Boolean(selected),refetchInterval:30_000});
  const content = shipments.data?.content || [];
  return <><PageHeader eyebrow="SHIPMENT VISIBILITY" title="Live tracking" description="Select a shipment to view its latest recorded tracking event. Refreshes every 30 seconds."/><div className="panel tracking-panel"><label htmlFor="shipment-picker">Shipment</label><div className="tracking-controls"><select id="shipment-picker" value={shipmentId} onChange={(e)=>setShipmentId(e.target.value)}><option value="">Choose a shipment</option>{content.map((s)=><option key={s.id} value={s.id}>{s.shipmentNumber || s.id} · {s.status}</option>)}</select><button className="button button-primary" disabled={!shipmentId} onClick={()=>setSelected(shipmentId)}>View location</button></div>{shipments.isLoading ? <LoadingState label="Loading shipments"/> : shipments.isError ? <ErrorState error={shipments.error} retry={()=>shipments.refetch()}/> : null}{latest.isLoading && <LoadingState label="Loading latest tracking event"/>}{latest.isError && <ErrorState error={latest.error} retry={()=>latest.refetch()}/ >}{latest.data && <div className="tracking-result"><div className="tracking-result-icon"><MapPin size={20}/></div><div><strong>{latest.data.locationName || latest.data.location || 'Latest recorded location'}</strong><span>{latest.data.eventType || latest.data.status || 'Tracking event'} · {latest.data.eventTime || latest.data.createdAt || ''}</span>{latest.data.latitude != null && <span>Coordinates: {latest.data.latitude}, {latest.data.longitude}</span>}</div></div>}<p className="inline-note">A fleet-wide live map is not available because the backend exposes tracking events by shipment ID and does not provide a location feed for all active shipments.</p></div></>;
}

function MaintenancePage() {
  const [vehicleId,setVehicleId]=useState('');
  const vehicles=useQuery({queryKey:['vehicles','maintenance-picker'],queryFn:()=>resourceApi.vehicles({page:0,size:100})});
  const history=useQuery({queryKey:['maintenance',vehicleId],queryFn:()=>resourceApi.maintenanceForVehicle(vehicleId,{page:0,size:50}),enabled:Boolean(vehicleId)});
  const queryClient=useQueryClient();
  const transition=useMutation({mutationFn:({id,action})=>action==='start'?resourceApi.maintenanceStart(id):resourceApi.maintenanceCancel(id),onSuccess:()=>queryClient.invalidateQueries({queryKey:['maintenance',vehicleId]})});
  const rows=history.data?.content||[];
  return <><PageHeader eyebrow="VEHICLE SERVICE" title="Maintenance" description="Review and advance maintenance records using backend state transitions."/><div className="panel maintenance-workspace"><label htmlFor="maintenance-vehicle">Vehicle</label><select id="maintenance-vehicle" value={vehicleId} onChange={(event)=>setVehicleId(event.target.value)}><option value="">Choose a vehicle</option>{(vehicles.data?.content||[]).map((vehicle)=><option key={vehicle.id} value={vehicle.id}>{vehicle.registrationNumber} · {vehicle.manufacturer} {vehicle.model}</option>)}</select>{vehicles.isLoading&&<LoadingState label="Loading vehicles"/>}{vehicles.isError&&<ErrorState error={vehicles.error} retry={()=>vehicles.refetch()}/ >}{vehicleId&&history.isLoading&&<LoadingState label="Loading maintenance records"/>}{vehicleId&&history.isError&&<ErrorState error={history.error} retry={()=>history.refetch()}/ >}{vehicleId&&!history.isLoading&&!history.isError&&!rows.length&&<EmptyState label="No maintenance history for this vehicle"/>}{rows.length>0&&<div className="maintenance-records">{rows.map((record)=><article key={record.id} className="maintenance-record"><div><strong>{humanize(record.maintenanceType||'Maintenance')}</strong><span>{record.description||'No description'} · {record.maintenanceDate||'Date not set'}</span><small>Status: {record.status} · Cost: {record.cost??'—'}</small></div>{record.status==='SCHEDULED'&&<button className="button button-small" disabled={transition.isPending} onClick={()=>transition.mutate({id:record.id,action:'start'})}>Start</button>}{['SCHEDULED','IN_PROGRESS'].includes(record.status)&&<button className="button button-small" disabled={transition.isPending} onClick={()=>transition.mutate({id:record.id,action:'cancel'})}>Cancel</button>}</article>)}</div>}{transition.isError&&<div className="form-error" role="alert">{apiMessage(transition.error)}</div>}<p className="inline-note">This is per-vehicle history because the backend does not currently expose a fleet-wide maintenance list. The API remains authoritative for every state transition.</p></div></>;
}

function AnalyticsPage() {
  const { user }=useAuth();
  const roles=user?.roles||[];
  const admin=roles.some((role)=>['ADMIN','SUPER_ADMIN'].includes(role));
  const finance=admin||roles.includes('FINANCE_MANAGER');
  const fleet=admin||roles.includes('FLEET_MANAGER');
  const dispatch=admin||roles.includes('DISPATCHER');
  const results=[
    useQuery({queryKey:['analytics','fleet','summary'],queryFn:analyticsApi.fleet,enabled:fleet}),
    useQuery({queryKey:['analytics','shipments','summary'],queryFn:analyticsApi.shipments,enabled:fleet||dispatch}),
    useQuery({queryKey:['analytics','drivers','summary'],queryFn:analyticsApi.drivers,enabled:fleet}),
    useQuery({queryKey:['analytics','financial','summary'],queryFn:analyticsApi.financial,enabled:finance}),
    useQuery({queryKey:['analytics','delivery','summary'],queryFn:analyticsApi.delivery,enabled:fleet||dispatch}),
  ];
  const panels=[['Fleet',results[0],fleet],['Shipments',results[1],fleet||dispatch],['Drivers',results[2],fleet],['Financial',results[3],finance],['Delivery',results[4],fleet||dispatch]].filter(([, ,enabled])=>enabled);
  const pending=panels.some(([,query])=>query.isLoading);
  return <><PageHeader eyebrow="BUSINESS INTELLIGENCE" title="Analytics & reports" description="Operational and financial metrics returned by the analytics APIs available to your role."/>{pending&&<LoadingState label="Loading analytics"/>}<div className="analytics-grid">{panels.map(([group,query])=><article className="panel analytics-card" key={group}><h2>{group}</h2>{query.isError?<ErrorState error={query.error} retry={()=>query.refetch()}/>:Object.entries(query.data||{}).map(([key,value])=><div className="metric-row" key={key}><span>{humanize(key)}</span><strong>{Array.isArray(value)?value.map((entry)=>`${entry.currency} ${entry.amount}`).join(' · '):String(value ?? '—')}</strong></div>)}</article>)}</div></>;
}

function SettingsPage() {
  const query=useQuery({queryKey:['notification-preferences'],queryFn:resourceApi.notificationPreferences});
  const queryClient=useQueryClient();
  const update=useMutation({mutationFn:resourceApi.updateNotificationPreference,onSuccess:()=>queryClient.invalidateQueries({queryKey:['notification-preferences']})});
  return <><PageHeader eyebrow="ACCOUNT PREFERENCES" title="Settings" description="Manage notification delivery preferences for your account."/>{query.isLoading?<LoadingState label="Loading preferences"/>:query.isError?<ErrorState error={query.error} retry={()=>query.refetch()}/>:!query.data?.length?<EmptyState label="No notification preferences configured"/>:<div className="panel preferences-list">{query.data.map((preference,index)=><div className="preference-row" key={preference.id||`${preference.notificationType}-${preference.channel}-${index}`}><div><strong>{humanize(preference.notificationType||'Notification')}</strong><span>{humanize(preference.channel||'Channel')}</span></div><label className="switch"><input type="checkbox" checked={preference.enabled} disabled={update.isPending} onChange={(event)=>update.mutate({notificationType:preference.notificationType,channel:preference.channel,enabled:event.target.checked})}/><span/></label></div>)}</div>}{update.isError&&<div className="form-error" role="alert">{apiMessage(update.error)}</div>}</>;
}

function DependencyPage({title,text,endpoint}) { return <><PageHeader eyebrow="BACKEND INTEGRATION" title={title} description="This page is limited to operations supported by the current API."/><div className="panel feature-limitation"><FileQuestion size={23}/><div><h2>Backend endpoint needed</h2><p>{text}</p><code>{endpoint}</code></div></div></>; }
function humanize(value) { return value.replace(/([A-Z])/g,' $1').replaceAll('_',' ').replace(/^./,(c)=>c.toUpperCase()); }
function formatCell(value) { if(value==null) return '—'; if(typeof value==='object') return value.name || value.id || JSON.stringify(value); if(typeof value==='string' && value.includes('T')) { const date=new Date(value); if(!Number.isNaN(date.getTime())) return date.toLocaleString(); } return String(value); }
