# Bối cảnh dự án & Tiến độ công việc (Project Context for Copilot)

**Dự án**: BidVault - Hệ thống đấu giá trực tuyến (Java Sockets + Serialization, JavaFX).
**Cấu trúc dự án**: Multi-module Maven (`auction-server`, `auction-client`, `auction-common`).

## 1. Giao diện (JavaFX Client)
- **Thiết kế**: Đã chuyển đổi hoàn toàn thiết kế từ một giao diện React + Tailwind CSS sang JavaFX. Sử dụng Global CSS (`d:\BTL\auction-client\src\main\resources\css\style.css`) để tạo giao diện hiện đại (glassmorphism, gradient background, border-radius, hover effects).
- **Các màn hình FXML (nằm trong `resources/view/`)**:
  - `login.fxml` / `register.fxml`: Giao diện chia đôi màn hình (Split screen), trái là gradient giới thiệu, phải là form auth.
  - `auction_list.fxml` (Bảng điều khiển người mua): Có Navbar trên cùng, Hero section, và `ListView` hiển thị danh sách sản phẩm.
  - `auction_detail.fxml`: Màn hình chi tiết sản phẩm. Có hình ảnh (placeholder), tên, mô tả chi tiết đa hình, giá hiện tại, thời gian đếm ngược và nút **Place Bid**.
  - `seller_dashboard.fxml`: Quản lý sản phẩm cho người bán. Đã tách form tạo sản phẩm ra thành nút "+ Tạo phiên đấu giá" để chuyển sang màn hình riêng.
  - `create_item.fxml`: Màn hình chuyên dụng cho Seller tạo sản phẩm. Hỗ trợ chọn ảnh (bằng `FileChooser`), nhập giá, chuyên mục, mô tả chi tiết, và **nhập số phút diễn ra đấu giá** (duration).
  - `admin.fxml`: Màn hình thống kê cơ bản.

## 2. Xử lý Logic Client (Java Controllers)
- `AuctionListController.java`: Đã bổ sung sự kiện `setOnMouseClicked` (double click) trên `ListView` để chuyển sang trang chi tiết (`AuctionDetailController`). Vì server chưa hoàn thiện API lấy trực tiếp Auction, Client hiện đang tạm parse từ `Item` sang `Auction` ảo để hiển thị detail.
- `AuctionDetailController.java`: Lấy `DetailedDescription` (đa hình) từ `Item`. Khi ấn "Đặt giá" (Place Bid), đã code logic sử dụng `NetworkClient` gửi `PlaceBidRequest` qua Sockets đến Server thực sự thay vì chỉ in ra console.
- `CreateItemController.java`: Lấy thông tin form tạo sản phẩm, nhét `durationMinutes` (số phút đấu giá) và `imagePath` vào `extraAttributes` của `CreateItemRequest` để gửi lên server.
- `SellerDashboardController.java`: Gọi `GetItemsRequest` để lấy danh sách sản phẩm theo `sellerId` và xử lý chuyển hướng.

## 3. Server Logic (Java Sockets)
- `ItemService.java`: Hàm `C()` (Create Item) đã được nâng cấp. Khi nhận được `CreateItemRequest`, Server không chỉ lưu `Item` xuống `SerializableItemRepository` mà còn **tự động tạo một đối tượng `Auction`**, đặt `startTime = now` và `endTime = now + durationMinutes`. Sau đó lưu `Auction` vào DB.
- `AuctionScheduler.java`: Đã được cài đặt dùng `ScheduledExecutorService`. Hàm `scheduleAuctionEnd(auctionId, durationMinutes)` được gọi ngay trong `ItemService`. Bộ đếm sẽ đếm ngược số phút tương ứng và đổi trạng thái của Auction sang `FINISHED` một cách tự động trên thread ngầm.

## 4. Các việc cần làm tiếp theo (Next Tasks)
Nếu nhờ Copilot code tiếp, hãy tập trung vào các luồng sau:
1. **Server - `Action.GET_AUCTIONS` / `Action.GET_AUCTION`**: Hiện tại trong `ClientHandler.java`, các luồng này đang báo `pending("AuctionService integration")`. Cần viết `AuctionService.java` để fetch danh sách các phiên đấu giá đang mở (OPEN) và trả về Client.
2. **Server - Bid Service (`handlePlaceBid`)**: Xác thực logic khi client gọi `Action.PLACE_BID`. Kiểm tra thời gian `endTime`, so sánh `amount` có lớn hơn `currentPrice` không, cập nhật `highestBidderId`, và trả về `ClientResponse` tương ứng.
3. **Upload / Đồng bộ File ảnh**: Client đang gửi `imagePath` cục bộ của máy. Cần cơ chế chuyển đổi ảnh thành `byte[]` gửi qua Socket và lưu tại ổ cứng của Server, sau đó trả đường dẫn trên Server về cho Client hiển thị.
