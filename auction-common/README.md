# 📦 Module: `auction-common`

## 📖 Vai trò (Role)
Đây là **Module Core (Dữ liệu Dùng chung)** của toàn bộ hệ thống Đấu giá trực tuyến.
Module này **không tự chạy độc lập**, mà được định nghĩa như một "thư viện" chứa các cấu trúc mã nguồn nền tảng để cả Máy chủ (`auction-server`) lẫn Máy khách (`auction-client`) cùng tái sử dụng. Tức là cả Server và Client đều `import` common vào để dùng.

## 🏗️ Thành phần chính
Nằm chủ yếu trong `src/main/java/com/auction/common/`:
- **`Entity` (Model)**: Cấu trúc các đối tượng thực thể cốt lõi (ví dụ: `User`, `Item`, `Auction`). Đại diện cho các bảng trong Database.
- **`Enum`**: Chứa các hằng số cấu hình hệ thống (ví dụ: `UserRole`, `AuctionStatus`, `ItemType`).
- **`Design Patterns`**: Chứa các Interface trừu tượng (Abstract) hoặc các nhà máy khởi tạo (ví dụ: `ItemFactory`).
- **Ràng buộc chung**: Thư viện `Gson` nằm ở đây đóng vai trò chuyển đổi qua lại giữa Java Object và mã JSON.

## 👥 Ý nghĩa thực tiễn
- Giúp loại bỏ hoàn toàn việc lặp lại code (DRY - Don't Repeat Yourself). Thay vì phải code class `User` 2 lần vào Client và Server, ta chỉ cần code 1 lần ở Common.
- Module này là khu vực làm việc chính của thành viên phụ trách mảng Cấu trúc dữ liệu và Lập trình Hướng đối tượng (OOP).
