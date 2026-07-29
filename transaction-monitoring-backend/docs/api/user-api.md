# User API

Base path: `/api/user`

This API is used to create a user/account mapping entry.

---

## 1) Add User

**Endpoint**

`POST /api/user/add/user`

**Description**

Creates a user record using request parameters.

**Request Parameters**

- `userName` — string
- `accountNo` — string

**Example**

```http
POST /api/user/add/user?userName=Alice&accountNo=ACC001
```

**Response**

Plain text:

- `add user successful`

