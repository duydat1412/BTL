# Phân tích Cốt lõi Kiến trúc Dự án (Deep Dive)

Hệ thống Đấu giá (Auction System) này được xây dựng theo mô hình **Client-Server** truyền thống, giao tiếp bằng **Java Socket** và lưu trữ dữ liệu bằng **Serialization**. Dự án được chia thành 3 module chính (multi-module Maven).

Dưới đây là giải phẫu chi tiết vai trò của từng thư mục và từng file để bạn nắm vững bản chất hệ thống.

---

## 1. Module `auction-common` (Bộ Khung Xương)
> **Bản chất:** Đây là nơi chứa những gì "chung nhất" mà cả Client và Server đều cần biết. Client cần biết để gói hàng gửi đi, Server cần biết để mở hộp hàng ra đọc. Không chứa logic nghiệp vụ phức tạp.

* **`com.auction.common.entity.*`**
  * *Vai trò:* Định nghĩa các đối tượng thực tế (OOP).
  * `User.java` (Cha), `Seller/Bidder/Admin.java` (Con): Thể hiện người dùng.
  * `Item.java` (Cha), `Electronics/Art/Vehicle.java` (Con): Thể hiện sản phẩm mang đi đấu giá.
  * `Auction.java`: Đại diện cho một phiên đấu giá (có Start Time, End Time, Current Price).
  * `BidTransaction.java`: Ghi nhận mỗi lần có người vung tiền đặt giá.
* **`com.auction.common.message.*`**
  * *Vai trò:* "Phong bì thư" để Client và Server gửi cho nhau qua Socket.
  * `ClientRequest.java`: Phong bì chiều đi (Client -> Server). Chứa `Action` (mã lệnh) và `payload` (dữ liệu).
  * `ClientResponse.java`: Phong bì chiều về (Server -> Client). Chứa `isSuccess` (thành công không), `message` (thông báo) và `data` (dữ liệu trả về).
  * Các file `...Request.java` (VD: `RegisterRequest`, `CreateItemRequest`): Chính là nội dung bức thư (payload) nằm trong phong bì `ClientRequest`.
* **`com.auction.common.enums.*`**
  * *Vai trò:* Các hằng số phân loại.
  * `Action.java`: Danh sách các lệnh mà Server hiểu (VD: `LOGIN`, `CREATE_ITEM`, `PLACE_BID`).
  * `UserRole.java`, `ItemType.java`, `AuctionStatus.java`: Phân loại trạng thái, vai trò.
* **`com.auction.common.strategy.*` / `factory.*` / `observer.*`**
  * *Vai trò:* Chứa các Design Pattern áp dụng vào hệ thống.
  * (Người B chịu trách nhiệm xây dựng các pattern này).

---

## 2. Module `auction-server` (Bộ Não & Trái Tim)
> **Bản chất:** Nơi xử lý toàn bộ logic nghiệp vụ (kiểm tra tính hợp lệ, tính toán, phân quyền) và lưu trữ dữ liệu. Nó lắng nghe và phục vụ Client liên tục.

* **`com.auction.server.AuctionServerApp.java`**
  * *Vai trò:* Cửa ngõ. Nó mở `ServerSocket` ở Port 8080. Cứ có 1 Client kết nối, nó sẽ quăng Client đó cho một `Thread` (luồng) riêng để xử lý, bản thân nó lại đứng canh cửa tiếp.

* **`com.auction.server.handler.ClientHandler.java`**
  * *Vai trò:* Lễ tân. Nó nhận `ClientRequest` từ Client, bóc phong bì ra xem `Action` là gì. Dựa vào vòng `switch (action)`, nó sẽ điều hướng việc cho các Service tương ứng.

