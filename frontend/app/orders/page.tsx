'use client';

import { useCallback, useEffect, useState } from 'react';
import { AuthGate } from '../../components/AuthGate';
import { api, money } from '../../lib/api';
import type { Order } from '../../lib/types';
import { useAuth } from '../providers';

export default function OrdersPage() { return <AuthGate><OrdersContent /></AuthGate>; }

function OrdersContent() {
  const { token } = useAuth(); const [orders, setOrders] = useState<Order[] | null>(null); const [error, setError] = useState('');
  const load = useCallback(() => token && api.orders(token).then(setOrders).catch(err => setError(err.message)), [token]);
  useEffect(() => { load(); }, [load]);
  async function cancel(id: number) { if (!token) return; try { await api.cancelOrder(token, id); await load(); } catch (err) { setError(err instanceof Error ? err.message : 'Cancellation failed'); } }

  return <main className="content-page"><div className="page-title"><p className="eyebrow">ACCOUNT</p><h1>Order history</h1></div>
    {error && <p className="notice error-message">{error}</p>}{!orders && !error && <div className="page-state">Loading orders…</div>}
    {orders?.length === 0 && <div className="empty-state"><h2>No orders yet.</h2><p>Your completed checkouts will appear here.</p><a className="primary-button" href="/">Shop products</a></div>}
    <div className="order-list">{orders?.map(order => <article className="order-card" key={order.id}>
      <header><div><p className="eyebrow">ORDER #{order.id}</p><h2>{new Date(order.createdAt).toLocaleDateString('en-IN',{dateStyle:'medium'})}</h2></div><div className="badges"><span>{order.status}</span><span>{order.paymentStatus}</span></div></header>
      <div className="order-items">{order.items.map(item => <div key={item.id}><span>{item.quantity} × <a href={`/products/${item.productId}`}>{item.productName}</a></span><strong>{money(item.subtotal)}</strong></div>)}</div>
      <footer><div><span>Deliver to</span><p>{order.shippingAddress}</p></div><strong>{money(order.totalAmount)}</strong>{['PLACED','PROCESSING'].includes(order.status) && <button className="text-button" onClick={() => cancel(order.id)}>Cancel order</button>}</footer>
    </article>)}</div>
  </main>;
}
