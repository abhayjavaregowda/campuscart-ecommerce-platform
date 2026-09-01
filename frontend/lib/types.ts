export type User = { id: number; name: string; email: string; role: string };

export type Product = {
  id: number;
  name: string;
  description: string;
  category: string;
  price: number;
  stock: number;
  imageUrl?: string;
  active: boolean;
};

export type ProductPage = {
  content: Product[];
  totalElements: number;
  totalPages: number;
  number: number;
};

export type Review = {
  id: string;
  productId: number;
  userEmail: string;
  rating: number;
  comment: string;
  createdAt: string;
};

export type ReviewList = { reviews: Review[]; averageRating: number; reviewCount: number };

export type CartItem = {
  id: number;
  productId: number;
  productName: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
  imageUrl?: string;
};

export type Cart = { items: CartItem[]; totalAmount: number };

export type OrderItem = {
  id: number;
  productId: number;
  productName: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
};

export type Order = {
  id: number;
  status: string;
  paymentStatus: string;
  paymentMethod: string;
  shippingAddress: string;
  totalAmount: number;
  items: OrderItem[];
  createdAt: string;
};
