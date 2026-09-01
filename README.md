# CampusCart E-commerce Platform

CampusCart is a small Java full-stack e-commerce project designed for a university demonstration. It uses three Spring Boot services, a React/Next-compatible frontend, MySQL for transactional data, MongoDB for reviews, JWT authentication, and Docker Compose.

## What is included

- Auth service (`8081`): registration, BCrypt, login, JWT, and `/api/auth/me`
- Product service (`8082`): CRUD, listing, search/filter, stock, sample data, and MongoDB reviews
- Order service (`8083`): persistent cart, checkout, order history, cancellation, inventory calls, COD, and safe demo payment
- Frontend (`3000`): register/login, catalog, filters, product details, reviews, cart, checkout, and orders
- MySQL (`3306`) and MongoDB (`27017`)

## Fastest way to run

Requirements: Docker Desktop with Docker Compose.

1. Copy `.env.example` to `.env`.
2. Change `MYSQL_ROOT_PASSWORD` and `JWT_SECRET` in `.env`.
3. From this directory run:

```powershell
docker compose up --build
```

The repository ignores `.env` and generated build output. Keep real credentials only in `.env`; `.env.example` contains placeholders that are safe to commit.

Open [http://localhost:3000](http://localhost:3000). The product service inserts six sample products only when its database is empty.

Stop the stack with `docker compose down`. Add `-v` only when you intentionally want to remove the MySQL and MongoDB volumes as well.

## Demo flow

1. Register a new user.
2. Search or filter the product catalog.
3. Open a product and add it to the cart.
4. Checkout with `COD`, or choose `DEMO_CARD`. The frontend sends the fixed token `DEMO_SUCCESS`; it never asks for real card details.
5. Open Orders to see history or cancel a placed order.
6. Sign in and add one review per product.

## Run tests without Docker

The backend tests use in-memory H2 databases and mock external service calls, so they do not modify local MySQL data.

```powershell
cd auth-service
.\mvnw.cmd test

cd ..\product-service
mvn test

cd ..\order-service
mvn test

cd ..\frontend
npm install
npm run build
```

## Run services locally without Docker

You need MySQL schemas named `ecommerce_auth`, `ecommerce_products`, and `ecommerce_orders`, plus MongoDB. Set these environment variables before starting services:

| Variable | Purpose |
|---|---|
| `DB_URL` | JDBC URL for that service's MySQL schema |
| `DB_USERNAME` / `DB_PASSWORD` | MySQL login |
| `JWT_SECRET` | Same secret in auth, product, and order services; minimum 32 bytes |
| `MONGODB_URI` | Product review database connection |
| `PRODUCT_SERVICE_URL` | Product base URL used by order service |
| `CORS_ALLOWED_ORIGINS` | Comma-separated frontend origins |

Start the backend services with `mvn spring-boot:run` (or the auth Maven wrapper), then start the frontend with `npm run dev`.

## Main API routes

| Method and route | Purpose |
|---|---|
| `POST /api/auth/register` | Create a user |
| `POST /api/auth/login` | Return JWT and user data |
| `GET /api/auth/me` | Validate JWT and return current user |
| `GET /api/products` | Page, search, and filter products |
| `POST/PUT/DELETE /api/products` | Product CRUD |
| `POST /api/products/{id}/stock/decrease` | Reserve stock |
| `GET/POST /api/products/{id}/reviews` | List or create MongoDB reviews |
| `GET/POST/PUT/DELETE /api/cart` | Manage the signed-in user's cart |
| `POST /api/orders` | Checkout the current cart |
| `GET /api/orders` | Signed-in user's order history |
| `POST /api/orders/{id}/cancel` | Cancel and restore inventory |

See [ARCHITECTURE.md](ARCHITECTURE.md) for service boundaries and [PROJECT_EXPLANATION.md](PROJECT_EXPLANATION.md) for a beginner-friendly interview explanation.


