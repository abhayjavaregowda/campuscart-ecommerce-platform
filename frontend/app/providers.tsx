'use client';

import { createContext, useContext, useEffect, useState } from 'react';
import { api } from '../lib/api';
import type { User } from '../lib/types';

type AuthContextValue = {
  user: User | null;
  token: string | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (name: string, email: string, password: string) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function Providers({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const saved = window.localStorage.getItem('campuscart_token');
    queueMicrotask(() => {
      if (!saved) { setLoading(false); return; }
      setToken(saved);
      api.me(saved).then(setUser).catch(() => window.localStorage.removeItem('campuscart_token'))
        .finally(() => setLoading(false));
    });
  }, []);

  async function login(email: string, password: string) {
    const response = await api.login({ email, password });
    window.localStorage.setItem('campuscart_token', response.token);
    setToken(response.token);
    setUser(response.user);
  }

  async function register(name: string, email: string, password: string) {
    await api.register({ name, email, password });
    await login(email, password);
  }

  function logout() {
    window.localStorage.removeItem('campuscart_token');
    setToken(null);
    setUser(null);
  }

  const value = { user, token, loading, login, register, logout };
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used inside Providers');
  return context;
}
