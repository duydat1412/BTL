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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Stores application data in memory and persists it through Java serialization.
 */
public class DataStore implements Serializable {
    private static final long serialVersionUID = 1L;

    static final String DATA_FILE_PROPERTY = "auction.data.file";
    private static final String DEFAULT_FILE_PATH = "data/auction_data.dat";
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@123";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@auction.local";

    private static volatile DataStore instance;

    private List<User> users = new CopyOnWriteArrayList<>();
    private List<Item> items = new CopyOnWriteArrayList<>();
    private List<Auction> auctions = new CopyOnWriteArrayList<>();
    private List<BidTransaction> bidTransactions = new CopyOnWriteArrayList<>();

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
        File file = getDataFilePath().toFile();
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
            this.users = new CopyOnWriteArrayList<>(loadedData.users);
            this.items = new CopyOnWriteArrayList<>(loadedData.items);
            this.auctions = new CopyOnWriteArrayList<>(loadedData.auctions);
            this.bidTransactions = new CopyOnWriteArrayList<>(loadedData.bidTransactions);
            ensureDefaultAdminAccount();
            System.out.println("Loaded data from " + file.getPath());
        } catch (Exception e) {
            System.err.println("Failed to load data file: " + e.getMessage());
        }
    }

    public synchronized void saveData() {
        Path dataFilePath = getDataFilePath();
        try {
            Path parent = dataFilePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            System.err.println("Failed to prepare data directory: " + e.getMessage());
            return;
        }

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(dataFilePath.toFile()))) {
            out.writeObject(this);
            System.out.println("Saved data to " + dataFilePath);
        } catch (IOException e) {
            System.err.println("Failed to write data file: " + e.getMessage());
        }
    }

    static Path getDataFilePath() {
        return Paths.get(System.getProperty(DATA_FILE_PROPERTY, DEFAULT_FILE_PATH));
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