* **`com.auction.server.service.*` (Core Logic - Quan trọng nhất)**
  * *Vai trò:* Bộ não (Business Logic). Nơi chứa các quy tắc kinh doanh.
  * `UserService.java`: Xử lý đăng nhập, băm mật khẩu (BCrypt), kiểm tra định dạng email/password.
  * `ItemService.java`: Kiểm tra xem tên sản phẩm có rỗng không, giá có hợp lệ không trước khi cho tạo mới.
  * `BidService.java`: (Sắp tới) Xử lý tranh chấp khi 2 người cùng đặt giá 1 lúc (dùng `ReentrantLock` để khóa, đảm bảo ai đến trước được trước).

* **`com.auction.server.repository.*` (Data Access Layer)**
  * *Vai trò:* Cánh tay phải của Service. Service chỉ suy nghĩ, còn Repository là người trực tiếp "chạm" vào dữ liệu (RAM) để thêm, sửa, xóa, tìm kiếm (CRUD).
  * File tiêu biểu: `SerializableItemRepository`, `SerializableUserRepository`.

* **`com.auction.server.datastore.DataStore.java`**
  * *Vai trò:* Cái kho lưu trữ thực sự (In-Memory Singleton).
  * Nó chứa các List như `List<User>`, `List<Item>` đang chạy trên RAM.
  * **Đặc biệt:** Nó chứa hàm `saveData()` để "chụp ảnh" toàn bộ các List trên RAM rồi ghi đè xuống file `.dat` (Serialization). Nhờ file này mà cúp điện server không mất dữ liệu.

---

## 3. Module `auction-client` (Giao Diện Chạm)
> **Bản chất:** Nơi hiển thị cái đẹp cho người dùng xem và bấm. Nó không tự suy nghĩ (logic), nó chỉ đóng gói ý định của người dùng gửi cho Server, rồi lấy kết quả Server trả về để hiển thị lên màn hình.

* **`com.auction.client.AuctionClientApp.java`**
  * *Vai trò:* Chạy cửa sổ JavaFX đầu tiên (thường là màn hình Login).

* **`com.auction.client.network.NetworkClient.java`**
  * *Vai trò:* Người đưa thư (Singleton). Nó chứa cái `Socket` kết nối tới port 8080 của Server. 
  * Cung cấp hàm `sendRequest(ClientRequest)` để gửi phong bì qua `ObjectOutputStream` và nhận `ClientResponse` từ `ObjectInputStream`.

* **`com.auction.client.controller.*`**
  * *Vai trò:* Lắng nghe người dùng bấm nút trên màn hình nào thì Controller đó xử lý.
  * `LoginController.java` / `RegisterController.java`: Lấy text từ các ô nhập liệu (username, password), gọi `NetworkClient` gửi đi, chờ kết quả rồi `setText()` báo lỗi hay báo thành công lên màn hình.
  * `SellerDashboardController.java`: Màn hình quản lý sản phẩm. Sẽ hiển thị danh sách, có nút Update, Delete.

* **`src/main/resources/view/*.fxml`**
  * *Vai trò:* File thiết kế giao diện bằng XML (chỉnh bằng Scene Builder). Kéo thả nút, canh chỉnh màu sắc tọa độ.

---

## 🚀 Tóm tắt Luồng Chạy Bản Chất (Essence)

1. **Client** (Màn hình `login.fxml` + `LoginController`) lấy chuỗi từ người dùng gõ.
2. Gói vào `LoginRequest` (nằm trong `common`).
3. Nhét `LoginRequest` vào phong bì `ClientRequest(Action.LOGIN)`.
4. **Mã hóa (Serialization)** rồi gửi qua mạng (Socket).
5. **Server** (`AuctionServerApp`) nhận được, đẩy qua cho `ClientHandler`.
6. `ClientHandler` rạch phong bì, thấy `LOGIN` -> gọi `UserService.login()`.
7. `UserService` vào kho `DataStore` (thông qua `UserRepository`) tìm tài khoản, kiểm tra đúng sai.
8. Trả kết quả bọc vào `ClientResponse`.
9. Mã hóa gửi về lại Socket.
10. `LoginController` bên Client nhận `ClientResponse`, phân tích chữ "Thành công" để hiển thị báo cáo. Khép kín 1 chu trình!
