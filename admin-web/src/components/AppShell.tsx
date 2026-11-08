import { useEffect, useMemo, useRef, useState, type KeyboardEvent } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { Activity, Bell, Boxes, Bot, ChartNoAxesCombined, CircleDollarSign, LayoutDashboard, LogOut, Map, Menu, Moon, PanelLeftClose, PanelLeftOpen, Route as RouteIcon, Search, Settings, Sun, Truck, Users, Wrench, X, type LucideIcon } from 'lucide-react';
import { useAuth } from '../auth/AuthProvider.tsx';
import { useTheme } from '../context/ThemeContext.tsx';
import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client.js';

type NavItem = { to: string; label: string; icon: LucideIcon; roles?: string[] };
const items: NavItem[] = [
  { to: '/', label: 'Overview', icon: LayoutDashboard, roles: ['SUPER_ADMIN','ADMIN'] },
  { to: '/fleet', label: 'Fleet', icon: Truck, roles: ['SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER','DRIVER'] },
  { to: '/drivers', label: 'Drivers', icon: Users, roles: ['ADMIN','FLEET_MANAGER','DISPATCHER'] },
  { to: '/customers', label: 'Customers', icon: Users, roles: ['SUPER_ADMIN','ADMIN'] },
  { to: '/shipments', label: 'Shipments', icon: Boxes, roles: ['SUPER_ADMIN','ADMIN','DISPATCHER'] },
  { to: '/routes', label: 'Routes', icon: RouteIcon, roles: ['SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER'] },
  { to: '/tracking', label: 'Live tracking', icon: Map, roles: ['SUPER_ADMIN','ADMIN','DISPATCHER','FLEET_MANAGER'] },
  { to: '/maintenance', label: 'Maintenance', icon: Wrench, roles: ['SUPER_ADMIN','ADMIN','FLEET_MANAGER'] },
  { to: '/billing', label: 'Billing', icon: CircleDollarSign, roles: ['ADMIN','CUSTOMER'] },
  { to: '/notifications', label: 'Notifications', icon: Bell },
  { to: '/analytics', label: 'Analytics', icon: ChartNoAxesCombined, roles: ['SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER','FINANCE_MANAGER'] },
  { to: '/assistant', label: 'AI assistant', icon: Bot, roles: ['SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER'] },
  { to: '/users', label: 'User management', icon: Users, roles: ['SUPER_ADMIN','ADMIN'] },
];

