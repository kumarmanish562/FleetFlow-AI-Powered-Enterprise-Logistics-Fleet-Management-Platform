import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { EmptyState } from './States.jsx';

export default function ShipmentTrendChart({ chart }) {
  const points = chart?.points || [];
  if (!points.length) return <EmptyState label="No shipment trend data for this period"/>;
  return <div className="chart-wrap"><ResponsiveContainer width="100%" height="100%"><LineChart data={points} margin={{top:12,right:12,left:-18,bottom:0}}><CartesianGrid stroke="#e8edf3" strokeDasharray="3 5" vertical={false}/><XAxis dataKey="label" axisLine={false} tickLine={false} tick={{fill:'#8a95a7',fontSize:11}}/><YAxis axisLine={false} tickLine={false} tick={{fill:'#8a95a7',fontSize:11}}/><Tooltip contentStyle={{borderRadius:10,border:'1px solid #e3e9f1',fontSize:12}}/><Line type="monotone" dataKey="value" name={chart.key || 'Shipments'} stroke="#3978e8" strokeWidth={2.5} dot={{r:3,fill:'#3978e8',strokeWidth:0}} activeDot={{r:5}}/></LineChart></ResponsiveContainer></div>;
}
