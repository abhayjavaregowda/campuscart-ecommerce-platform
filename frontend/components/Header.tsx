'use client';

import { useAuth } from '../app/providers';

export function Header() {
  const { user, loading, logout } = useAuth();
  return (
    <header className="site-header">
      <a className="brand" href="/">CampusCart</a>
      <nav aria-label="Main navigation">
        <a href="/">Products</a>
        <a href="/orders">Orders</a>
        <a href="/cart">Cart</a>
        {!loading && (user ? (
          <button className="nav-action" type="button" onClick={logout}>Log out · {user.name.split(' ')[0]}</button>
        ) : <a className="cart-link" href="/login">Sign in</a>)}
      </nav>
    </header>
  );
}
