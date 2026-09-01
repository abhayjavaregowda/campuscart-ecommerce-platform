'use client';

import { FormEvent, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '../providers';

export default function RegisterPage() {
  const { register } = useAuth();
  const router = useRouter();
  const [form, setForm] = useState({ name: '', email: '', password: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault(); setBusy(true); setError('');
    try { await register(form.name, form.email, form.password); router.push('/'); }
    catch (err) { setError(err instanceof Error ? err.message : 'Registration failed'); }
    finally { setBusy(false); }
  }

  return <main className="auth-page"><section className="auth-card">
    <p className="eyebrow">JOIN CAMPUSCART</p><h1>Create your account.</h1>
    <p className="muted">Use at least eight characters for your password.</p>
    <form className="stack-form" onSubmit={submit}>
      <label>Name<input required maxLength={100} value={form.name} onChange={event => setForm({...form,name:event.target.value})} /></label>
      <label>Email<input type="email" required value={form.email} onChange={event => setForm({...form,email:event.target.value})} /></label>
      <label>Password<input type="password" required minLength={8} value={form.password} onChange={event => setForm({...form,password:event.target.value})} /></label>
      {error && <p className="error-message" role="alert">{error}</p>}
      <button className="primary-button" disabled={busy}>{busy ? 'Creating account…' : 'Create account'}</button>
    </form>
    <p className="form-foot">Already registered? <a href="/login">Sign in</a></p>
  </section></main>;
}
