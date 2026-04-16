# Module `auction-common`

Đây là một thư viện sử dụng chung cho cả `auction-client` (Phía Khách) và `auction-server` (Phía Máy Chủ).

## Lý do tồn tại của Module này
Bởi vì hệ thống Đấu giá của chúng ta được thiết kế theo cấu trúc truyền/nhận Object bằng Stream (Java Serialization).
-> Nên **CẢ HAI BÊN** Server và Client đều phải "hiểu" chung một định dạng lớp (Ví dụ: Class `User`, `Item`, `RequestMessage`, v.v... phải có cấu trúc thuộc tính y chang nhau).

Việc ném các class thực thể đó vào `auction-common` giúp cả hai dự án có thể `import com.auction.common...` trực tiếp mà không phải copy-paste code dư thừa.

## Các thành phần
```text
src/main/java/com/auction/common/
├── entity/          ← Các đối tượng cốt lõi: User, Item, Auction, Bid. BẮT BUỘC implements Serializable
├── enums/           ← Tập hợp các trạng thái: Status (OPEN, CLOSED), MessageType (REQUEST, RESPONSE)
├── exceptions/      ← Custom Exceptions (Người D sẽ phụ trách) dùng chung cho báo lỗi
└── network/         ← Tập hợp các DTO hoặc Messages truyền dẫn (nếu có)
```

**⚠️ Quy tắc tối quan trọng:** TẤT CẢ các Models nằm ở module này nếu muốn bắn sang Socket hoặc đẩy vào File Database đều bắt buộc phải implements `java.io.Serializable` và khai báo tĩnh `serialVersionUID`.
