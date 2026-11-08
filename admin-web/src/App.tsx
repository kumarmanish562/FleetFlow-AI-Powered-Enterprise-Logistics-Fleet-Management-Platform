import { lazy, Suspense, type ReactNode } from 'react';
import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { useAuth } from './auth/AuthProvider.tsx';
import { AppShell } from './components/AppShell.tsx';

const LoginPage = lazy(() => import('./pages/LoginPage.jsx').then((module) => ({ default: module.LoginPage })));
const DashboardPage = lazy(() => import('./pages/DashboardPage.jsx').then((module) => ({ default: module.DashboardPage })));
const ResourcePage = lazy(() => import('./pages/ResourcePage.jsx').then((module) => ({ default: module.ResourcePage })));

function Deferred({ children }: { children: ReactNode }) {
  return <Suspense fallback={<div className="screen-state">Loading workspace…</div>}>{children}</Suspense>;
}

function Protected({ children, roles }: { children: ReactNode; roles?: string[] }) {
  const { user, loading } = useAuth();
  const location = useLocation();
  if (loading) return <div className="screen-state">Restoring your session…</div>;
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  if (roles && !user.roles?.some((role) => roles.includes(role))) return <Navigate to="/forbidden" replace />;
  return children;
}

function Forbidden() {
  return <main className="screen-state"><h1>Access unavailable</h1><p>Your account does not have permission to open this area.</p></main>;
}

export default function App() {
  return <Routes>
    <Route path="/login" element={<Deferred><LoginPage /></Deferred>} />
    <Route path="/forbidden" element={<Protected><Forbidden /></Protected>} />
    <Route path="/" element={<Protected><AppShell /></Protected>}>
      <Route index element={<Protected roles={['SUPER_ADMIN','ADMIN']}><Deferred><DashboardPage /></Deferred></Protected>} />
      <Route path="fleet" element={<Protected roles={['SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER','DRIVER']}><Deferred><ResourcePage kind="vehicles" /></Deferred></Protected>} />
      <Route path="drivers" element={<Protected roles={['ADMIN','FLEET_MANAGER','DISPATCHER']}><Deferred><ResourcePage kind="drivers" /></Deferred></Protected>} />
      <Route path="customers" element={<Protected roles={['SUPER_ADMIN','ADMIN']}><Deferred><ResourcePage kind="customers" /></Deferred></Protected>} />
      <Route path="shipments" element={<Protected roles={['SUPER_ADMIN','ADMIN','DISPATCHER']}><Deferred><ResourcePage kind="shipments" /></Deferred></Protected>} />
      <Route path="routes" element={<Protected roles={['SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER']}><Deferred><ResourcePage kind="routes" /></Deferred></Protected>} />
      <Route path="tracking" element={<Protected roles={['SUPER_ADMIN','ADMIN','DISPATCHER','FLEET_MANAGER']}><Deferred><ResourcePage kind="tracking" /></Deferred></Protected>} />
      <Route path="maintenance" element={<Protected roles={['SUPER_ADMIN','ADMIN','FLEET_MANAGER']}><Deferred><ResourcePage kind="maintenance" /></Deferred></Protected>} />
      <Route path="billing" element={<Protected roles={['ADMIN','CUSTOMER']}><Deferred><ResourcePage kind="billing" /></Deferred></Protected>} />
      <Route path="notifications" element={<Deferred><ResourcePage kind="notifications" /></Deferred>} />
      <Route path="analytics" element={<Protected roles={['SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER','FINANCE_MANAGER']}><Deferred><ResourcePage kind="analytics" /></Deferred></Protected>} />
      <Route path="assistant" element={<Protected roles={['SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER']}><Deferred><ResourcePage kind="assistant" /></Deferred></Protected>} />
      <Route path="users" element={<Protected roles={['SUPER_ADMIN','ADMIN']}><Deferred><ResourcePage kind="users" /></Deferred></Protected>} />
      <Route path="settings" element={<Deferred><ResourcePage kind="settings" /></Deferred>} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Route>
  </Routes>;
}
