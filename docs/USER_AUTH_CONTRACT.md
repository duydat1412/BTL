# USER AUTH CONTRACT (C <-> A <-> D)

## 0) Source of truth (Task 3)
- All shared DTO/message classes must be created and used from:
  - `auction-common/src/main/java/com/auction/common/message/`
- Do not create duplicate Request/Response classes in `auction-client` or `auction-server`.

## 1) Actions
- `REGISTER`
- `LOGIN`

## 2) Request payload
- `REGISTER` -> `RegisterRequest`
  - `username: String`
  - `password: String` (plain text from client, server will hash)
  - `email: String`
  - `role: UserRole`
- `LOGIN` -> `LoginRequest`
  - `username: String`
  - `password: String`

## 3) Role rule for self-registration
- Allowed: `BIDDER`, `SELLER`
- Rejected: `ADMIN`
- Shared helper: `RegistrationPolicy.isSelfRegistrationAllowed(role)`

## 4) Response wrapper
- Server returns `ClientResponse`
  - `success: boolean`
  - `message: String`
  - `data: Serializable` (optional)

## 4.1) Request wrapper
- Client sends `ClientRequest`
  - `action: Action`
  - `payload: Serializable`

## 5) Minimum REGISTER test cases
1. Register BIDDER -> success
2. Register SELLER -> success
3. Register ADMIN -> fail
4. Register duplicate username -> fail
