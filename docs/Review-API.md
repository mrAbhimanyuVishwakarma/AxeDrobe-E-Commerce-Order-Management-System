# Review API

Base URL: order service (`http://localhost:8083` locally).

## List recent verified reviews

`GET /api/reviews/latest?limit=6`

- Public.
- `limit` is clamped to 1–20.
- Returns reviews newest-updated first.

## Create or update a review

`POST /api/reviews`

Requires a bearer token. The authenticated customer must own the order, the order must be `DELIVERED`, and the product must be an item in that order. A customer has one review per product; posting again updates it.

```json
{
  "orderId": "<order id>",
  "productId": "<product id>",
  "rating": 5,
  "comment": "Comfortable fit and the fabric feels excellent."
}
```

Validation: rating 1–5; comment 10–1000 characters. Errors use the project-wide `{ "status": 400, "message": "..." }` shape.
