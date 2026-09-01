# CampusCart Project Explanation

Use this file to understand the project and prepare a clear interview explanation. Focus on the request flow and the reason each class exists.

## One-sentence project explanation

CampusCart is a React storefront connected to three Spring Boot services: auth manages users and JWTs, product manages catalog/stock/reviews, and order manages cart/checkout/history.

## What each service does

### Auth service

Auth service owns the `users` table. It registers users, hashes passwords with BCrypt, verifies login details, creates JWTs, validates JWTs, and returns the current user from `/api/auth/me`.

Important folders:

- `controller`: receives HTTP requests
- `service`: contains registration/login/JWT rules
- `repository`: talks to the database
- `entity`: maps Java objects to database tables
- `config`: Spring Security and the JWT filter
- `dto`: safe request and response shapes
- `exception`: consistent JSON errors

### Product service

Product service owns products and stock in MySQL. It supports CRUD, paging, search, category/price/in-stock filters, category listing, and locked stock increase/decrease operations. It also owns reviews, but review documents are stored in MongoDB.

### Order service

Order service owns carts and orders in MySQL. It identifies the user from the JWT, calls product service to check products/update inventory, creates an immutable price/name snapshot in each order item, and returns only that user's history.

### Frontend

The frontend is a React application with routes for catalog, product details/reviews, register, login, cart, checkout, and order history. Its API helper knows which backend owns each request.

## Controller -> Service -> Repository flow

Think of the layers like this:

1. **Controller:** translates HTTP input into a Java method call. It should stay small.
2. **Service:** applies business rules and controls transactions.
3. **Repository:** reads/writes entities. Spring Data generates common SQL operations.
4. **Database:** stores the final data.

Example: `POST /api/products` reaches `ProductController.create()`, which validates `ProductRequest`, calls `ProductService.create()`, and then `ProductRepository.save()` writes the row.

This separation makes code easier to test and stops controllers from becoming large.

## Registration flow

1. React sends name, email, and password to `POST /api/auth/register`.
2. Jakarta Validation rejects blank/invalid/short fields.
3. `AuthService` trims and lowercases the email.
4. `UserRepository.existsByEmail()` checks for a duplicate.
5. The password is passed to BCrypt.
6. The saved user gets role `USER`.
7. The response contains id/name/email/role, never the password hash.

Duplicate email returns HTTP 409. Bad fields return HTTP 400 with field-level messages.

## BCrypt flow

BCrypt is a one-way password hashing algorithm with a random salt. Registration calls `passwordEncoder.encode(rawPassword)` and stores only the result. Login calls `passwordEncoder.matches(rawPassword, storedHash)`.

The same password can produce different hashes because of the salt. We do not decrypt passwords. If `matches` succeeds, the password is correct.

## Login and JWT flow

1. React sends email/password to `POST /api/auth/login`.
2. Auth loads the user by normalized email.
3. BCrypt compares the submitted password to the stored hash.
4. `JwtService` creates a signed token whose subject (`sub`) is the email and whose expiry is one hour by default.
5. The API returns `{ token, tokenType: "Bearer", user }`.
6. React stores the token and attaches it to protected requests.

A JWT is signed, not encrypted. Do not put passwords or private card data inside it.

## How protected requests work

The browser sends:

```text
Authorization: Bearer eyJ...
```

`JwtAuthenticationFilter` runs before the controller. It extracts the token, verifies the signature/expiry, reads the email, and puts an authenticated object into Spring Security's context. The controller can then use `Authentication.getName()`.

Without a valid token, protected routes return HTTP 401. Product reading is public; writing a review and all cart/order operations require a token.

## Product flow

- Listing builds a JPA `Specification` only from filters the user supplied.
- Paging prevents returning an unlimited catalog.
- Search matches lowercased name or description.
- Stock decrease uses a pessimistic database lock, checks active/available quantity, then updates the row in one transaction.
- Product creation/update uses a validated DTO so invalid negative stock or zero price is rejected.

The service inserts sample products only when the table is empty, which makes the Docker demo immediately usable.

## Review flow

1. Anyone can list reviews for a product.
2. A signed-in user posts rating (1–5) and a comment.
3. Product service confirms the product exists.
4. MongoDB stores a review document.
5. A compound unique index enforces one review per user per product.
6. The list response also calculates average rating and count.

MongoDB is useful here because reviews are document-like content and can evolve independently of product rows.

## Cart flow

1. The JWT email is the cart owner; the client never chooses another email.
2. Adding an item calls product service for current product/price/stock.
3. If the product is already in the cart, quantities are combined.
4. Cart stores a useful price/name snapshot and calculates subtotals.
5. Cart stock is checked but not reserved. Actual reservation happens at checkout.

The `(user_email, product_id)` database constraint stops duplicate rows for one product in a user's cart.

## Order flow

1. Order service validates address and payment method.
2. It reads the authenticated user's cart.
3. It calls product service to decrease each item's stock.
4. It creates `CustomerOrder` plus `OrderItem` rows with product name and price snapshots.
5. It calculates the total on the server, saves the order, and clears the cart.
6. History queries include the JWT email, so one user cannot see another user's orders.
7. Cancelling a placed/processing order increases stock again. A paid demo order becomes `REFUNDED`.

