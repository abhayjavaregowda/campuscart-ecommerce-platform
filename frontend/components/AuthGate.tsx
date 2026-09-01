'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '../app/providers';

export function AuthGate({ children }: { children: React.ReactNode }) {
  const { token, loading } = useAuth();
  const router = useRouter();
  useEffect(() => { if (!loading && !token) router.replace('/login'); }, [loading, token, router]);
  if (loading || !token) return <div className="page-state">Checking your session…</div>;
  return children;
}
