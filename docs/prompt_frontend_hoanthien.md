# Prompt: Hoàn thiện Frontend (JavaFX) - Auction System

## 📋 Tổng quan dự án

Hệ thống **Đấu giá trực tuyến (Online Auction System)** — bài tập lớn môn Lập trình Nâng cao (Java OOP).  
Kiến trúc **Client-Server** giao tiếp qua **Java Socket + ObjectStream**.  
Đã hoàn thành **100% Backend**, cần hoàn thiện **Frontend (JavaFX)**.

## 🏗 Kiến trúc

```
Client (JavaFX + FXML + CSS)
    │ NetworkClient (Singleton, Socket)
    ▼
Server (Socket port 8080)
    │ ClientHandler → Service → Repository → DataStore (Serialization)
    ▼
File .dat (lưu data)
```

**Giao tiếp:** Client gửi `ClientRequest(action, payload)` → Server trả `ClientResponse(success, message, data)`  
**Push realtime:** Server tự gửi `ServerPushMessage(type, data, message)` xuống client qua Socket (listener thread).

## 🧩 Tech Stack

- Java 17+
- JavaFX (FXML + Scene Builder)
- Maven multi-module
- CSS (đã có file style.css theme sẵn)
- Giao tiếp: `java.net.Socket` + `ObjectInputStream/ObjectOutputStream` (Serialization)

## 📁 Cấu trúc thư mục Frontend

```
auction-client/src/main/
├── java/com/auction/client/
│   ├── AuctionClientApp.java              ← JavaFX Application main
│   ├── controller/
│   │   ├── LoginController.java
│   │   ├── RegisterController.java
│   │   ├── AuctionListController.java
│   │   ├── AuctionDetailController.java
│   │   ├── SellerDashboardController.java
│   │   ├── CreateItemController.java
│   │   └── AdminController.java
│   └── network/
│       └── NetworkClient.java             ← Socket Singleton + Push Listener
└── resources/
    ├── view/                              ← FXML files
    │   ├── login.fxml
    │   ├── register.fxml
    │   ├── auction_list.fxml
    │   ├── auction_detail.fxml
    │   ├── seller_dashboard.fxml
    │   ├── create_item.fxml
    │   └── admin.fxml
    └── CSS/
        └── style.css                      ← Theme CSS
```

## 📦 Common classes (trong `auction-common` — chỉ DÙNG, không sửa)

```java
// Entity
com.auction.common.entity.User, Bidder, Seller, Admin
com.auction.common.entity.Item, Electronics, Art, Vehicle
com.auction.common.entity.Auction            // getId(), getTitle(), getCurrentPrice(), getStartPrice(), getStatus(), getStartTime(), getEndTime(), getItemId(), getSellerId()
com.auction.common.entity.BidTransaction     // getId(), getAuctionId(), getBidderId(), getAmount(), getTimestamp()

// Enums
com.auction.common.enums.AuctionStatus       // OPEN, RUNNING, FINISHED, PAID, CANCELED
com.auction.common.enums.UserRole            // BIDDER, SELLER, ADMIN
com.auction.common.enums.ItemType            // ELECTRONICS, ART, VEHICLE

// Message
com.auction.common.message.ClientRequest     // new ClientRequest(Action, Serializable payload)
com.auction.common.message.ClientResponse    // isSuccess(), getMessage(), getData()
com.auction.common.message.ServerPushMessage // getType(), getData(), getMessage()
  ServerPushMessage.PushType: NEW_BID, PRICE_UPDATE, AUCTION_ENDED, AUCTION_STARTED, AUCTION_CREATED

// Request payloads (cần import để gửi)
com.auction.common.message.*
```

## 🔧 NetworkClient API (cách gọi server)

```java
NetworkClient client = NetworkClient.getInstance();
client.connect();  // kết nối Socket + khởi động listener thread

// Gửi request bất đồng bộ (không block UI thread)
client.sendRequestAsync(new ClientRequest(Action.LOGIN, payload))
    .thenAccept(res -> Platform.runLater(() -> {
        // res.isSuccess(), res.getMessage(), res.getData()
    }));

// Push listener (nhận realtime từ server)
client.addPushListener(pushMsg -> {
    if (pushMsg.getType() == ServerPushMessage.PushType.NEW_BID) {
        // pushMsg.getData() = Auction (cập nhật)
    }
});

// Lấy user hiện tại
client.getCurrentUser();  // trả AuthUserData (getUserId(), getUsername(), getRole())
```

## 🚀 Các Action có sẵn (dùng `Action.XXX`)

