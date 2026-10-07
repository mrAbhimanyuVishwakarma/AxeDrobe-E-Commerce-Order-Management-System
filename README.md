# AxeDrobe - E-Commerce Order Management System

A full-stack fashion store built as a set of Spring Boot microservices with a React frontend. Customers can browse a catalog of ~70 products, sign in with Google, a one-time code or a password, check out with Cash on Delivery, and track or cancel their orders. Orders flow through Kafka so stock and notifications are handled by separate services.

**Live demo:** https://axedrobe.vercel.app

<img width="1250" height="703" alt="axedrobe_dark" src="https://github.com/user-attachments/assets/2389ee10-32e9-4587-b3f0-11d6212c1d91" />
<img width="1250" height="703" alt="axedrobe_light" src="https://github.com/user-attachments/assets/a8541d80-165b-4217-8710-54e842a16b27" />
<img width="966" height="596" alt="axedrobe_shop" src="https://github.com/user-attachments/assets/98db89d0-8ee5-4b7c-a492-6fac02d3a9f3" />
<img width="1004" height="408" alt="axedrobe_jcart" src="https://github.com/user-attachments/assets/a64efb2a-8909-4757-93a2-868f6abe487d" />

## Features

**Store**
- Catalog of men's, women's, ethnic wear, footwear and accessories with real photos, sizes, MRP/discounts, ratings and live stock
- Category pages, search, filters (gender, category, brand, price, discount) and sorting
- Bag with quantity controls, free delivery threshold and price breakdown
- Checkout with a delivery address and Cash on Delivery
- Order history, order tracking timeline and self-service cancellation
- Light and dark themes, responsive layout with a mobile bottom bar

**Accounts**
- Sign in with Google
- Passwordless sign in with a one-time code sent by email (Brevo API or SMTP) or SMS (2Factor.in or Fast2SMS)
- Email + password accounts (email verified with a code) and "forgot password"
- Rate limited codes (30 s resend cooldown, 5 sends per hour, 5 attempts per code)
- Admin role assigned by email address

**Backend**
- Five independent services, each with its own MongoDB database
- JWT authentication shared across services, role based access (customer/admin)
- Event driven stock handling: placing an order reserves stock, cancelling returns it
- Atomic stock updates that can never go negative
- Order confirmation and cancellation emails
- Swagger UI on every HTTP service, unit tests for the core logic

## Architecture

```text
                        React app (Vercel)
                               |
          +--------------------+--------------------+
          |                    |                    |
    User Service         Product Service       Order Service
    accounts, OTP,       catalog, stock        checkout, history,
    Google, JWT                 ^              tracking, cancel
          |                    |                    |
       MongoDB              MongoDB              MongoDB
                               |                    |
                               |          publishes order-created /
                               |               order-cancelled
                               |                    |
                               |                  Kafka
                               |                 /     \
                               |                v       v
                         Inventory Service   Notification Service
                         adjusts stock via   emails the customer
                         the product API
```

1. The customer signs in with the user service and receives a JWT.
2. The order service checks prices and stock with the product service, saves the order and publishes `order-created`.
3. The inventory service consumes the event and reduces stock through the product service's admin-only stock endpoint.
4. The notification service consumes the same event and emails a confirmation.
5. Cancelling publishes `order-cancelled`; inventory puts the stock back and the customer gets an email.

## Tech stack

| Layer | Technology |
| --- | --- |
| Frontend | React 19, Vite, React Router, Axios, Google Identity Services |
| Backend | Java 17, Spring Boot 3.5, Spring Security, Spring Data MongoDB, Spring Kafka, Spring Mail |
| Auth | JWT (jjwt), BCrypt, Google ID token verification |
| Database | MongoDB (Atlas in production) |
| Messaging | Apache Kafka (Aiven in production, KRaft mode locally) |
| API docs | springdoc OpenAPI / Swagger UI |
| Build & run | Maven, Docker, Docker Compose |
| Hosting | Vercel (frontend), Render (services) |

