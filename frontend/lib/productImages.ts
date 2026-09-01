const SEEDED_PRODUCT_IMAGES: Record<number, string> = {
  1: '/products/mechanical-keyboard.svg',
  2: '/products/wireless-mouse.svg',
  3: '/products/java-interview-guide.svg',
  4: '/products/everyday-backpack.svg',
  5: '/products/insulated-bottle.svg',
  6: '/products/usb-c-study-lamp.svg',
};

const SEEDED_PRODUCT_IMAGES_BY_NAME: Record<string, string> = {
  'mechanical keyboard': '/products/mechanical-keyboard.svg',
  'wireless mouse': '/products/wireless-mouse.svg',
  'java interview guide': '/products/java-interview-guide.svg',
  'everyday backpack': '/products/everyday-backpack.svg',
  'insulated bottle': '/products/insulated-bottle.svg',
  'usb-c study lamp': '/products/usb-c-study-lamp.svg',
};

export function productImagePath(productId: number, productName: string) {
  return SEEDED_PRODUCT_IMAGES[productId] ?? SEEDED_PRODUCT_IMAGES_BY_NAME[productName.trim().toLowerCase()];
}
