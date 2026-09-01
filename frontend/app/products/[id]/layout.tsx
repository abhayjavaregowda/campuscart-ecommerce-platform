import type { Metadata } from 'next';
import { productImagePath } from '../../../lib/productImages';

const PRODUCT_URL = process.env.NEXT_PUBLIC_PRODUCT_API_URL ?? 'http://localhost:8082';

export async function generateMetadata({ params }: { params: Promise<{ id: string }> }): Promise<Metadata> {
  const { id } = await params;
  try {
    const response = await fetch(`${PRODUCT_URL}/api/products/${id}`);
    if (!response.ok) throw new Error('Not found');
    const product = await response.json();
    const image = productImagePath(Number(id), product.name);
    return {
      title: `${product.name} — CampusCart`,
      description: product.description,
      openGraph: { title: `${product.name} — CampusCart`, description: product.description, images: image ? [image] : [] },
      twitter: { card: 'summary_large_image', title: `${product.name} — CampusCart`, description: product.description, images: image ? [image] : [] },
    };
  } catch {
    return { title: 'Product — CampusCart', description: 'View product details, stock, and community reviews.' };
  }
}

export default function ProductLayout({ children }: { children: React.ReactNode }) { return children; }
