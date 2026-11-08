import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { authApi, readSession, writeSession } from '../api/client.js';

export type UserProfile = {
  id?: string;
  email: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
  status?: string;
  roles: string[];
};

export type AuthSession = {
  accessToken: string;
  refreshToken: string;
  tokenType?: string;
  expiresIn?: number;
  user?: UserProfile;
};

type Credentials = { email: string; password: string };
type AuthContextValue = {
  session: AuthSession | null;
  user: UserProfile | undefined;
  loading: boolean;
  login: (credentials: Credentials) => Promise<void>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(() => readSession() as AuthSession | null);
  const [loading, setLoading] = useState(Boolean(readSession()?.accessToken));

  useEffect(() => {
    const expire = () => { writeSession(null); setSession(null); };
    window.addEventListener('fleetflow:session-expired', expire);
    return () => window.removeEventListener('fleetflow:session-expired', expire);
  }, []);

  useEffect(() => {
    if (!session?.accessToken) { setLoading(false); return; }
    let alive = true;
    authApi.me().then((user: UserProfile) => {
      if (!alive) return;
      const next = { ...readSession(), user } as AuthSession;
      writeSession(next); setSession(next);
    }).catch(() => {
      if (alive && !readSession()?.refreshToken) { writeSession(null); setSession(null); }
    }).finally(() => { if (alive) setLoading(false); });
    return () => { alive = false; };
  }, []);

  const login = useCallback(async (credentials: Credentials) => {
    const tokens = await authApi.login(credentials) as Omit<AuthSession, 'user'>;
    const next = { ...tokens } as AuthSession;
    writeSession(next);
    try {
      const user = await authApi.me() as UserProfile;
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
