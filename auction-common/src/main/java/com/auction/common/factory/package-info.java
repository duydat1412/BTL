/**
 * Package chứa Factory Pattern — tạo object theo loại.
 *
 * <p>Factory Method = thay vì viết new Electronics(), new Art(), new Vehicle()
 * ở nhiều nơi, ta gọi ItemFactory.create("ELECTRONICS") — một chỗ duy nhất.
 * Khi thêm loại mới, chỉ sửa Factory, không sửa code gọi.
 *
 * <p>Sẽ được Người B triển khai: ItemFactory.
 */
package com.auction.common.factory;
