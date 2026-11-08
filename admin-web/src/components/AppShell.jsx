import { useState } from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { Activity, Bell, Boxes, ChartNoAxesCombined, ChevronDown, CircleDollarSign, ClipboardList, LayoutDashboard, Map, Menu, Route as RouteIcon, Settings, Truck, Users, Wrench, X, LogOut } from 'lucide-react';
import { useAuth } from '../auth/AuthProvider.jsx';
import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client.js';

const items = [
  { to: '/', label: 'Overview', icon: LayoutDashboard, roles: ['SUPER_ADMIN','ADMIN'] },
  { to: '/fleet', label: 'Fleet', icon: Truck, roles: ['SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER','DRIVER'] },
  { to: '/drivers', label: 'Drivers', icon: Users, roles: ['SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER'] },
  { to: '/customers', label: 'Customers', icon: Users, roles: ['SUPER_ADMIN','ADMIN'] },
  { to: '/shipments', label: 'Shipments', icon: Boxes, roles: ['SUPER_ADMIN','ADMIN','DISPATCHER'] },
  { to: '/routes', label: 'Routes', icon: RouteIcon, roles: ['SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER'] },
  { to: '/tracking', label: 'Live tracking', icon: Map, roles: ['SUPER_ADMIN','ADMIN','DISPATCHER','FLEET_MANAGER'] },
  { to: '/maintenance', label: 'Maintenance', icon: Wrench, roles: ['SUPER_ADMIN','ADMIN','FLEET_MANAGER'] },
  { to: '/billing', label: 'Billing', icon: CircleDollarSign, roles: ['SUPER_ADMIN','ADMIN','BILLING','FINANCE_MANAGER'] },
  { to: '/notifications', label: 'Notifications', icon: Bell },
  { to: '/analytics', label: 'Analytics', icon: ChartNoAxesCombined, roles: ['SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER','FINANCE_MANAGER'] },
  { to: '/users', label: 'User management', icon: Users, roles: ['SUPER_ADMIN','ADMIN'] },
];

export function AppShell() {
  const { user, logout } = useAuth();
  const [open, setOpen] = useState(false);
  const location = useLocation();
  const unread = useQuery({ queryKey: ['notifications','unread-count'], queryFn: () => api.get('/notifications/unread-count').then((r) => r.data.unreadCount), refetchInterval: 60_000, retry: false });
  const visible = items.filter((item) => !item.roles || item.roles.some((role) => user?.roles?.includes(role)));
  const title = items.find((item) => item.to === location.pathname)?.label || 'FleetFlow';
  const fullName = [user?.firstName, user?.lastName].filter(Boolean).join(' ') || user?.email || 'Account';
  return <div className="app-shell">
    {open && <button aria-label="Close navigation" className="mobile-scrim" onClick={() => setOpen(false)} />}
    <aside className={`sidebar ${open ? 'sidebar-open' : ''}`}>
      <div className="brand"><span className="brand-mark"><Truck size={19} /></span><span>fleetflow<span className="brand-dot">.</span><small>OPERATIONS PLATFORM</small></span><button className="icon-button mobile-close" aria-label="Close navigation" onClick={() => setOpen(false)}><X size={18}/></button></div>
      <div className="workspace"><span className="workspace-indicator"/> Northstar Logistics <ChevronDown size={14}/></div>
      <div className="nav-caption">WORKSPACE</div>
      <nav aria-label="Main navigation">{visible.map(({to,label,icon:Icon}) => <NavLink end={to === '/'} key={to} to={to} onClick={() => setOpen(false)} className={({isActive}) => `nav-link ${isActive ? 'active' : ''}`}><Icon size={18}/><span>{label}</span>{to === '/notifications' && unread.data > 0 && <span className="nav-count">{unread.data}</span>}</NavLink>)}</nav>
      <div className="sidebar-bottom"><div className="nav-caption">PREFERENCES</div><NavLink to="/settings" className={({isActive}) => `nav-link ${isActive ? 'active' : ''}`}><Settings size={18}/><span>Settings</span></NavLink><div className="sidebar-foot"><span className="online-dot"/>All systems operational</div></div>
    </aside>
    <main className="main-area"><header className="topbar"><button className="icon-button mobile-menu" aria-label="Open navigation" onClick={() => setOpen(true)}><Menu size={20}/></button><div className="crumb"><span>Workspace</span><span className="crumb-sep">/</span><strong>{title}</strong></div><div className="topbar-actions"><NavLink to="/notifications" aria-label="Notifications" className="icon-button notification-button"><Bell size={19}/>{unread.data > 0 && <i/>}</NavLink><div className="profile"><div className="avatar">{fullName.charAt(0).toUpperCase()}</div><div className="profile-copy"><strong>{fullName}</strong><span>{user?.roles?.[0]?.replaceAll('_',' ') || 'Member'}</span></div><button aria-label="Sign out" title="Sign out" className="icon-button signout" onClick={logout}><LogOut size={17}/></button></div></div></header><section className="page-wrap"><Outlet /></section><footer className="app-footer"><span>FleetFlow Operations</span><span>Connected to your FleetFlow backend</span></footer></main>
  </div>;
}
