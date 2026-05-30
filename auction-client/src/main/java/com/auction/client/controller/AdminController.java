package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.common.entity.Auction;
import com.auction.common.entity.Item;
import com.auction.common.entity.User;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.message.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AdminController {

    // Dashboard
    @FXML private Label totalUsersLabel;
    @FXML private Label totalSellersLabel;
    @FXML private Label totalBiddersLabel;
    @FXML private Label totalItemsLabel;
    @FXML private Label activeAuctionsLabel;
    @FXML private Label finishedAuctionsLabel;
    @FXML private Label dashboardStatus;
    @FXML private Label userInfoLabel;

    // User table
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private TableColumn<User, String> roleCol;
    @FXML private TableColumn<User, String> statusCol;
    @FXML private TableColumn<User, String> balanceCol;
    @FXML private TableColumn<User, Void> actionCol;
    @FXML private Label userStatus;

    // Auction table
    @FXML private TableView<Auction> auctionTable;
    @FXML private TableColumn<Auction, String> auctionTitleCol;
    @FXML private TableColumn<Auction, String> auctionStatusCol;
    @FXML private TableColumn<Auction, String> auctionPriceCol;
    @FXML private TableColumn<Auction, String> auctionEndCol;
    @FXML private TableColumn<Auction, Void> auctionActionCol;
    @FXML private Label auctionStatus;

    private NetworkClient.PushListener pushListener;

    @FXML
    public void initialize() {
        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        if (user != null && userInfoLabel != null) {
            userInfoLabel.setText("Xin chào, " + user.getUsername());
        }

        setupUserTable();
        setupAuctionTable();
        loadAllData();
        registerPushListener();

        // Callback khi bị ban
        NetworkClient.getInstance().setOnBannedCallback(reason -> {
            com.auction.client.util.BanHandler.handleBan(userInfoLabel.getScene(), reason);
        });
    }

    private void setupUserTable() {
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        statusCol.setCellValueFactory(cellData -> {
            User u = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(u.isBanned() ? "Banned" : "Hoạt động");
        });
        balanceCol.setCellValueFactory(cellData -> {
            User u = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    String.format("%,.0f VNĐ", u.getBalance()));
        });

        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button banBtn = new Button("Ban");
            private final Button unbanBtn = new Button("Unban");
            private final Button topUpBtn = new Button("Nạp tiền");
            private final HBox actionBox = new HBox(5, banBtn, unbanBtn, topUpBtn);
            {
                banBtn.getStyleClass().addAll("btn-sm", "btn-sm-red");
                unbanBtn.getStyleClass().addAll("btn-sm", "btn-sm-green");
                topUpBtn.getStyleClass().addAll("btn-sm", "btn-sm-orange");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User u = getTableView().getItems().get(getIndex());
                    AuthUserData currentUser = NetworkClient.getInstance().getCurrentUser();
                    if (u.getId().equals(currentUser.getUserId())) {
                        setGraphic(new Label("--"));
                    } else {
                        Button banUnbanBtn = u.isBanned() ? unbanBtn : banBtn;
                        banUnbanBtn.setOnAction(e -> {
                            if (u.isBanned()) handleUnban(u);
                            else handleBan(u);
                        });
                        topUpBtn.setOnAction(e -> handleAdminTopUp(u));
                        actionBox.getChildren().setAll(banUnbanBtn, topUpBtn);
                        setGraphic(actionBox);
                    }
                }
            }
        });
    }

    private void setupAuctionTable() {
        auctionTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        auctionStatusCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus().toString()));
        auctionPriceCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%,.0f VNĐ", cellData.getValue().getCurrentPrice())));
        auctionEndCol.setCellValueFactory(cellData -> {
            Auction a = cellData.getValue();
            String endStr = a.getEndTime() != null
                    ? a.getEndTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "N/A";
            return new javafx.beans.property.SimpleStringProperty(endStr);
        });

        auctionActionCol.setCellFactory(param -> new TableCell<>() {
            private final Button cancelBtn = new Button("Hủy phiên");
            {
                cancelBtn.getStyleClass().addAll("btn-sm", "btn-sm-red");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Auction a = getTableView().getItems().get(getIndex());
                    if (a.getStatus() == AuctionStatus.RUNNING || a.getStatus() == AuctionStatus.OPEN) {
                        cancelBtn.setOnAction(e -> handleCancelAuction(a));
                        setGraphic(cancelBtn);
                    } else {
                        setGraphic(new Label("--"));
                    }
                }
            }
        });
    }

    private void loadAllData() {
        loadDashboard();
        loadUsers();
        loadAuctions();
    }

    private void loadDashboard() {
        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        if (user == null) return;

        // Load users count
        NetworkClient.getInstance().sendRequestAsync(
                new ClientRequest(Action.GET_USERS, new GetAllUsersRequest(user.getUserId()))
        ).thenAccept(res -> Platform.runLater(() -> {
            if (res.isSuccess() && res.getData() != null) {
                List<?> rawList = (List<?>) res.getData();
                List<User> users = rawList.stream()
                        .filter(User.class::isInstance)
                        .map(User.class::cast)
                        .toList();
                long sellers = users.stream().filter(u -> u.getRole().name().equals("SELLER")).count();
                long bidders = users.stream().filter(u -> u.getRole().name().equals("BIDDER")).count();
                totalUsersLabel.setText(String.valueOf(users.size()));
                totalSellersLabel.setText(String.valueOf(sellers));
                totalBiddersLabel.setText(String.valueOf(bidders));
            }
        }));

        // Load items count
        NetworkClient.getInstance().sendRequestAsync(
                new ClientRequest(Action.GET_ITEMS, new GetItemsRequest(null, null))
        ).thenAccept(res -> Platform.runLater(() -> {
            if (res.isSuccess() && res.getData() != null) {
                List<?> rawList = (List<?>) res.getData();
                totalItemsLabel.setText(String.valueOf(rawList.size()));
            }
        }));

        // Load auctions count
        NetworkClient.getInstance().sendRequestAsync(
                new ClientRequest(Action.GET_AUCTIONS, new GetAuctionsRequest(null, null))
        ).thenAccept(res -> Platform.runLater(() -> {
            if (res.isSuccess() && res.getData() != null) {
                List<?> rawList = (List<?>) res.getData();
                List<Auction> auctions = rawList.stream()
                        .filter(Auction.class::isInstance)
                        .map(Auction.class::cast)
                        .toList();
                long active = auctions.stream()
                        .filter(a -> a.getStatus() == AuctionStatus.RUNNING || a.getStatus() == AuctionStatus.OPEN)
                        .count();
                long finished = auctions.stream()
                        .filter(a -> a.getStatus() == AuctionStatus.FINISHED)
                        .count();
                activeAuctionsLabel.setText(String.valueOf(active));
                finishedAuctionsLabel.setText(String.valueOf(finished));
            }
            dashboardStatus.setText("Đã tải thống kê");
        }));
    }

    private void loadUsers() {
        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        if (user == null) return;

        ClientRequest req = new ClientRequest(Action.GET_USERS, new GetAllUsersRequest(user.getUserId()));
        NetworkClient.getInstance().sendRequestAsync(req).thenAccept(res -> Platform.runLater(() -> {
            if (res.isSuccess() && res.getData() != null) {
                List<?> rawList = (List<?>) res.getData();
                List<User> users = rawList.stream()
                        .filter(User.class::isInstance)
                        .map(User.class::cast)
                        .toList();
                userTable.getItems().setAll(users);
                userStatus.setText("Có " + users.size() + " người dùng");
            } else {
                userStatus.setText("Lỗi tải: " + res.getMessage());
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> userStatus.setText("Lỗi kết nối!"));
            return null;
        });
    }

    private void loadAuctions() {
        ClientRequest req = new ClientRequest(Action.GET_AUCTIONS, new GetAuctionsRequest(null, null));
        NetworkClient.getInstance().sendRequestAsync(req).thenAccept(res -> Platform.runLater(() -> {
            if (res.isSuccess() && res.getData() != null) {
                List<?> rawList = (List<?>) res.getData();
                List<Auction> auctions = rawList.stream()
                        .filter(Auction.class::isInstance)
                        .map(Auction.class::cast)
                        .toList();
                auctionTable.getItems().setAll(auctions);
                auctionStatus.setText("Có " + auctions.size() + " phiên đấu giá");
            } else {
                auctionStatus.setText("Lỗi tải: " + res.getMessage());
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> auctionStatus.setText("Lỗi kết nối!"));
            return null;
        });
    }

    private void handleBan(User targetUser) {
        AuthUserData admin = NetworkClient.getInstance().getCurrentUser();
        if (admin == null) return;

        TextInputDialog reasonDialog = new TextInputDialog("Vi phạm điều khoản");
        reasonDialog.setTitle("Ban User");
        reasonDialog.setHeaderText("Nhập lý do ban cho \"" + targetUser.getUsername() + "\":");
        reasonDialog.setContentText("Lý do:");
        Optional<String> reasonResult = reasonDialog.showAndWait();
        if (reasonResult.isEmpty() || reasonResult.get().isBlank()) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận ban");
        confirm.setHeaderText("Xác nhận ban user \"" + targetUser.getUsername() + "\"");
        confirm.setContentText("Lý do: " + reasonResult.get());
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            BanUserRequest req = new BanUserRequest(admin.getUserId(), targetUser.getId(), reasonResult.get());
            NetworkClient.getInstance().sendRequestAsync(new ClientRequest(Action.BAN_USER, req))
                    .thenAccept(res -> Platform.runLater(() -> {
                        if (res.isSuccess()) {
                            loadUsers();
                            userStatus.setText("Đã ban user " + targetUser.getUsername());
                        } else {
                            userStatus.setText("Lỗi: " + res.getMessage());
                        }
                    }));
        }
    }

    private void handleUnban(User targetUser) {
        AuthUserData admin = NetworkClient.getInstance().getCurrentUser();
        if (admin == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Unban User");
        confirm.setHeaderText("Xác nhận unban");
        confirm.setContentText("Bạn có chắc muốn unban user \"" + targetUser.getUsername() + "\"?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            UnbanUserRequest req = new UnbanUserRequest(admin.getUserId(), targetUser.getId(), "");
            NetworkClient.getInstance().sendRequestAsync(new ClientRequest(Action.UNBAN_USER, req))
                    .thenAccept(res -> Platform.runLater(() -> {
                        if (res.isSuccess()) {
                            loadUsers();
                            userStatus.setText("Đã unban user " + targetUser.getUsername());
                        } else {
                            userStatus.setText("Lỗi: " + res.getMessage());
                        }
                    }));
        }
    }

    private void handleCancelAuction(Auction auction) {
        AuthUserData admin = NetworkClient.getInstance().getCurrentUser();
        if (admin == null) return;

        TextInputDialog reasonDialog = new TextInputDialog();
        reasonDialog.setTitle("Hủy phiên đấu giá");
        reasonDialog.setHeaderText("Nhập lý do hủy phiên \"" + auction.getTitle() + "\"");
        reasonDialog.setContentText("Lý do:");
        Optional<String> reason = reasonDialog.showAndWait();

        if (reason.isPresent() && !reason.get().isEmpty()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận hủy");
            confirm.setHeaderText("Xác nhận hủy phiên đấu giá");
            confirm.setContentText("Bạn có chắc muốn hủy phiên \"" + auction.getTitle() + "\"?");
            Optional<ButtonType> result = confirm.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                CancelAuctionRequest req = new CancelAuctionRequest(admin.getUserId(), auction.getId(), reason.get());
                NetworkClient.getInstance().sendRequestAsync(new ClientRequest(Action.CANCEL_AUCTION, req))
                        .thenAccept(res -> Platform.runLater(() -> {
                            if (res.isSuccess()) {
                                loadAuctions();
                                loadDashboard();
                                auctionStatus.setText("Đã hủy phiên " + auction.getTitle());
                            } else {
                                auctionStatus.setText("Lỗi: " + res.getMessage());
                            }
                        }));
            }
        }
    }

    private void registerPushListener() {
        pushListener = pushMsg -> Platform.runLater(this::loadAllData);
        NetworkClient.getInstance().addPushListener(pushListener);
    }

    @FXML
    public void handleRefreshUsers() {
        loadUsers();
    }

    private void handleAdminTopUp(User targetUser) {
        TextInputDialog dialog = new TextInputDialog("100000");
        dialog.setTitle("Nạp tiền cho " + targetUser.getUsername());
        dialog.setHeaderText("Nhập số tiền muốn nạp cho " + targetUser.getUsername() + ":");
        dialog.setContentText("Số tiền (VNĐ):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) return;
                TopUpRequest req = new TopUpRequest(targetUser.getId(), amount);
                NetworkClient.getInstance().sendRequestAsync(new ClientRequest(Action.TOP_UP, req))
                    .thenAccept(res -> Platform.runLater(() -> {
                        if (res.isSuccess()) {
                            userStatus.setText("Đã nạp " + String.format("%,.0f", amount) + " VNĐ cho " + targetUser.getUsername());
                            loadUsers();
                        } else {
                            userStatus.setText("Lỗi: " + res.getMessage());
                        }
                    }));
            } catch (NumberFormatException e) {
                // ignore
            }
        });
    }

    @FXML
    public void handleRefreshAuctions() {
        loadAuctions();
    }

    @FXML
    public void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Đăng xuất");
        confirm.setHeaderText("Xác nhận đăng xuất");
        confirm.setContentText("Bạn có chắc muốn đăng xuất?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (pushListener != null) {
                NetworkClient.getInstance().removePushListener(pushListener);
            }
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
                Stage stage = (Stage) userInfoLabel.getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
