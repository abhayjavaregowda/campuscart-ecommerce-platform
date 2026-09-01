'use client';

import { FormEvent, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '../providers';

export default function LoginPage() {
  const { login } = useAuth();
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault(); setBusy(true); setError('');
    try { await login(email, password); router.push('/'); }
    catch (err) { setError(err instanceof Error ? err.message : 'Login failed'); }
    finally { setBusy(false); }
  }

  return <main className="auth-page"><section className="auth-card">
    <p className="eyebrow">WELCOME BACK</p><h1>Sign in to continue.</h1>
    <p className="muted">Your cart, checkout, reviews, and order history use this account.</p>
    <form className="stack-form" onSubmit={submit}>
      <label>Email<input type="email" required value={email} onChange={event => setEmail(event.target.value)} /></label>
      <label>Password<input type="password" required value={password} onChange={event => setPassword(event.target.value)} /></label>
      {error && <p className="error-message" role="alert">{error}</p>}
      <button className="primary-button" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'}</button>
    </form>
    <p className="form-foot">New here? <a href="/register">Create an account</a></p>
  </section></main>;
}
