# Alert API

Base path: `/api/alerts`

This API is used to list, create, query, and update alert status values.

---

## 1) Get All Alerts

**Endpoint**

`GET /api/alerts`

**Description**

Returns all alert records.

**Response**

```json
[
  {
    "id": 1,
    "transaction_id": 101,
    "rule_id": 2,
    "severity": "HIGH",
    "status": "OPEN"
  }
]
```

---

## 2) Get Alert by ID

**Endpoint**

`GET /api/alerts/{id}`

**Description**

Returns a single alert by ID.

**Responses**

- `200 OK` with the alert object
- `404 Not Found` if no alert exists

---

## 3) Get Open Alerts

**Endpoint**

`GET /api/alerts/open`

**Description**

Returns alerts whose status is open.

**Response**

```json
[
  {
    "id": 1,
    "transaction_id": 101,
    "rule_id": 2,
    "severity": "HIGH",
    "status": "OPEN"
  }
]
```

---

## 4) Create Alert

**Endpoint**

`POST /api/alerts`

**Description**

Creates a new alert record.

**Request Body**

```json
{
  "transactionId": 101,
  "ruleId": 2,
  "severity": "HIGH",
  "status": "OPEN"
}
```

**Response**

- `201 Created`
- Response body is the saved alert object

---

## 5) Update Alert Status

**Endpoint**

`PUT /api/alerts/{id}/status`

**Description**

Updates the status of an alert.

**Query Parameter**

- `status` — one value from `AlertStatus`

**Example**

```http
PUT /api/alerts/1/status?status=ACKNOWLEDGED
```

**Valid Status Values**

- `OPEN`
- `ACKNOWLEDGED`
- `INVESTIGATING`
- `CLOSED`
- `DISMISSED`

**Behavior**

- `200 OK` when update succeeds
- `400 Bad Request` for invalid transitions or invalid status values
- `404 Not Found` when alert does not exist

