# CHANGELOG — Cập nhật tài liệu theo hướng dẫn thầy

> **Ngày cập nhật**: 2026-04-14  
> **Lý do**: Đồng bộ plan + context với hướng dẫn chính thức của thầy (PDF)  
> **Files bị ảnh hưởng**: `plan.md`, `context_nguoi_A.md`, `context_nguoi_B.md`, `context_nguoi_C.md`, `context_nguoi_D.md`

---

## NHÓM 1 — Chỉnh lịch trình (8 tuần → 9 tuần, Tuần 6→15)

### Vấn đề
`plan.md` viết "8 tuần" bắt đầu từ Tuần 1. Thực tế hiện tại là **Tuần 6** của học kỳ, deadline **Tuần 15**.

### Thay đổi

| File | Thay đổi |
|------|----------|
| `plan.md` | Bảng "Lịch trình 8 tuần" (Tuần 1→8) → **"Lịch trình 9 tuần (Tuần 6→15)"** |
| `plan.md` | Toàn bộ cột "Tuần" trong bảng phân công → remap theo lịch mới |
| `context_nguoi_A.md` | Tuần 1→6, Tuần 2-3→7-8, Tuần 4-5→8-9, Tuần 6→9 |
| `context_nguoi_B.md` | Tuần 1-2→6-7, Tuần 2→7, Tuần 4→7, Tuần 4-5→7-8, Tuần 6→8 |
| `context_nguoi_C.md` | Tuần 1→6, Tuần 2-3→7-8, Tuần 3→8, Tuần 4-5→8-9, Tuần 5→9-10, Tuần 5-6→10-11 |
| `context_nguoi_D.md` | Tuần 2-3→7-8, Tuần 4-5→8-9, Tuần 6→9-10, Tuần 7→13-14 |

---

## NHÓM 2 — Thay đổi công nghệ (Spring Boot → Java Socket)

### Vấn đề
`plan.md` chỉ định **Spring Boot + REST API + WebSocket + H2/SQLite + DAO**. Thầy yêu cầu **Java Socket thuần + Serialization**.

### Thay đổi

| File | Cũ | Mới |
|------|-----|------|
| `plan.md` — Mermaid diagram | `REST API + WebSocket`, `DAO Layer`, `Database` | `SocketServer`, `Message Handlers`, `Repository (Serialization)`, `DataFile` |
| `plan.md` — Phân công Người A | `Spring Boot`, `REST API endpoints`, `WebSocket endpoint`, `H2/SQLite`, `DatabaseManager`, `DAO layer` | `ServerSocket`, `Socket message handlers`, `Realtime update qua Socket + Observer`, `Serialization (ObjectOutputStream/ObjectInputStream)`, `DataStore (Singleton)`, `Repository layer (Serialization)` |
| `plan.md` — Cấu trúc thư mục | `db/`, `dao/`, `controller/`, `websocket/` | `datastore/`, `repository/`, `handler/` |
| `context_nguoi_A.md` | Toàn bộ REST API table, WebSocket section, H2 Database + DAO section, Spring Boot config | Socket Message Handlers table, Realtime Updates qua Socket, DataStore + Repository (Serialization), ServerSocket + Thread per client |
| `context_nguoi_C.md` | `NetworkClient` gọi REST API + lắng nghe WebSocket, dùng `HttpClient` | `NetworkClient` dùng `Socket` + `ObjectOutputStream` gửi request, `ObjectInputStream` nhận response |

---

## NHÓM 3 — Di chuyển Observer sớm hơn (Tuần 4 → Tuần 7)

### Vấn đề
Observer Pattern đặt ở Tuần 4-5. Thầy yêu cầu hoàn thành ở **Tuần 7**.

### Thay đổi

| File | Thay đổi |
|------|----------|
| `plan.md` — Bảng phân công Người B | `Observer Pattern ... \| Tuần 4` → `Observer Pattern ... \| Tuần 7` + ghi chú ƯU TIÊN CAO |
| `plan.md` — Bảng lịch trình | Observer chuyển sang cột Tuần 7 |
| `context_nguoi_B.md` | Observer từ bảng Design Patterns: deadline → **Tuần 7 (ƯU TIÊN CAO)** |
| `context_nguoi_B.md` | Thêm warning block: "⚠️ ƯU TIÊN CAO: Phải hoàn thành Observer Pattern trong Tuần 7 theo yêu cầu thầy." |

---

## NHÓM 4 — Thêm yêu cầu mới từ thầy

### Vấn đề
Một số yêu cầu trong hướng dẫn thầy chưa xuất hiện trong plan.

### Thay đổi

| Yêu cầu mới | File | Người | Tuần |
|-------------|------|-------|------|
| **Custom Exceptions**: `InvalidBidException`, `AuctionClosedException`, `AuthenticationException` | `plan.md` (Người D), `context_nguoi_D.md` | D | 8 |
| **Checkstyle + Maven**: Enforce Google Java Style tự động | `plan.md` (Người A), `context_nguoi_A.md` | A | 9 |
| **Code coverage ≥ 60%** + JaCoCo plugin | `plan.md` (Người B/D), `context_nguoi_B.md`, `context_nguoi_D.md` | B + D | 10 |
| **README.md đầy đủ**: hướng dẫn cài đặt, chạy Server/Client | `plan.md` (Người A), `context_nguoi_A.md` | A | 13-14 |
| **Tự học JavaFX + SceneBuilder** | `context_nguoi_C.md` | C | 6 |
| **Tự học Socket client-side** | `context_nguoi_C.md` | C | 9 |
| **Tự học Socket server-side + Serialization** | `context_nguoi_A.md` | A | 9 |
| **Entity implements Serializable** | `context_nguoi_B.md` | B | 6-7 |

---

## Tóm tắt tổng quan

```
✅ Lịch trình: 8 tuần (Tuần 1-8) → 9 tuần (Tuần 6-15)
✅ Công nghệ: Spring Boot + REST + WebSocket + H2 + DAO → Java Socket + Serialization + Repository
✅ Observer Pattern: Tuần 4 → Tuần 7 (ưu tiên cao)
✅ Custom Exceptions: InvalidBidException, AuctionClosedException, AuthenticationException (Tuần 8)
✅ Checkstyle + Maven: Google Java Style enforcement (Tuần 9)
✅ Code coverage: ≥ 60% với JaCoCo (Tuần 10)
✅ README.md: Hướng dẫn đầy đủ (Tuần 13-14)
✅ Tự học: JavaFX (C-Tuần 6), Socket client (C-Tuần 9), Socket server + Serialization (A-Tuần 9)
```
