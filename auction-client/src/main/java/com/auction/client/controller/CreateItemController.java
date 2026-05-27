package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
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
import java.util.Map;

public class CreateItemController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;
    @FXML
    private ComboBox<ItemType> typeBox;
    @FXML
    private TextArea descArea;
    @FXML
    private TextField durationField;
    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        typeBox.setItems(FXCollections.observableArrayList(ItemType.values()));
    }

    @FXML
    public void handleCreateItem() {
        String name = nameField.getText();
        String priceStr = priceField.getText();
        String desc = descArea.getText();
        String durationStr = durationField.getText();
        ItemType type = typeBox.getValue();

        if (name.isEmpty() || priceStr.isEmpty() || type == null || durationStr.isEmpty()) {
            statusLabel.setText("Vui lòng điền đầy đủ Tên, Giá, Loại và Thời gian!");
            statusLabel.getStyleClass().add("status-error");
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

            Map<String, String> extraAttrs = new HashMap<>();
            extraAttrs.put("durationMinutes", String.valueOf(duration));

            CreateItemRequest req = new CreateItemRequest(name, desc, price, user.getUserId(), type, extraAttrs);
            ClientRequest request = new ClientRequest(Action.CREATE_ITEM, req);

            statusLabel.setText("Đang tạo sản phẩm...");
            statusLabel.getStyleClass().add("status-info");

            NetworkClient.getInstance().sendRequestAsync(request).thenAccept(res -> Platform.runLater(() -> {
                if (res.isSuccess()) {
                    statusLabel.setText("Tạo sản phẩm & lên lịch đấu giá thành công!");
                    statusLabel.getStyleClass().add("status-success");
                    new Thread(() -> {
                        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                        Platform.runLater(this::goBack);
                    }).start();
                } else {
                    statusLabel.setText(res.getMessage());
                    statusLabel.getStyleClass().add("status-error");
                }
            })).exceptionally(ex -> {
                Platform.runLater(() -> {
                    statusLabel.setText("Lỗi kết nối server!");
                    statusLabel.getStyleClass().add("status-error");
                });
                return null;
            });

        } catch (NumberFormatException e) {
            statusLabel.setText("Giá và Thời gian phải là số hợp lệ!");
            statusLabel.getStyleClass().add("status-error");
        }
    }

    @FXML
    public void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/seller_dashboard.fxml"));
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
