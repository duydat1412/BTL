package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.common.entity.Auction;
import com.auction.common.entity.Item;
import com.auction.common.enums.ItemType;
import com.auction.common.message.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellerDashboardController {

    @FXML
    private Label userInfoLabel;
    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;
    @FXML
    private ComboBox<ItemType> typeBox;
    @FXML
    private TextField descField;
    @FXML
    private Label statusLabel;
    @FXML
    private ListView<Item> itemListView;

    private NetworkClient.PushListener pushListener;

    @FXML
    public void initialize() {
        typeBox.setItems(FXCollections.observableArrayList(ItemType.values()));

        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        if (user != null && userInfoLabel != null) {
            userInfoLabel.setText("Xin chào, " + user.getUsername());
        }

        itemListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " - " + item.getStartingPrice());
                }
            }
        });

        loadItems();
        registerPushListener();
    }

    private void loadItems() {
        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        if (user == null) return;
        ClientRequest req = new ClientRequest(Action.GET_ITEMS, new GetItemsRequest(user.getUserId(), null));
        NetworkClient.getInstance().sendRequestAsync(req).thenAccept(res -> Platform.runLater(() -> {
            if (res.isSuccess() && res.getData() != null) {
                List<?> rawList = (List<?>) res.getData();
                List<Item> items = rawList.stream()
                        .filter(Item.class::isInstance)
                        .map(Item.class::cast)
                        .toList();
                itemListView.getItems().setAll(items);
                statusLabel.setText("Có " + items.size() + " sản phẩm");
            } else {
                statusLabel.setText("Lỗi tải dữ liệu: " + res.getMessage());
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
                Platform.runLater(this::loadItems);
            }
        };
        NetworkClient.getInstance().addPushListener(pushListener);
    }

    @FXML
    public void handleLogout() {
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

    @FXML
    public void handleCreateItem() {
        String name = nameField.getText();
        String priceStr = priceField.getText();
        ItemType type = typeBox.getValue();
        String desc = descField.getText();

        if (name.isEmpty() || priceStr.isEmpty() || type == null) {
            statusLabel.setText("Vui lòng điền Tên, Giá và Loại!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            AuthUserData user = NetworkClient.getInstance().getCurrentUser();
            if (user == null) {
                statusLabel.setText("Vui lòng đăng nhập lại!");
                return;
            }

            Map<String, String> attrs = new HashMap<>();
            attrs.put("durationMinutes", "60");

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
    }
}
