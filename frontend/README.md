# AxeDrobe Frontend

React 19 + Vite storefront for the AxeDrobe services. See the [main README](../README.md) for the full project.

## Scripts

```bash
npm install
npm run dev       # http://localhost:5173
npm run lint
npm run build     # production build in dist/
npm run preview   # serve the production build
```

## Configuration

Copy `.env.example` to `.env.local` and point the app at the services. Without it, it uses `localhost:8081-8083`.

| Variable | Purpose |
| --- | --- |
| `VITE_USER_SERVICE_URL` | User service (sign-in, profile) |
| `VITE_PRODUCT_SERVICE_URL` | Product service (catalog) |
| `VITE_ORDER_SERVICE_URL` | Order service (checkout, orders) |
| `VITE_WARMUP_URLS` | Optional extra health URLs pinged on load |
| `VITE_BRAND_NAME` | Optional store name |

Google sign-in needs no frontend setting: the client ID is read from the user service (`/api/auth/options`).

## Layout

```text
src/
├── components/   # header, footer, product card, order timeline...
├── context/      # auth (JWT), bag, theme, toasts
├── hooks/        # catalog loading, quick add to bag
├── lib/          # API clients, formatting, catalog helpers
└── pages/        # one file per route
```

`vercel.json` rewrites every path to `index.html` so client-side routes work on refresh.
