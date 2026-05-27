package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.common.entity.Auction;
import com.auction.common.entity.Item;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.enums.ItemType;
import com.auction.common.message.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
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
    private TextField nameField;
    @FXML
    private TextField priceField;
    @FXML
    private ComboBox<ItemType> typeBox;
    @FXML
    private TextField descField;
    @FXML
    private TextField durationField;
    @FXML
    private Label statusLabel;
    @FXML
    private ListView<Item> itemListView;

    private NetworkClient.PushListener pushListener;
    private Set<String> finishedItemIds = java.util.Collections.emptySet();
    private Map<String, String> activeAuctionItemIds = new HashMap<>(); // itemId -> auctionId

    @FXML
    public void initialize() {
        typeBox.setItems(FXCollections.observableArrayList(ItemType.values()));

        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        if (user != null && userInfoLabel != null) {
            userInfoLabel.setText("Xin chào, " + user.getUsername());
        }

        itemListView.setCellFactory(param -> new ListCell<>() {
            private final Button editBtn = new Button("Sửa");
            private final Button deleteBtn = new Button("Xóa");
            private final Button cancelBtn = new Button("Hủy phiên");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn, cancelBtn);
            private final HBox container = new HBox(10);

            {
                editBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 4 10; -fx-background-radius: 4;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 4 10; -fx-background-radius: 4;");
                cancelBtn.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 4 10; -fx-background-radius: 4;");
            }

            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label nameLabel = new Label(item.getName() + " - " + String.format("%,.0f", item.getStartingPrice()) + " VNĐ");
                    nameLabel.setStyle("-fx-text-fill: #0f172a; -fx-font-weight: bold;");
                    container.getChildren().setAll(nameLabel, new Region(), buttons);
                    container.setHgrow(nameLabel, javafx.scene.layout.Priority.ALWAYS);

                    editBtn.setOnAction(e -> showEditDialog(item));
                    boolean hasFinished = finishedItemIds.contains(item.getId());
                    deleteBtn.setDisable(hasFinished);
                    deleteBtn.setStyle(hasFinished
                            ? "-fx-background-color: #9ca3af; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 4 10; -fx-background-radius: 4;"
                            : "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 4 10; -fx-background-radius: 4;");
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

    private void showEditDialog(Item item) {
        Dialog<Item> dialog = new Dialog<>();
        dialog.setTitle("Sửa sản phẩm");
        dialog.setHeaderText("Chỉnh sửa thông tin sản phẩm");

        ButtonType saveBtnType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        TextField nameField = new TextField(item.getName());
        TextField priceField = new TextField(String.valueOf(item.getStartingPrice()));
        TextField descField = new TextField(item.getDescription());
        ComboBox<ItemType> typeField = new ComboBox<>(FXCollections.observableArrayList(ItemType.values()));
        typeField.setValue(item.getItemType());

        VBox content = new VBox(10,
                new Label("Tên:"), nameField,
                new Label("Giá:"), priceField,
                new Label("Mô tả:"), descField,
                new Label("Loại:"), typeField
        );
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogBtn -> {
            if (dialogBtn == saveBtnType) {
                try {
                    item.setName(nameField.getText());
                    item.setStartingPrice(Double.parseDouble(priceField.getText()));
                    item.setDescription(descField.getText());
                    item.setItemType(typeField.getValue());
                    return item;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Item> result = dialog.showAndWait();
        result.ifPresent(updatedItem -> updateItem(updatedItem));
    }

    private void updateItem(Item item) {
        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        if (user == null) return;

        UpdateItemRequest req = new UpdateItemRequest(
                item.getId(), item.getName(), item.getDescription(),
                item.getStartingPrice(), item.getItemType(), null
        );
        ClientRequest request = new ClientRequest(Action.UPDATE_ITEM, req);

        statusLabel.setText("Đang cập nhật...");
        statusLabel.setStyle("-fx-text-fill: gray;");

        NetworkClient.getInstance().sendRequestAsync(request).thenAccept(res -> Platform.runLater(() -> {
            if (res.isSuccess()) {
                statusLabel.setText("Cập nhật thành công!");
                statusLabel.setStyle("-fx-text-fill: #2ecc71;");
                loadItems();
            } else {
                statusLabel.setText(res.getMessage());
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> statusLabel.setText("Lỗi kết nối server!"));
            return null;
        });
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
                    statusLabel.setStyle("-fx-text-fill: #2ecc71;");
                    loadItems();
                } else {
                    statusLabel.setText(res.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #e74c3c;");
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
    public void handleCreateItem() {
        String name = nameField.getText();
        String priceStr = priceField.getText();
        ItemType type = typeBox.getValue();
        String desc = descField.getText();
        String durationStr = durationField.getText();

        if (name.isEmpty() || priceStr.isEmpty() || type == null || durationStr.isEmpty()) {
            statusLabel.setText("Vui lòng điền Tên, Giá, Loại và Thời gian!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            long duration = Long.parseLong(durationStr);
            AuthUserData user = NetworkClient.getInstance().getCurrentUser();
            if (user == null) {
                statusLabel.setText("Vui lòng đăng nhập lại!");
                return;
            }

            Map<String, String> attrs = new HashMap<>();
            attrs.put("durationMinutes", String.valueOf(duration));

            CreateItemRequest req = new CreateItemRequest(name, desc, price, user.getUserId(), type, attrs);
            ClientRequest request = new ClientRequest(Action.CREATE_ITEM, req);

            statusLabel.setText("Đang tạo...");
            statusLabel.setStyle("-fx-text-fill: gray;");

            NetworkClient.getInstance().sendRequestAsync(request).thenAccept(res -> Platform.runLater(() -> {
                if (res.isSuccess()) {
                    statusLabel.setText("Tạo thành công!");
                    statusLabel.setStyle("-fx-text-fill: #2ecc71;");
                    nameField.clear();
                    priceField.clear();
                    typeBox.setValue(null);
                    descField.clear();
                    loadItems();
                } else {
                    statusLabel.setText(res.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                }
            })).exceptionally(ex -> {
                Platform.runLater(() -> statusLabel.setText("Lỗi kết nối server!"));
                return null;
            });
        } catch (NumberFormatException e) {
            statusLabel.setText("Giá phải là số!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
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
                    statusLabel.setStyle("-fx-text-fill: #2ecc71;");
                    loadItems();
                } else {
                    statusLabel.setText(res.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                }
            })).exceptionally(ex -> {
                Platform.runLater(() -> statusLabel.setText("Lỗi kết nối server!"));
                return null;
            });
        }
    }
}
