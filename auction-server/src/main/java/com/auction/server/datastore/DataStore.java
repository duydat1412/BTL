package com.auction.server.datastore;

import com.auction.common.entity.Admin;
import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;
import com.auction.common.entity.Item;
import com.auction.common.entity.User;
import com.auction.common.enums.UserRole;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Stores application data in memory and persists it through Java serialization.
 */
public class DataStore implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String FILE_PATH = "data/auction_data.dat";
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@123";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@auction.local";

    private static volatile DataStore instance;

    private List<User> users = new ArrayList<>();
    private List<Item> items = new ArrayList<>();
    private List<Auction> auctions = new ArrayList<>();
    private List<BidTransaction> bidTransactions = new ArrayList<>();

    private DataStore() {
    }

    public static DataStore getInstance() {
        if (instance == null) {
            synchronized (DataStore.class) {
                if (instance == null) {
                    instance = new DataStore();
                }
            }
        }
        return instance;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Item> getItems() {
        return items;
    }

    public List<Auction> getAuctions() {
        return auctions;
    }

    public List<BidTransaction> getBidTransactions() {
        return bidTransactions;
    }

    public synchronized void loadData() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            System.out.println("Data file does not exist. Starting with empty store.");
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            ensureDefaultAdminAccount();
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            DataStore loadedData = (DataStore) in.readObject();
            this.users = loadedData.users;
            this.items = loadedData.items;
            this.auctions = loadedData.auctions;
            this.bidTransactions = loadedData.bidTransactions;
            ensureDefaultAdminAccount();
            System.out.println("Loaded data from " + FILE_PATH);
        } catch (Exception e) {
            System.err.println("Failed to load data file: " + e.getMessage());
        }
    }

    public synchronized void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            out.writeObject(this);
            System.out.println("Saved data to " + FILE_PATH);
        } catch (IOException e) {
            System.err.println("Failed to write data file: " + e.getMessage());
        }
    }

    private void ensureDefaultAdminAccount() {
        boolean hasAdmin = users.stream().anyMatch(user -> user.getRole() == UserRole.ADMIN);
        if (hasAdmin) {
            return;
        }

        Admin admin = new Admin(
                DEFAULT_ADMIN_USERNAME,
                BCrypt.hashpw(DEFAULT_ADMIN_PASSWORD, BCrypt.gensalt()),
                DEFAULT_ADMIN_EMAIL,
                "System"
        );
        users.add(admin);
        saveData();
        System.out.println("Created default admin account.");
    }
}
