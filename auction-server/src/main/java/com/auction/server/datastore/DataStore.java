package com.auction.server.datastore;

import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;
import com.auction.common.entity.Item;
import com.auction.common.entity.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý dữ liệu hệ thống bằng Singleton Pattern và Java Serialization.
 * Tất cả Data được lưu xuống một file duy nhất: data/auction_data.dat
 */
public class DataStore implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final String FILE_PATH = "data/auction_data.dat";
    
    // Thuộc tính lưu trữ duy nhất của hệ thống
    private static volatile DataStore instance;

    // Các danh sách dữ liệu trong hệ thống
    private List<User> users = new ArrayList<>();
    private List<Item> items = new ArrayList<>();
    private List<Auction> auctions = new ArrayList<>();
    private List<BidTransaction> bidTransactions = new ArrayList<>();

    // Private constructor (chỉ cho phép lấy qua getInstance)
    private DataStore() {
    }

    public static DataStore getInstance() {
        if (instance == null) {
            synchronized (DataStore.class) {
                if (instance==null) {
                    instance = new DataStore();
                }
            };
        }
        return instance;
    }

    public List<User> getUsers() { return users; }
    public List<Item> getItems() { return items; }
    public List<Auction> getAuctions() { return auctions; }
    public List<BidTransaction> getBidTransactions() { return bidTransactions; }

    /**
     * Tải dữ liệu từ file lên RAM
     */
    public synchronized void loadData() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            System.out.println("File dữ liệu chưa tồn tại, bắt đầu với DataStore rỗng.");
            // Đảm bảo thư mục tồn tại
            file.getParentFile().mkdirs();
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            DataStore loadedData = (DataStore) in.readObject();
            this.users = loadedData.users;
            this.items = loadedData.items;
            this.auctions = loadedData.auctions;
            this.bidTransactions = loadedData.bidTransactions;
            System.out.println("Tải dữ liệu từ " + FILE_PATH + " thành công!");
        } catch (Exception e) {
            System.err.println("Lỗi khi đọc file dữ liệu: " + e.getMessage());
        }
    }

    /**
     * Lưu dữ liệu từ RAM xuống file
     */
    public synchronized void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            out.writeObject(this);
            System.out.println("Đã lưu dữ liệu xuống " + FILE_PATH);
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi file dữ liệu: " + e.getMessage());
        }
    }
}
