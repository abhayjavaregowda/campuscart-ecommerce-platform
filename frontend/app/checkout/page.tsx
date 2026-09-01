'use client';

import { FormEvent, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { AuthGate } from '../../components/AuthGate';
import { api, money } from '../../lib/api';
import type { Cart } from '../../lib/types';
import { useAuth } from '../providers';

export default function CheckoutPage() { return <AuthGate><CheckoutContent /></AuthGate>; }

function CheckoutContent() {
  const { token } = useAuth(); const router = useRouter();
  const [cart, setCart] = useState<Cart | null>(null); const [address, setAddress] = useState('');
  const [method, setMethod] = useState('COD'); const [error, setError] = useState(''); const [busy, setBusy] = useState(false);
  useEffect(() => { if (token) api.cart(token).then(setCart).catch(err => setError(err.message)); }, [token]);

  async function submit(event: FormEvent) {
    event.preventDefault(); if (!token) return; setBusy(true); setError('');
    try { await api.checkout(token, { shippingAddress: address, paymentMethod: method, paymentToken: method === 'DEMO_CARD' ? 'DEMO_SUCCESS' : undefined }); router.push('/orders?placed=true'); }
    catch (err) { setError(err instanceof Error ? err.message : 'Checkout failed'); setBusy(false); }
  }

  return <main className="content-page"><div className="page-title"><p className="eyebrow">FINAL STEP</p><h1>Checkout</h1></div>
    <div className="checkout-layout"><form className="checkout-form" onSubmit={submit}><h2>Delivery</h2>
      <label>Shipping address<textarea required maxLength={500} value={address} onChange={event => setAddress(event.target.value)} placeholder="Room / house, street, city, postal code" /></label>
      <h2>Payment</h2><label className="payment-option"><input type="radio" value="COD" checked={method === 'COD'} onChange={event => setMethod(event.target.value)} /><span><strong>Cash on delivery</strong><small>Payment stays pending until delivery.</small></span></label>
      <label className="payment-option"><input type="radio" value="DEMO_CARD" checked={method === 'DEMO_CARD'} onChange={event => setMethod(event.target.value)} /><span><strong>Demo card payment</strong><small>Simulates success. Never enter real card details.</small></span></label>
      {error && <p className="error-message">{error}</p>}<button className="primary-button" disabled={busy || !cart?.items.length}>{busy ? 'Placing order…' : 'Place order'}</button>
    </form><aside className="summary-card"><p className="eyebrow">ORDER</p>{cart?.items.map(item => <div key={item.id}><span>{item.quantity} × {item.productName}</span><span>{money(item.subtotal)}</span></div>)}<div className="summary-total"><span>Total</span><strong>{money(cart?.totalAmount || 0)}</strong></div></aside></div>
  </main>;
}
