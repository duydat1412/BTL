package com.auction.common.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Lớp trừu tượng cơ sở cho tất cả Entity trong hệ thống.
 *
 * <p>Mọi entity đều có: id duy nhất, thời gian tạo, thời gian cập nhật.
 * Implements Serializable để hỗ trợ truyền qua Socket (ObjectStream).
 *
 * <p>Subclass bắt buộc override {@link #getInfo()} để cung cấp
 * thông tin mô tả riêng (thể hiện tính Đa hình — Polymorphism).
 */
public abstract class Entity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID duy nhất của entity — tự động tạo bằng UUID. */
    private String id;

    /** Thời gian tạo entity. */
    private LocalDateTime createdAt;

    /** Thời gian cập nhật gần nhất. */
    private LocalDateTime updatedAt;

    /**
     * Constructor mặc định — tự động gán id và timestamps.
     */
    protected Entity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Trả về thông tin mô tả của entity.
     * Mỗi subclass override phương thức này để hiển thị thông tin riêng.
     *
     * @return chuỗi mô tả entity
     */
    public abstract String getInfo();

    // ==================== Getters & Setters ====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ==================== equals, hashCode, toString ====================

    /**
     * Hai entity bằng nhau nếu cùng id.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Entity entity = (Entity) o;
        return Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * toString() gọi getInfo() — đảm bảo tính đa hình khi in ra console.
     */
    @Override
    public String toString() {
        return getInfo();
    }
}
