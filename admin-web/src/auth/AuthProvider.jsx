import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { authApi, readSession, writeSession } from '../api/client.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [session, setSession] = useState(readSession);
  const [loading, setLoading] = useState(Boolean(readSession()?.accessToken));

  useEffect(() => {
    const expire = () => { writeSession(null); setSession(null); };
    window.addEventListener('fleetflow:session-expired', expire);
    return () => window.removeEventListener('fleetflow:session-expired', expire);
  }, []);

  useEffect(() => {
    if (!session?.accessToken) { setLoading(false); return; }
    let alive = true;
    authApi.me().then((user) => {
      if (!alive) return;
      const next = { ...readSession(), user };
      writeSession(next); setSession(next);
    }).catch(() => {
      if (alive && !readSession()?.refreshToken) { writeSession(null); setSession(null); }
    }).finally(() => { if (alive) setLoading(false); });
    return () => { alive = false; };
  }, []);

  const login = useCallback(async (credentials) => {
    const tokens = await authApi.login(credentials);
    const next = { ...tokens };
    writeSession(next);
    try {
      const user = await authApi.me();
      const authenticated = { ...next, user };
      writeSession(authenticated); setSession(authenticated);
    } catch (error) {
      writeSession(null);
      throw error;
    }
  }, []);

  const logout = useCallback(async () => {
    const refresh = readSession()?.refreshToken;
    try { if (refresh) await authApi.logout(refresh); }
    finally { writeSession(null); setSession(null); }
  }, []);

  const value = useMemo(() => ({ session, user: session?.user, loading, login, logout }), [session, loading, login, logout]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error('useAuth must be inside AuthProvider');
  return value;
}
