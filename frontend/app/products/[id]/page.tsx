'use client';

import { FormEvent, useCallback, useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { ProductImage } from '../../../components/ProductImage';
import { api, money } from '../../../lib/api';
import type { Product, ReviewList } from '../../../lib/types';
import { useAuth } from '../../providers';

export default function ProductDetailPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const { token, user } = useAuth();
  const [product, setProduct] = useState<Product | null>(null);
  const [reviews, setReviews] = useState<ReviewList | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const loadReviews = useCallback(() => api.reviews(id).then(setReviews).catch(() => setReviews({ reviews: [], averageRating: 0, reviewCount: 0 })), [id]);
  useEffect(() => { api.product(id).then(setProduct).catch(err => setError(err.message)); loadReviews(); }, [id, loadReviews]);

  async function addToCart() {
    if (!token) { router.push('/login'); return; }
    setError('');
    try { await api.addCart(token, Number(id), quantity); setMessage('Added to your cart.'); }
    catch (err) { setError(err instanceof Error ? err.message : 'Could not add to cart'); }
  }

  async function submitReview(event: FormEvent) {
    event.preventDefault();
    if (!token) { router.push('/login'); return; }
    try { await api.addReview(id, token, { rating, comment }); setComment(''); setMessage('Review published.'); await loadReviews(); }
    catch (err) { setError(err instanceof Error ? err.message : 'Could not publish review'); }
  }

  if (error && !product) return <main className="content-page"><div className="notice error-message">{error}</div></main>;
  if (!product) return <div className="page-state">Loading product…</div>;

  return <main className="content-page">
    <section className="detail-grid">
      <div className="detail-art"><ProductImage productId={product.id} productName={product.name} sizes="(max-width: 760px) 92vw, 48vw" /></div>
      <div className="detail-copy">
        <p className="eyebrow">{product.category}</p><h1>{product.name}</h1>
        <p className="detail-price">{money(product.price)}</p><p className="muted long-copy">{product.description}</p>
        <p className={product.stock ? 'stock-good' : 'stock-out'}>{product.stock ? `${product.stock} available` : 'Currently sold out'}</p>
        <div className="buy-row"><input aria-label="Quantity" type="number" min="1" max={product.stock} value={quantity} onChange={event => setQuantity(Number(event.target.value))} />
          <button className="primary-button" disabled={!product.stock} onClick={addToCart}>Add to cart</button></div>
        {message && <p className="success-message">{message}</p>}{error && <p className="error-message">{error}</p>}
      </div>
    </section>
    <section className="reviews-section">
      <div className="review-summary"><p className="eyebrow">REVIEWS</p><h2>{reviews?.averageRating || '—'} <span>/ 5</span></h2><p>{reviews?.reviewCount || 0} verified community reviews</p></div>
      <form className="review-form" onSubmit={submitReview}><h3>Share your experience</h3>
        <label>Rating<select value={rating} onChange={event => setRating(Number(event.target.value))}>{[5,4,3,2,1].map(value => <option key={value} value={value}>{value} stars</option>)}</select></label>
        <label>Comment<textarea required maxLength={1000} value={comment} onChange={event => setComment(event.target.value)} placeholder={user ? `Review as ${user.name}` : 'Sign in to review'} /></label>
        <button className="secondary-button">{token ? 'Publish review' : 'Sign in to review'}</button>
      </form>
      <div className="review-list">{reviews?.reviews.map(review => <article key={review.id} className="review-card"><div><strong>{'★'.repeat(review.rating)}</strong><span>{new Date(review.createdAt).toLocaleDateString('en-IN')}</span></div><p>{review.comment}</p><small>{review.userEmail}</small></article>)}</div>
    </section>
  </main>;
}