## Project structure

```text
.
├── backend/
│   ├── user-service/          # accounts, sign-in, JWT            :8081
│   ├── product-service/       # catalog and stock                 :8082
│   ├── order-service/         # checkout and orders               :8083
│   ├── inventory-service/     # Kafka consumer, stock updates     :8084
│   └── notification-service/  # Kafka consumer, emails            :8085
├── frontend/                  # React app                         :5173 (dev) / :8080 (docker)
├── docs/
│   └── E-Commerce-Postman-Collection.json
├── docker-compose.yml
├── .env.example
└── start.bat                  # Windows helper around docker compose
```

The starter catalog lives in `backend/product-service/src/main/resources/catalog.json` and is loaded automatically the first time the product service starts with an empty database.

## Running locally

### With Docker (recommended)

Requirements: Docker Desktop.

```bash
cp .env.example .env      # optional, every value has a working default
docker compose up -d --build
```

| URL | What |
| --- | --- |
| http://localhost:8080 | Store |
| http://localhost:8081/swagger-ui.html | User service API |
| http://localhost:8082/swagger-ui.html | Product service API |
| http://localhost:8083/swagger-ui.html | Order service API |

On Windows you can also double-click `start.bat`.

Locally `OTP_DEMO_MODE=true`, so one-time codes are shown on the sign-in screen instead of being emailed or texted. Add SMTP or SMS settings to `.env` to test real delivery.

To get an admin account, put your email in `ADMIN_EMAILS` and sign in with it.

### Without Docker

Requirements: Java 17, Maven, Node 20+, MongoDB on `localhost:27017` and Kafka on `localhost:9092`.

```bash
# in five terminals
cd backend/user-service && mvn spring-boot:run
cd backend/product-service && mvn spring-boot:run
cd backend/order-service && mvn spring-boot:run
cd backend/inventory-service && mvn spring-boot:run
cd backend/notification-service && mvn spring-boot:run

# frontend
cd frontend
npm install
npm run dev
```

## Configuration

All settings come from environment variables. Defaults are tuned for local development; the `prod` Spring profile (`SPRING_PROFILES_ACTIVE=prod`) removes defaults for secrets.

| Variable | Services | Description |
| --- | --- | --- |
| `MONGODB_URI` | user, product, order | MongoDB connection string. Each service uses its own database on the cluster. |
| `JWT_SECRET` | user, product, order, inventory | Shared token signing secret, at least 32 characters. |
| `KAFKA_BOOTSTRAP_SERVERS` | order, inventory, notification | Kafka brokers. |
| `KAFKA_CERT_PATH`, `KAFKA_KEY_PATH`, `KAFKA_CA_CERT_PATH` | order, inventory, notification (prod) | Client certificate, key and CA for Kafka over SSL (defaults to `/etc/secrets/...`). |
| `PRODUCT_SERVICE_URL` | order, inventory | Base URL of the product service. |
| `CORS_ALLOWED_ORIGINS` | user, product, order | Comma separated frontend origins, e.g. `https://axedrobe.vercel.app`. Defaults to `*`. |
| `ADMIN_EMAILS` | user | Emails that get the ADMIN role on sign in. |
| `GOOGLE_CLIENT_ID` | user | OAuth client ID for Sign in with Google. Leave empty to hide the button. |
| `OTP_DEMO_MODE` | user | `true` shows codes on screen. Never enable on a public deployment. |
| `MAIL_FROM` | user, notification | Sender address for codes and order emails. |
| `BREVO_API_KEY` | user, notification | Sends email through Brevo's HTTPS API. Use this on Render's free plan, which blocks SMTP. |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | user, notification | SMTP alternative to Brevo (local development or paid hosting). |
| `SMS_PROVIDER`, `SMS_API_KEY`, `SMS_TEMPLATE` | user | `twofactor` or `fast2sms` for SMS codes to Indian numbers. |
| `BRAND_NAME` | user, notification | Store name used in emails. |
| `STORE_URL` | notification | Link used in order emails. |
| `CATALOG_SEED` | product | Set to `false` to skip loading `catalog.json`. |

