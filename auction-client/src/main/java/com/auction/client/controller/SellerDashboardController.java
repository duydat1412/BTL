package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.common.entity.Auction;
import com.auction.common.entity.Item;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.message.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.auction.client.util.NotificationToast;

public class SellerDashboardController {

    @FXML
    private Label userInfoLabel;
    @FXML
    private Label balanceLabel;
    @FXML
    private Label totalItemsLabel;
    @FXML
    private Label activeAuctionsLabel;
    @FXML
    private Label finishedAuctionsLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private ListView<Item> itemListView;

    private NetworkClient.PushListener pushListener;
    private Set<String> finishedItemIds = java.util.Collections.emptySet();
    private Map<String, String> activeAuctionItemIds = new HashMap<>(); // itemId -> auctionId

    @FXML
    public void initialize() {
        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        if (user != null && userInfoLabel != null) {
            userInfoLabel.setText("Xin chào, " + user.getUsername());
        }

        itemListView.setCellFactory(param -> new ListCell<>() {
            private final Button deleteBtn = new Button("Xóa");
            private final Button cancelBtn = new Button("Hủy phiên");
            private final HBox buttons = new HBox(8, deleteBtn, cancelBtn);
            private final HBox container = new HBox(15);

            {
                container.getStyleClass().add("custom-card-cell");
                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                deleteBtn.getStyleClass().addAll("btn-sm", "btn-sm-red");
                cancelBtn.getStyleClass().addAll("btn-sm", "btn-sm-orange");
            }

            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label nameLabel = new Label(item.getName() + " - Khởi điểm: " + String.format("%,.0f", item.getStartingPrice()) + " VNĐ");
                    nameLabel.getStyleClass().addAll("text-white", "font-bold");
                    container.getChildren().setAll(nameLabel, new Region(), buttons);
                    container.setHgrow(container.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);

                    boolean hasFinished = finishedItemIds.contains(item.getId());
                    deleteBtn.setDisable(hasFinished);
                    deleteBtn.setOnAction(e -> {
                        if (!hasFinished) handleDeleteItem(item);
                    });

                    boolean hasActive = activeAuctionItemIds.containsKey(item.getId());
                    cancelBtn.setVisible(hasActive);
                    cancelBtn.setManaged(hasActive);
                    if (hasActive) {
                        String auctionId = activeAuctionItemIds.get(item.getId());
                        cancelBtn.setOnAction(e -> handleCancelAuction(auctionId, item));
                    }

                    setGraphic(container);
                    setText(null);
                }
            }
        });

        loadItems();
        loadBalance();
        registerPushListener();

        // Callback khi bị ban
        NetworkClient.getInstance().setOnBannedCallback(reason -> {
            com.auction.client.util.BanHandler.handleBan(userInfoLabel.getScene(), reason);
        });
    }

    @FXML
    public void goToAddItem() {
        if (pushListener != null) {
            NetworkClient.getInstance().removePushListener(pushListener);
        }
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/create_item.fxml"));
            Stage stage = (Stage) userInfoLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadItems() {
        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        if (user == null) return;
        ClientRequest itemReq = new ClientRequest(Action.GET_ITEMS, new GetItemsRequest(user.getUserId(), null));
        ClientRequest auctionReq = new ClientRequest(Action.GET_AUCTIONS, null);
        // Fetch items và auctions song song để biết item nào có FINISHED auction
        CompletableFuture<ClientResponse> itemsFuture = NetworkClient.getInstance().sendRequestAsync(itemReq);
        CompletableFuture<ClientResponse> auctionsFuture = NetworkClient.getInstance().sendRequestAsync(auctionReq);
        itemsFuture.thenAcceptBoth(auctionsFuture, (itemsRes, auctionsRes) -> Platform.runLater(() -> {
            if (itemsRes.isSuccess() && itemsRes.getData() != null) {
                List<?> rawList = (List<?>) itemsRes.getData();
                List<Item> items = rawList.stream()
                        .filter(Item.class::isInstance)
                        .map(Item.class::cast)
                        .toList();

                // Xác định item nào có FINISHED auction và item nào có active auction
                if (auctionsRes.isSuccess() && auctionsRes.getData() != null) {
                    List<?> rawAuctions = (List<?>) auctionsRes.getData();
                    List<Auction> auctionList = rawAuctions.stream()
                            .filter(Auction.class::isInstance)
                            .map(Auction.class::cast)
                            .toList();
                    finishedItemIds = auctionList.stream()
                            .filter(a -> a.getStatus() == AuctionStatus.FINISHED)
                            .map(Auction::getItemId)
                            .collect(Collectors.toSet());
                    activeAuctionItemIds = auctionList.stream()
                            .filter(a -> a.getStatus() == AuctionStatus.OPEN
                                    || a.getStatus() == AuctionStatus.RUNNING)
                            .collect(Collectors.toMap(Auction::getItemId, Auction::getId, (a1, a2) -> a1));
                } else {
                    finishedItemIds = java.util.Collections.emptySet();
                    activeAuctionItemIds = new HashMap<>();
                }

                itemListView.getItems().setAll(items);
                
                // Cập nhật thẻ thống kê
                if (totalItemsLabel != null) totalItemsLabel.setText(String.valueOf(items.size()));
                if (activeAuctionsLabel != null) activeAuctionsLabel.setText(String.valueOf(activeAuctionItemIds.size()));
                if (finishedAuctionsLabel != null) finishedAuctionsLabel.setText(String.valueOf(finishedItemIds.size()));

                statusLabel.setText("Có " + items.size() + " sản phẩm");
            } else {
                statusLabel.setText("Lỗi tải dữ liệu: " + itemsRes.getMessage());
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> statusLabel.setText("Lỗi kết nối server!"));
            return null;
        });
    }

    private void registerPushListener() {
        pushListener = pushMsg -> {
            if (pushMsg.getType() == ServerPushMessage.PushType.AUCTION_STARTED
                    || pushMsg.getType() == ServerPushMessage.PushType.AUCTION_ENDED) {
                Platform.runLater(() -> {
                    loadItems();
                    loadBalance();
                });
            }
        };
        NetworkClient.getInstance().addPushListener(pushListener);
    }

    private void handleDeleteItem(Item item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xóa sản phẩm");
        confirm.setHeaderText("Xác nhận xóa");
        confirm.setContentText("Bạn có chắc muốn xóa sản phẩm \"" + item.getName() + "\"?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            AuthUserData user = NetworkClient.getInstance().getCurrentUser();
            if (user == null) return;

            DeleteItemRequest req = new DeleteItemRequest(item.getId(), user.getUserId());
            ClientRequest request = new ClientRequest(Action.DELETE_ITEM, req);

            statusLabel.setText("Đang xóa...");
            NetworkClient.getInstance().sendRequestAsync(request).thenAccept(res -> Platform.runLater(() -> {
                if (res.isSuccess()) {
                    statusLabel.setText("Xóa thành công!");
                    statusLabel.getStyleClass().setAll("status-success");
                    loadItems();
                } else {
                    statusLabel.setText(res.getMessage());
                    statusLabel.getStyleClass().setAll("status-error");
                }
            })).exceptionally(ex -> {
                Platform.runLater(() -> statusLabel.setText("Lỗi kết nối server!"));
                return null;
            });
        }
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

    @FXML
    public void handleRefresh() {
        loadItems();
        loadBalance();
    }

    private void loadBalance() {
        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        if (user == null || balanceLabel == null) return;
        ClientRequest req = new ClientRequest(Action.GET_BALANCE, user.getUserId());
        NetworkClient.getInstance().sendRequestAsync(req).thenAccept(res -> Platform.runLater(() -> {
            if (res.isSuccess() && res.getData() != null) {
                double balance = (double) res.getData();
                balanceLabel.setText("Số dư: " + String.format("%,.0f", balance) + " VNĐ");
            }
        }));
    }

    @FXML
    public void handleTopUp() {
        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        if (user == null) return;

        TextInputDialog dialog = new TextInputDialog("100000");
        dialog.setTitle("Nạp tiền");
        dialog.setHeaderText("Nhập số tiền muốn nạp:");
        dialog.setContentText("Số tiền (VNĐ):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) return;
                TopUpRequest req = new TopUpRequest(user.getUserId(), amount);
                NetworkClient.getInstance().sendRequestAsync(new ClientRequest(Action.TOP_UP, req))
                    .thenAccept(res -> Platform.runLater(() -> {
                        if (res.isSuccess()) {
                            Stage stage = (Stage) userInfoLabel.getScene().getWindow();
                            NotificationToast.show(stage, "Nạp thành công: " + String.format("%,.0f", amount) + " VNĐ", false);
                            loadBalance();
                        }
                    }));
            } catch (NumberFormatException e) {
                // ignore
            }
        });
    }

    private void handleCancelAuction(String auctionId, Item item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Hủy phiên đấu giá");
        confirm.setHeaderText("Xác nhận hủy phiên");
        confirm.setContentText("Bạn có chắc muốn hủy phiên đấu giá cho sản phẩm \"" + item.getName() + "\"?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            AuthUserData user = NetworkClient.getInstance().getCurrentUser();
            if (user == null) return;

            SellerCancelAuctionRequest req = new SellerCancelAuctionRequest(
                    user.getUserId(), auctionId, "Seller cancelled");
            ClientRequest request = new ClientRequest(Action.SELLER_CANCEL_AUCTION, req);

            statusLabel.setText("Đang hủy...");
            NetworkClient.getInstance().sendRequestAsync(request).thenAccept(res -> Platform.runLater(() -> {
                if (res.isSuccess()) {
                    statusLabel.setText("Hủy phiên thành công!");
                    statusLabel.getStyleClass().setAll("status-success");
                    loadItems();
                } else {
                    statusLabel.setText(res.getMessage());
                    statusLabel.getStyleClass().setAll("status-error");
                }
            })).exceptionally(ex -> {
                Platform.runLater(() -> statusLabel.setText("Lỗi kết nối server!"));
                return null;
            });
        }
    }
}
