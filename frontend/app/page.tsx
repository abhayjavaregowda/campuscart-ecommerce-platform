'use client';

import { FormEvent, useEffect, useState } from 'react';
import { ProductImage } from '../components/ProductImage';
import { api, money } from '../lib/api';
import type { Product, ProductPage } from '../lib/types';

export default function Home() {
  const [products, setProducts] = useState<ProductPage | null>(null);
  const [categories, setCategories] = useState<string[]>([]);
  const [searchInput, setSearchInput] = useState('');
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('');
  const [inStock, setInStock] = useState(false);
  const [page, setPage] = useState(0);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    const query = new URLSearchParams({ page: String(page), size: '9', sortBy: 'name' });
    if (search) query.set('search', search);
    if (category) query.set('category', category);
    if (inStock) query.set('inStock', 'true');
    api.products(query).then(data => { if (active) { setProducts(data); setError(''); } })
      .catch(err => { if (active) setError(err instanceof Error ? err.message : 'Could not load products'); });
    return () => { active = false; };
  }, [search, category, inStock, page]);
  useEffect(() => { api.categories().then(setCategories).catch(() => undefined); }, []);

  function submitSearch(event: FormEvent) {
    event.preventDefault(); setPage(0); setSearch(searchInput.trim());
  }

  return <main>
    <section className="hero">
      <p className="eyebrow">SMART PICKS · SIMPLE CHECKOUT</p>
      <h1>Everything you need,<br />without the noise.</h1>
      <p className="hero-copy">A focused university marketplace with clear prices, honest stock, and a checkout you can explain.</p>
      <form className="search-bar" onSubmit={submitSearch}>
        <label className="sr-only" htmlFor="search">Search products</label>
        <input id="search" value={searchInput} onChange={event => setSearchInput(event.target.value)} placeholder="Search products, descriptions…" />
        <button type="submit">Search</button>
      </form>
    </section>

    <section className="catalog-shell">
      <div className="section-heading">
        <div><p className="eyebrow">CATALOG</p><h2>{search ? `Results for “${search}”` : 'Explore the catalog'}</h2></div>
        <div className="filters">
          <select aria-label="Filter by category" value={category} onChange={event => { setCategory(event.target.value); setPage(0); }}>
            <option value="">All categories</option>{categories.map(item => <option key={item}>{item}</option>)}
          </select>
          <label className="check-filter"><input type="checkbox" checked={inStock} onChange={event => { setInStock(event.target.checked); setPage(0); }} /> In stock</label>
        </div>
      </div>
      {error && <div className="notice error-message">Product service: {error}</div>}
      {!products && !error && <div className="page-state">Loading the catalog…</div>}
      {products?.content.length === 0 && <div className="empty-state"><h3>No products found.</h3><p>Try a broader search or remove a filter.</p></div>}
      <div className="product-grid">
        {products?.content.map(product => <ProductCard key={product.id} product={product} />)}
      </div>
      {products && products.totalPages > 1 && <div className="pagination">
        <button disabled={page === 0} onClick={() => setPage(value => value - 1)}>Previous</button>
        <span>Page {page + 1} of {products.totalPages}</span>
        <button disabled={page + 1 >= products.totalPages} onClick={() => setPage(value => value + 1)}>Next</button>
      </div>}
    </section>
  </main>;
}

function ProductCard({ product }: { product: Product }) {
  return <article className="product-card">
    <a className="product-art" href={`/products/${product.id}`} aria-label={`View ${product.name}`}>
      <ProductImage productId={product.id} productName={product.name} sizes="(max-width: 760px) 92vw, 30vw" />
    </a>
    <div className="product-meta"><span>{product.category}</span><span>{product.stock > 0 ? `${product.stock} in stock` : 'Sold out'}</span></div>
    <h3><a href={`/products/${product.id}`}>{product.name}</a></h3>
    <div className="product-footer"><strong>{money(product.price)}</strong><a className="small-button" href={`/products/${product.id}`}>View product</a></div>
  </article>;
}
