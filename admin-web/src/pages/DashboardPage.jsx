import { lazy, Suspense } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Activity, ArrowUpRight, Box, CircleDollarSign, Truck, Users, Wrench } from 'lucide-react';
import { analyticsApi, api } from '../api/client.js';
import { PageHeader } from '../components/PageHeader.jsx';
import { ErrorState, LoadingState } from '../components/States.jsx';
import { useAuth } from '../auth/AuthProvider.jsx';

const money = (items) => Array.isArray(items) ? items.map(({currency, amount}) => `${currency} ${Number(amount).toLocaleString(undefined,{maximumFractionDigits:0})}`).join(' · ') || '—' : '—';
const ShipmentTrendChart = lazy(() => import('../components/ShipmentTrendChart.jsx'));

export function DashboardPage() {
  const { user } = useAuth();
  const query = useQuery({ queryKey: ['analytics','dashboard'], queryFn: analyticsApi.dashboard });
  const charts = useQuery({ queryKey: ['analytics','shipment-trends'], queryFn: () => api.get('/analytics/shipments/trends').then((r) => r.data), enabled: Boolean(query.data) });
  const data = query.data;
  return <>
    <PageHeader eyebrow="FLEET OPERATIONS / OVERVIEW" title={`Good day, ${user?.firstName || 'there'}`} description="A live view of your fleet performance and operations." />
    <div className="dashboard-meta"><span><i className="live-dot"/> Live operational data</span><span>Updated {query.dataUpdatedAt ? new Date(query.dataUpdatedAt).toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'}) : '—'}</span></div>
    {query.isLoading && <LoadingState label="Loading dashboard metrics"/>}
    {query.isError && <ErrorState error={query.error} retry={() => query.refetch()}/>}
    {data && <>
      <section className="kpi-grid" aria-label="Fleet metrics">
        <Kpi icon={Truck} label="Total vehicles" value={data.fleet?.total} detail={`${data.fleet?.active ?? 0} active · ${data.fleet?.available ?? 0} available`} tone="blue"/>
        <Kpi icon={Box} label="Active shipments" value={data.shipments?.active} detail={`${data.shipments?.delivered ?? 0} delivered · ${data.shipments?.delayed ?? 0} delayed`} tone="violet"/>
        <Kpi icon={Users} label="Available drivers" value={data.drivers?.available} detail={`${data.drivers?.total ?? 0} registered drivers`} tone="green"/>
        <Kpi icon={CircleDollarSign} label="Collected revenue" value={money(data.financial?.collected)} detail={`${data.financial?.successfulPaymentCount ?? 0} successful payments`} tone="amber"/>
      </section>
      <section className="dashboard-grid">
        <article className="panel chart-panel"><div className="panel-heading"><div><h2>Shipment activity</h2><p>Trend returned by analytics</p></div><span className="panel-tag"><Activity size={14}/> PHASE 14</span></div>{charts.isLoading ? <LoadingState label="Loading shipment trend"/> : charts.isError ? <ErrorState error={charts.error} retry={() => charts.refetch()}/> : <Suspense fallback={<LoadingState label="Loading chart"/>}><ShipmentTrendChart chart={charts.data}/></Suspense>}</article>
        <article className="panel"><div className="panel-heading"><div><h2>Operations snapshot</h2><p>Current fleet health</p></div><ArrowUpRight size={17} className="muted-icon"/></div><Metric label="Fleet assignment" value={`${data.fleet?.assignmentCoveragePercent ?? 0}%`}/><Metric label="Shipment completion" value={`${data.shipments?.completionPercent ?? 0}%`}/><Metric label="On-time deliveries" value={`${data.delivery?.onTimePercent ?? 0}%`}/><Metric label="Vehicles in maintenance" value={data.fleet?.maintenance ?? 0}/></article>
        <article className="panel"><div className="panel-heading"><div><h2>Delivery performance</h2><p>Reported by delivery analytics</p></div><Activity size={17} className="muted-icon"/></div><div className="delivery-summary"><strong>{data.delivery?.successPercent ?? 0}<small>%</small></strong><span>successful delivery rate</span></div><div className="split-stats"><div><b>{data.delivery?.onTime ?? 0}</b><span>On time</span></div><div><b>{data.delivery?.delayed ?? 0}</b><span>Delayed</span></div><div><b>{data.delivery?.failed ?? 0}</b><span>Failed</span></div></div></article>
        <article className="panel"><div className="panel-heading"><div><h2>Maintenance</h2><p>Service workload</p></div><Wrench size={17} className="muted-icon"/></div><div className="maintenance-count"><strong>{data.maintenance?.vehiclesUnderMaintenance ?? 0}</strong><span>vehicles currently under maintenance</span></div><Metric label="Scheduled records" value={data.maintenance?.scheduled ?? 0}/><Metric label="In progress" value={data.maintenance?.inProgress ?? 0}/><Metric label="Completed records" value={data.maintenance?.completed ?? 0}/></article>
      </section>
      <div className="data-note">Figures are supplied by FleetFlow Analytics. Access and results depend on your account permissions and configured data range.</div>
    </>}
  </>;
}

function Kpi({icon:Icon,label,value,detail,tone}) { return <article className="kpi-card"><div className="kpi-top"><span className={`kpi-icon ${tone}`}><Icon size={18}/></span><span className="kpi-context">SUMMARY</span></div><div className="kpi-value">{value ?? '—'}</div><div className="kpi-label">{label}</div><div className="kpi-detail">{detail}</div></article>; }
function Metric({label,value}) { return <div className="metric-row"><span>{label}</span><strong>{value ?? '—'}</strong></div>; }
