## Transaction API

Base path: `/transactions`

This API provides transaction creation, retrieval, and filtering by amount/time range.

---

### 1) Create Transaction

**Endpoint**

`POST /transactions`

**Description**

Creates a new transaction record. The request body is validated with `@Valid`.

**Request Body**

```json
{
  "accountId": "A1001",
  "payeeId": "P2001",
  "amount": 120.00,
  "currency": "USD",
  "transType": "DEBIT",
  "transTimestamp": "2026-07-20T09:00:00",
  "description": "sample transaction",
  "status": "Approved"
}
```

**Field Notes**

- `accountId`: required, non-blank
- `payeeId`: required, non-blank
- `amount`: required, decimal with up to 18 digits and 2 fraction digits
- `currency`: required
- `transType`: required
- `transTimestamp`: required
- `description`: optional
- `status`: optional in request, but commonly set by backend flow

**Response**

Plain text string:

- `add transaction success`
- `add transaction false`

---

### 2) Get All Transactions

**Endpoint**

`GET /transactions`

**Description**

Returns all stored transactions.

**Response Body**

```json
[
  {
	"id": 1,
	"accountId": "A1001",
	"payeeId": "P2001",
	"amount": 120.00,
	"currency": "USD",
	"transType": "DEBIT",
	"transTimestamp": "2026-07-20T09:00:00",
	"description": "sample transaction",
	"status": "Approved",
	"createdAt": "2026-07-20T09:00:00"
  }
]
```

---

### 3) Get Transaction by ID

**Endpoint**

`GET /transactions/{id}`

**Description**

Returns a single transaction by its database ID.

**Path Parameters**

- `id` (Long)

**Response Body**

```json
{
  "id": 1,
  "accountId": "A1001",
  "payeeId": "P2001",
  "amount": 120.00,
  "currency": "USD",
  "transType": "DEBIT",
  "transTimestamp": "2026-07-20T09:00:00",
  "description": "sample transaction",
  "status": "Approved",
  "createdAt": "2026-07-20T09:00:00"
}
```

---

### 4) Filter Transactions by Amount and Time Range

**Endpoint**

`GET /transactions/range-filter`

**Description**

Filters transactions by `amount` range and `transTimestamp` range.

**Query Parameters**

- `minAmount` — minimum amount, required
- `maxAmount` — maximum amount, required
- `startTime` — range start, ISO datetime, required
- `endTime` — range end, ISO datetime, required

**Example Request**

```http
GET /transactions/range-filter?minAmount=100.00&maxAmount=500.00&startTime=2026-07-20T00:00:00&endTime=2026-07-22T23:59:59
```

**Validation Rules**

- `minAmount` must be less than or equal to `maxAmount`
- `startTime` must be less than or equal to `endTime`

If invalid, the API returns `400 Bad Request`.

**Response Body**

List of matching transactions.

---

### Error Notes

- Validation errors from `@Valid` may return `400 Bad Request`
- Missing transaction rows may return an empty list or `null` for a single ID lookup depending on repository behavior


