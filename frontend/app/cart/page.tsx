'use client';

import { useCallback, useEffect, useState } from 'react';
import { AuthGate } from '../../components/AuthGate';
import { ProductImage } from '../../components/ProductImage';
import { api, money } from '../../lib/api';
import type { Cart } from '../../lib/types';
import { useAuth } from '../providers';

export default function CartPage() { return <AuthGate><CartContent /></AuthGate>; }

function CartContent() {
  const { token } = useAuth();
  const [cart, setCart] = useState<Cart | null>(null);
  const [error, setError] = useState('');
  const load = useCallback(() => token && api.cart(token).then(setCart).catch(err => setError(err.message)), [token]);
  useEffect(() => { load(); }, [load]);

  async function change(itemId: number, quantity: number) {
    if (!token || quantity < 1) return;
    try { setCart(await api.updateCart(token, itemId, quantity)); } catch (err) { setError(err instanceof Error ? err.message : 'Update failed'); }
  }
  async function remove(itemId: number) { if (token) setCart(await api.removeCart(token, itemId)); }

  return <main className="content-page"><div className="page-title"><p className="eyebrow">YOUR SELECTION</p><h1>Shopping cart</h1></div>
    {error && <p className="notice error-message">{error}</p>}
    {!cart && !error && <div className="page-state">Loading cart…</div>}
    {cart?.items.length === 0 && <div className="empty-state"><h2>Your cart is empty.</h2><p>Browse the catalog and add something useful.</p><a className="primary-button" href="/">Browse products</a></div>}
    {cart && cart.items.length > 0 && <div className="cart-layout"><section className="cart-list">{cart.items.map(item => <article className="cart-item" key={item.id}>
      <div className="cart-thumb"><ProductImage productId={item.productId} productName={item.productName} sizes="70px" /></div><div className="cart-info"><a href={`/products/${item.productId}`}>{item.productName}</a><span>{money(item.unitPrice)} each</span></div>
      <input aria-label={`Quantity for ${item.productName}`} type="number" min="1" value={item.quantity} onChange={event => change(item.id, Number(event.target.value))} />
      <strong>{money(item.subtotal)}</strong><button className="text-button" onClick={() => remove(item.id)}>Remove</button>
    </article>)}</section>
      <aside className="summary-card"><p className="eyebrow">SUMMARY</p><div><span>Items</span><span>{cart.items.reduce((sum,item) => sum + item.quantity,0)}</span></div><div className="summary-total"><span>Total</span><strong>{money(cart.totalAmount)}</strong></div><a className="primary-button full-button" href="/checkout">Continue to checkout</a><small>Taxes and delivery are included in this university demo.</small></aside>
    </div>}
  </main>;
}