Frontend variables (Vite, set at build time): `VITE_USER_SERVICE_URL`, `VITE_PRODUCT_SERVICE_URL`, `VITE_ORDER_SERVICE_URL`, optional `VITE_WARMUP_URLS` and `VITE_BRAND_NAME`. See `frontend/.env.example`.

### Setting up sign-in providers

- **Google:** in Google Cloud console create an OAuth 2.0 Client ID of type *Web application*. Add your site (and `http://localhost:5173` for development) to *Authorized JavaScript origins*, then set `GOOGLE_CLIENT_ID` on the user service.
- **Email:** create a free [Brevo](https://www.brevo.com) account, verify your sender address, create an API key and set `BREVO_API_KEY` and `MAIL_FROM`. Render's free plan blocks outgoing SMTP, so the API is the reliable option there. Locally any SMTP account also works (for Gmail, create an App Password and use `smtp.gmail.com`, port `587`).
- **SMS:** create an account with [2Factor.in](https://2factor.in) or [Fast2SMS](https://www.fast2sms.com), then set `SMS_PROVIDER` and `SMS_API_KEY`.

## API overview

| Method | Endpoint | Access |
| --- | --- | --- |
| GET | `/api/auth/options` | public |
| POST | `/api/auth/otp/request` | public |
| POST | `/api/auth/otp/verify` | public |
| POST | `/api/auth/register` | public |
| POST | `/api/auth/login` | public |
| POST | `/api/auth/password/reset` | public |
| POST | `/api/auth/google` | public |
| GET / PATCH | `/api/users/me` | signed in |
| GET | `/api/products`, `/api/products/{id}` | public |
| POST / PUT / DELETE | `/api/products/**` | admin |
| PATCH | `/api/products/{id}/stock` | admin (used by the inventory service) |
| POST | `/api/orders` | signed in |
| GET | `/api/orders`, `/api/orders/{id or number}` | signed in (own orders) |
| POST | `/api/orders/{id}/cancel` | signed in (own orders) |
| PATCH | `/api/orders/{id}/status` | admin |
| GET | `/api/orders/admin/latest` | admin |

Errors always come back as `{"status": 400, "message": "..."}`. A Postman collection is in `docs/`.

## Testing

```bash
cd backend/<service> && mvn test
cd frontend && npm run lint && npm run build
```

## Deployment

The live setup uses free tiers:

- **Frontend - Vercel:** project root `frontend`, framework Vite. Set the `VITE_*` variables. `vercel.json` handles SPA routing.
- **Services - Render:** one Web Service per folder in `backend/`, using its Dockerfile. Set `SPRING_PROFILES_ACTIVE=prod` and the variables above. Upload the Kafka `service.cert`, `service.key` and `ca.pem` as Secret Files. Use `/actuator/health` as the health check path.
- **Database - MongoDB Atlas:** a free M0 cluster. Allow access from anywhere (Render has no fixed outbound IPs on the free plan) and use the connection string as `MONGODB_URI`.
- **Kafka - Aiven:** create the `order-created` and `order-cancelled` topics if auto-creation is disabled.

Free Render services sleep after 15 minutes without traffic, so the first request can take up to a minute. The frontend pings the services on page load to start them early (`VITE_WARMUP_URLS` can include the inventory and notification services).

## Rebranding

- Frontend name: `VITE_BRAND_NAME` (header, footer, sign-in), plus the title and meta tags in `frontend/index.html` and the static pages in `frontend/public/`.
- Email name: `BRAND_NAME` on the user and notification services.
- Logo and icons: `frontend/public/` (favicons and `site.webmanifest`).
- Products: edit `catalog.json` or manage them through the product API as an admin.