export function AppShell() {
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [collapsed, setCollapsed] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const [pageSearch, setPageSearch] = useState('');
  const profileRef = useRef<HTMLDivElement>(null);
  const searchRef = useRef<HTMLDivElement>(null);
  const location = useLocation();
  const navigate = useNavigate();
  const unread = useQuery({ queryKey: ['notifications','unread-count'], queryFn: () => api.get('/notifications/unread-count').then((r) => r.data.unreadCount), refetchInterval: 60_000, retry: false });
  const visible = items.filter((item) => !item.roles || item.roles.some((role) => user?.roles?.includes(role)));
  const results = useMemo(() => visible.filter((item) => item.label.toLowerCase().includes(pageSearch.trim().toLowerCase())), [visible, pageSearch]);
  const title = items.find((item) => item.to === location.pathname)?.label || (location.pathname === '/settings' ? 'Settings' : 'FleetFlow');
  const fullName = [user?.firstName, user?.lastName].filter(Boolean).join(' ') || user?.email || 'Account';

  useEffect(() => {
    if (!profileOpen && !pageSearch) return;
    const close = (event: PointerEvent) => {
      const target = event.target as Node;
      if (profileOpen && profileRef.current && !profileRef.current.contains(target)) setProfileOpen(false);
      if (pageSearch && searchRef.current && !searchRef.current.contains(target)) setPageSearch('');
    };
    const escape = (event: globalThis.KeyboardEvent) => { if (event.key === 'Escape') { setProfileOpen(false); setPageSearch(''); } };
    document.addEventListener('pointerdown', close);
    document.addEventListener('keydown', escape);
    return () => { document.removeEventListener('pointerdown', close); document.removeEventListener('keydown', escape); };
  }, [profileOpen, pageSearch]);

  const openPage = (path: string) => { navigate(path); setPageSearch(''); setMobileOpen(false); };
  const onSearchKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter' && results[0]) { event.preventDefault(); openPage(results[0].to); }
    if (event.key === 'Escape') setPageSearch('');
  };

  return <div className={`app-shell ${collapsed ? 'app-shell-collapsed' : ''}`}>
    {mobileOpen && <button aria-label="Close navigation" className="mobile-scrim" onClick={() => setMobileOpen(false)} />}
    <aside className={`sidebar ${mobileOpen ? 'sidebar-open' : ''} ${collapsed ? 'sidebar-collapsed' : ''}`}>
      <div className="brand"><span className="brand-mark"><Truck size={19} /></span><span className="brand-word">fleetflow<span className="brand-dot">.</span><small>OPERATIONS PLATFORM</small></span><button className="icon-button mobile-close" aria-label="Close navigation" onClick={() => setMobileOpen(false)}><X size={18}/></button></div>
      <div className="workspace"><span className="workspace-indicator"/><span className="workspace-name">FleetFlow workspace</span></div>
      <div className="nav-caption">WORKSPACE</div>
      <nav aria-label="Main navigation">{visible.map(({to,label,icon:Icon}) => <NavLink end={to === '/'} key={to} to={to} title={collapsed?label:undefined} onClick={() => setMobileOpen(false)} className={({isActive}) => `nav-link ${isActive ? 'active' : ''}`}><Icon size={18}/><span className="nav-label">{label}</span>{to === '/notifications' && unread.data > 0 && <span className="nav-count">{unread.data}</span>}</NavLink>)}</nav>
      <div className="sidebar-bottom"><div className="nav-caption">PREFERENCES</div><NavLink to="/settings" title={collapsed?'Settings':undefined} className={({isActive}) => `nav-link ${isActive ? 'active' : ''}`}><Settings size={18}/><span className="nav-label">Settings</span></NavLink><div className="sidebar-foot"><span className="online-dot"/><span className="sidebar-foot-label">Authenticated workspace</span></div></div>
    </aside>
    <main className="main-area"><header className="topbar">
      <button className="icon-button desktop-collapse" aria-label={collapsed?'Expand sidebar':'Collapse sidebar'} title={collapsed?'Expand sidebar':'Collapse sidebar'} onClick={() => setCollapsed((value) => !value)}>{collapsed?<PanelLeftOpen size={19}/>:<PanelLeftClose size={19}/>}</button>
      <button className="icon-button mobile-menu" aria-label="Open navigation" onClick={() => setMobileOpen(true)}><Menu size={20}/></button>
      <div className="crumb"><span>Workspace</span><span className="crumb-sep">/</span><strong>{title}</strong></div>
      <div className="topbar-actions">
        <div className="global-page-search" ref={searchRef}><Search size={16}/><input aria-label="Search pages" placeholder="Search pages" value={pageSearch} onChange={(event) => setPageSearch(event.target.value)} onKeyDown={onSearchKeyDown}/>{pageSearch&&<div className="page-search-results" role="listbox" aria-label="Matching pages">{results.map((item)=>{const Icon=item.icon;return <button key={item.to} role="option" aria-selected="false" onClick={()=>openPage(item.to)}><Icon size={15}/>{item.label}</button>})}{!results.length&&<span>No accessible pages match</span>}</div>}</div>
        <button className="icon-button theme-toggle" aria-label={`Switch to ${theme==='light'?'dark':'light'} theme`} title={`Switch to ${theme==='light'?'dark':'light'} theme`} onClick={toggleTheme}>{theme==='light'?<Moon size={18}/>:<Sun size={18}/>}</button>
        <NavLink to="/notifications" aria-label={`Notifications${unread.data>0?`, ${unread.data} unread`:''}`} className="icon-button notification-button"><Bell size={19}/>{unread.data > 0 && <i/>}</NavLink>
        <div className="profile-menu-wrap" ref={profileRef}><button className="profile profile-trigger" aria-haspopup="menu" aria-expanded={profileOpen} onClick={()=>setProfileOpen((value)=>!value)}><span className="avatar">{fullName.charAt(0).toUpperCase()}</span><span className="profile-copy"><strong>{fullName}</strong><span>{user?.roles?.[0]?.replaceAll('_',' ') || 'Member'}</span></span><span className="profile-chevron">⌄</span></button>{profileOpen&&<div className="profile-dropdown" role="menu"><div className="profile-dropdown-user"><strong>{fullName}</strong><span>{user?.email}</span><small>{user?.roles?.join(', ')}</small></div><NavLink role="menuitem" to="/settings" onClick={()=>setProfileOpen(false)}>Account settings</NavLink><button role="menuitem" onClick={()=>{setProfileOpen(false);void logout()}}><LogOut size={15}/>Sign out</button></div>}</div>
      </div>
    </header><section className="page-wrap"><Outlet /></section><footer className="app-footer"><span>FleetFlow Operations</span><span>Connected to your FleetFlow backend</span></footer></main>
  </div>;
}