If checkout fails after some stock calls, the service tries to compensate by increasing already-decreased stock. Explain that this is a simple saga-like compensation and not a true distributed transaction.

## Simplified payment flow

- `COD` creates `paymentStatus=PENDING`.
- `DEMO_CARD` requires the fixed token `DEMO_SUCCESS` and creates `paymentStatus=PAID`.
- There are no card number, CVV, or expiry fields.

For a real system, use a payment provider's hosted form/tokenization and webhooks. Never store raw card details.

## Database design

### MySQL: auth schema

`users` has id, name, unique normalized email, BCrypt hash, and role.

### MySQL: product schema

`products` has catalog text, decimal price, integer stock, active flag, and timestamps.

### MySQL: order schema

`cart_items` belongs to a user email. `customer_orders` is the order header. `order_items` has a many-to-one foreign key to the header. Order item snapshots preserve what was purchased even if the catalog later changes.

### MongoDB

`product_reviews` stores review documents and a unique product/user compound index.

Each service owns its own schema. Services communicate through HTTP instead of reading another service's tables.

## Frontend -> backend request flow

1. A page/component calls a function from `frontend/lib/api.ts`.
2. That function selects auth/product/order base URL and sends JSON.
3. Protected calls attach the token stored by `Providers`.
4. A Spring controller validates the request and calls its service.
5. The frontend renders returned JSON or displays the API error message.

Examples:

- Login page → auth service
- Catalog/details/reviews → product service
- Cart/checkout/orders → order service
- Order service → product service for inventory

## What was generated or changed by AI

The repository initially contained working auth basics only. The `product-service`, `order-service`, and `frontend` directories were empty.

### Existing auth files improved by AI

- `pom.xml`, `application.properties`
- `AuthController`, `AuthService`, `User`, `UserRepository`, `SecurityConfig`
- Reused the existing `JwtService`, `JwtAuthenticationFilter`, application class, and login DTO

### Auth files added by AI

- Register/auth/user response DTOs
- Structured exception classes and handler
- H2 test profile and full auth integration tests
- Docker files

### Product service generated by AI

- The complete Spring Boot module: entity, DTOs, controller, service, repositories, errors, configuration, JWT protection, seed data, tests, and Docker files
- MongoDB review entity/repository/service/controller and tests

### Order service generated by AI

- The complete Spring Boot module: cart/order entities, DTOs, repositories, controllers, services, product HTTP client, JWT security, errors, tests, and Docker files

### Frontend generated by AI

- The frontend was scaffolded with the Sites React/Vinext template, then its starter screen was replaced
- All routes, shared auth state, header, API client, types, responsive styling, metadata, social preview, and Docker files were generated

### Root files generated by AI

- `docker-compose.yml`, `.env.example`, `.gitignore`
- `README.md`, `ARCHITECTURE.md`, and this explanation file
- MySQL database initialization SQL

### Submission stabilization changes

- Product service now uses Spring Boot 4's canonical `spring.mongodb.uri` setting, so Docker's `SPRING_MONGODB_URI` correctly points it to the `mongodb` container.
- Order service includes Spring Boot's REST client module so Spring can provide the `RestClient.Builder` used by `ProductClient`.
- Frontend internal links use normal browser navigation because the current Vinext beta's `next/link` runtime failed during local Docker navigation. No routes or page design changed.
- These fixes were verified with the complete Docker stack, backend integration tests, frontend lint/build, browser checks, and a register-to-cancel API journey that also checked stock and reviews.

You should still read the code and be able to trace one request end-to-end. In an interview, be transparent that AI helped generate code and explain how you verified it.

## Key Technical Concepts

Be ready to explain these points in your own words:

1. Why BCrypt hashes instead of encrypts passwords.
2. JWT structure, signing, expiry, and why the same secret must be configured across validating services.
3. How the security filter runs before controllers.
4. Why DTOs avoid returning password fields and protect entity design.
5. Controller/service/repository responsibilities.
6. JPA entity relationships between order and order items.
7. Why order items snapshot product name/price.
8. Why stock changes need locking and why cart addition does not reserve stock.
9. The weakness of synchronous cross-service inventory calls and how compensation helps.
10. Why reviews use MongoDB while money/orders use MySQL.
11. CORS: the browser origin must be allowed by each API.
12. Unit test versus integration test and why H2/mocks make tests repeatable.
13. Why the demo payment is safe for a university project but not a real payment integration.
14. How Docker Compose provides service names such as `mysql`, `mongodb`, and `product-service` on one network.

## Architecture Summary

> I built a small e-commerce system with separate auth, product, and order services. Auth hashes passwords with BCrypt and issues signed JWTs. Product service owns searchable catalog and locked stock updates in MySQL, while reviews are MongoDB documents. Order service scopes carts and history using the JWT email, snapshots price data, calls product service to reserve inventory, and compensates stock on failure/cancellation. The React frontend calls each REST API, and Docker Compose runs the services and databases together. I used automated integration tests for the important request flows and kept payments intentionally simulated so no real card data is handled.