| Action | Payload | Response data |
|--------|---------|---------------|
| REGISTER | RegisterRequest | null |
| LOGIN | LoginRequest | AuthUserData |
| GET_AUCTIONS | GetAuctionsRequest(sellerId, statusFilter) | List<Auction> |
| GET_AUCTION | auctionId (String) | Auction |
| CREATE_AUCTION | CreateAuctionRequest | Auction |
| CANCEL_AUCTION | CancelAuctionRequest(auctionId, adminId, reason) | Auction |
| PLACE_BID | PlaceBidRequest(auctionId, bidderId, amount, isAutoBid) | BidTransaction |
| GET_BID_HISTORY | GetBidHistoryRequest(auctionId) | List<BidTransaction> |
| GET_ITEMS | GetItemsRequest(sellerId, itemType) | List<Item> |
| CREATE_ITEM | CreateItemRequest(name, desc, price, sellerId, type, extraAttrs) | Item |
| UPDATE_ITEM | UpdateItemRequest(itemId, name, desc, price, type, sellerId, extraAttrs) | Item |
| DELETE_ITEM | DeleteItemRequest(itemId, sellerId) | null |
| GET_USERS | GetAllUsersRequest(adminId) | List<User> |
| BAN_USER | BanUserRequest(adminId, targetUserId) | null |
| UNBAN_USER | UnbanUserRequest(adminId, targetUserId) | null |
| REGISTER_AUTO_BID | RegisterAutoBidRequest(auctionId, bidderId, maxBid, increment) | null |
| REMOVE_AUTO_BID | RemoveAutoBidRequest(auctionId, bidderId) | null |

---

## ✅ YÊU CẦU CẦN LÀM

### 1. Login Screen — Thêm nút "Hiện mật khẩu"

**File:** `LoginController.java` + `login.fxml`

**Yêu cầu:**
- Thêm một nút (button) bên cạnh ô password, có biểu tượng eye 👁
- Khi click: chuyển PasswordField → TextField để hiện password (và ngược lại)
- Có thể dùng `StackPane` để overlay nút lên góc phải của ô password
- Gợi ý: thêm `CheckBox` với text "Hiện mật khẩu" nếu không muốn dùng icon

### 2. Login Screen — Enter key để submit

**Yêu cầu:**
- Khi focus đang ở ô password và nhấn Enter → tự động gọi `handleLogin()`
- Tương tự, khi focus ở ô username và nhấn Enter → chuyển xuống ô password

### 3. Register Screen — Confirm password

**File:** `RegisterController.java` + `register.fxml`

**Yêu cầu:**
- Thêm ô "Nhập lại mật khẩu" (PasswordField)
- Kiểm tra 2 ô password khớp nhau trước khi gửi request
- Nếu không khớp, hiển thị lỗi "Mật khẩu không khớp!"
- Thêm nút "Hiện mật khẩu" cho cả 2 ô password (dùng CheckBox hoặc nút eye)
- Enter key ở ô cuối cùng gọi `handleRegister()`

### 4. Register Screen — Strength indicator

**Yêu cầu:**
- Khi người dùng gõ password, hiển thị thanh/ label độ mạnh password:
  - Yếu (đỏ): < 6 ký tự
  - Trung bình (vàng): >= 6 ký tự, có chữ hoa hoặc số
  - Mạnh (xanh): >= 8 ký tự, có chữ hoa + số + ký tự đặc biệt

### 5. Auction List — Tab "Phiên đã thắng" cho Bidder

**File:** `AuctionListController.java` + `auction_list.fxml`

**Yêu cầu:**
- Thêm TabPane hoặc 2 button toggle: "Đang diễn ra" | "Đã thắng"
- "Đã thắng": gọi `GET_AUCTIONS` với status filter, lọc các auction có status = FINISHED, kiểm tra bidder hiện tại có BidTransaction nào là winner không
  - Cách đơn giản: gọi `GET_AUCTIONS` → lấy danh sách FINISHED → gọi `GET_BID_HISTORY` cho từng auction → kiểm tra bid cuối cùng có phải của user không
  - Cách tối ưu: để đơn giản, có thể tự suy luận: auction FINISHED mà user có bid trong lịch sử và là người cuối cùng đặt giá thì coi như thắng
- Hiển thị danh sách các auction đã thắng kèm giá thắng

### 6. Auction Detail — Countdown timer realtime

**File:** `AuctionDetailController.java`

**Yêu cầu:**
- Dùng `java.util.Timer` hoặc `javafx.animation.Timeline` để cập nhật thời gian còn lại mỗi giây
- Hiển thịnh dạng: "Còn lại: 02:30:15" (hh:mm:ss)
- Khi hết giờ: tự động hiển thị "Đã kết thúc" và disable nút đặt giá
- Màu sắc: xanh (>1h), vàng (<1h), đỏ (<5 phút)
- Khi server push `AUCTION_ENDED` → ngay lập tức báo kết thúc

### 7. Auction Detail — Lịch sử bid (Bid History)

**File:** `AuctionDetailController.java` + `auction_detail.fxml`

