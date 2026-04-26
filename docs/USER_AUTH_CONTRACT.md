# USER AUTH CONTRACT (C <-> A <-> D)

## 0) Source of truth
- All shared DTO/message classes must be created and used from:
  - `auction-common/src/main/java/com/auction/common/message/`
- Do not create duplicate Request/Response classes in `auction-client` or `auction-server`.

## 1) Actions
- `REGISTER`
- `LOGIN`
- `GET_AUCTIONS`, `GET_AUCTION`, `CREATE_AUCTION`
- `PLACE_BID`, `GET_BID_HISTORY`
- `GET_ITEMS`, `CREATE_ITEM`, `UPDATE_ITEM`, `DELETE_ITEM`

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

## 4.2) Handler response policy
- Server always replies with `ClientResponse` for every action.
- For actions not integrated yet, server returns:
  - `success=false`
  - `message="<ACTION> pending: <integration-name>"`
  - `data=null`

## 5) Minimum REGISTER test cases
1. Register BIDDER -> success
2. Register SELLER -> success
3. Register ADMIN -> fail
4. Register duplicate username -> fail

## 6) Realtime note (from lecturer guide)
- Observer + realtime update is a required part of the project.
- Socket-based realtime integration is expected in the later integration phase.
- Current phase focuses on stable request/response contract before wiring realtime push path.
