import type { Cart, Order, Product, ProductPage, ReviewList, User } from './types';

const AUTH_URL = process.env.NEXT_PUBLIC_AUTH_API_URL ?? 'http://localhost:8081';
const PRODUCT_URL = process.env.NEXT_PUBLIC_PRODUCT_API_URL ?? 'http://localhost:8082';
const ORDER_URL = process.env.NEXT_PUBLIC_ORDER_API_URL ?? 'http://localhost:8083';

type Options = RequestInit & { token?: string | null };

async function request<T>(url: string, options: Options = {}): Promise<T> {
  const headers = new Headers(options.headers);
  if (options.body) headers.set('Content-Type', 'application/json');
  if (options.token) headers.set('Authorization', `Bearer ${options.token}`);
  const response = await fetch(url, { ...options, headers, cache: 'no-store' });
  if (response.status === 204) return undefined as T;
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    const details = data.validationErrors
      ? Object.values(data.validationErrors).join('. ')
      : data.message;
    throw new Error(details || `Request failed (${response.status})`);
  }
  return data as T;
}

export const api = {
  register: (body: { name: string; email: string; password: string }) =>
    request<User>(`${AUTH_URL}/api/auth/register`, { method: 'POST', body: JSON.stringify(body) }),
  login: (body: { email: string; password: string }) =>
    request<{ token: string; tokenType: string; user: User }>(`${AUTH_URL}/api/auth/login`, {
      method: 'POST', body: JSON.stringify(body),
    }),
  me: (token: string) => request<User>(`${AUTH_URL}/api/auth/me`, { token }),
  products: (query: URLSearchParams) => request<ProductPage>(`${PRODUCT_URL}/api/products?${query}`),
  categories: () => request<string[]>(`${PRODUCT_URL}/api/products/categories`),
  product: (id: string | number) => request<Product>(`${PRODUCT_URL}/api/products/${id}`),
  reviews: (id: string | number) => request<ReviewList>(`${PRODUCT_URL}/api/products/${id}/reviews`),
  addReview: (id: string | number, token: string, body: { rating: number; comment: string }) =>
    request(`${PRODUCT_URL}/api/products/${id}/reviews`, {
      method: 'POST', token, body: JSON.stringify(body),
    }),
  cart: (token: string) => request<Cart>(`${ORDER_URL}/api/cart`, { token }),
  addCart: (token: string, productId: number, quantity: number) =>
    request<Cart>(`${ORDER_URL}/api/cart/items`, {
      method: 'POST', token, body: JSON.stringify({ productId, quantity }),
    }),
  updateCart: (token: string, itemId: number, quantity: number) =>
    request<Cart>(`${ORDER_URL}/api/cart/items/${itemId}`, {
      method: 'PUT', token, body: JSON.stringify({ quantity }),
    }),
  removeCart: (token: string, itemId: number) =>
    request<Cart>(`${ORDER_URL}/api/cart/items/${itemId}`, { method: 'DELETE', token }),
  checkout: (token: string, body: { shippingAddress: string; paymentMethod: string; paymentToken?: string }) =>
    request<Order>(`${ORDER_URL}/api/orders`, { method: 'POST', token, body: JSON.stringify(body) }),
  orders: (token: string) => request<Order[]>(`${ORDER_URL}/api/orders`, { token }),
  cancelOrder: (token: string, id: number) =>
    request<Order>(`${ORDER_URL}/api/orders/${id}/cancel`, { method: 'POST', token }),
};

export const money = (value: number) =>
  new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(value);