**Yêu cầu:**
- Thêm ListView hoặc TableView bên dưới phần chi tiết
- Khi vào trang, gọi `GET_BID_HISTORY` với auctionId
- Hiển thị: số thứ tự, người đặt, số tiền, thời gian
- Khi nhận push `NEW_BID` → tự động thêm bid mới vào đầu danh sách
- Highlight bid của user hiện tại (màu vàng)
- Format: "1. user_A: 500,000 VNĐ - 14/05 15:30:22"

### 8. Auction Detail — Nút Auto-bid

**File:** `AuctionDetailController.java` + `auction_detail.fxml`

**Yêu cầu:**
- Thêm 1 ô nhập "Giá tối đa (max bid)" + nút "Bật auto-bid"
- Khi click: gọi `REGISTER_AUTO_BID` với auctionId, bidderId, maxBid, increment=500
- Nếu đã đăng ký auto-bid: hiển thị nút "Hủy auto-bid" gọi `REMOVE_AUTO_BID`
- Backend đã hỗ trợ sẵn, chỉ cần gọi action

### 9. Seller Dashboard — Nút sửa / xóa sản phẩm

**File:** `SellerDashboardController.java` + `seller_dashboard.fxml`

**Yêu cầu:**
- Khi click vào 1 item trong ListView → hiện dialog hoặc form sửa
- Form sửa: cho phép đổi tên, giá, mô tả, loại
- Gọi `UPDATE_ITEM` để lưu
- Nút "Xóa" (màu đỏ) bên cạnh mỗi item → confirm dialog → gọi `DELETE_ITEM`
- Sau khi sửa/xóa → refresh danh sách

### 10. Admin Panel — Hoàn thiện toàn bộ

**File:** `AdminController.java` + `admin.fxml`

**Yêu cầu:**
Phải có 3 tab chính:

**Tab 1: Dashboard (Thống kê)**
- Tổng số user, số seller, số bidder
- Tổng số auction đang chạy, đã kết thúc
- Tổng số item
Gọi: `GET_USERS`, `GET_AUCTIONS`, `GET_ITEMS`

**Tab 2: Quản lý User**
- TableView hiển thị tất cả users: username, email, role, trạng thái (banned hay không)
- Nút "Ban" / "Unban" bên cạnh mỗi user
- Gọi: `GET_USERS`, `BAN_USER`, `UNBAN_USER`

**Tab 3: Quản lý Auction**
- TableView hiển thị tất cả auctions
- Nút "Hủy phiên" (Cancel) cho auction đang chạy
- Gọi: `GET_AUCTIONS`, `CANCEL_AUCTION`

### 11. UX chung — Toast notification

**File:** Tạo mới `util/NotificationToast.java` hoặc thêm trong từng controller

**Yêu cầu:**
- Khi nhận push `NEW_BID` ở Auction List, hiển thị toast nhỏ góc phải màn hình: "Có lượt đặt giá mới!"
- Khi `AUCTION_ENDED`: toast "Phiên đấu giá X đã kết thúc!"
- Dùng `Platform.runLater()` và `Timeline` để tự động ẩn sau 3s

### 12. UX chung — Confirm dialog

**Yêu cầu:**
- Khi xóa item / cancel auction / logout: hiển thị Alert confirm
- Dùng `Alert(AlertType.CONFIRMATION)` với nội dung phù hợp
- Nếu user chọn OK mới thực thi hành động

---

## 🎯 MỤC TIÊU

Hoàn thiện frontend để demo được các luồng sau:

**Bidder flow:** Login → Xem danh sách đấu giá → Xem chi tiết + đặt giá → Xem bid history + auto-bid → Xem phiên đã thắng

**Seller flow:** Login → Dashboard → Tạo sản phẩm → Xem danh sách sản phẩm → Sửa/xóa sản phẩm

**Admin flow:** Login → Xem thống kê → Quản lý user (ban/unban) → Quản lý auction (hủy phiên)

## ⚠️ LƯU Ý QUAN TRỌNG

1. **Luôn import từ `com.auction.common.*`** — không tạo class trùng trong client
2. **Cập nhật UI bằng `Platform.runLater()`** — tuyệt đối không sửa UI từ thread khác
3. **Gọi server async** — dùng `NetworkClient.sendRequestAsync()` để không block UI
4. **Unregister PushListener** khi rời trang (trong `goBack()` hoặc listener cũ)
5. **CSS class sẵn có:** `.btn-gold`, `.btn-outline`, `.input-field`, `.card`, `.text-danger`, `.text-gold`, `.text-muted`, `.bg-white`, `.text-sm`, `.text-lg`, `.text-xl`, `.text-2xl`, `.text-3xl`, `.font-bold`
6. **Format tiền:** dùng `String.format("%,.0f VNĐ", price)` cho VNĐ
7. **Format ngày giờ:** dùng `DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")`
8. **Chỉ sửa file trong `auction-client/`** — không sửa `auction-common` hoặc `auction-server`
9. **Test build:** kiểm tra `mvn compile -pl auction-client -am` trước khi kết luận
