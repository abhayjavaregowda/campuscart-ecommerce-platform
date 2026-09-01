'use client';

import Image from 'next/image';
import { useState } from 'react';
import { productImagePath } from '../lib/productImages';

type ProductImageProps = {
  productId: number;
  productName: string;
  sizes: string;
};

export function ProductImage({ productId, productName, sizes }: ProductImageProps) {
  const [failed, setFailed] = useState(false);
  const src = productImagePath(productId, productName);

  if (!src || failed) return <span aria-hidden="true">{productName.slice(0, 1)}</span>;

  return <Image
    src={src}
    alt={`${productName} illustration`}
    fill
    sizes={sizes}
    style={{ objectFit: 'contain' }}
    unoptimized
    onError={() => setFailed(true)}
  />;
}
