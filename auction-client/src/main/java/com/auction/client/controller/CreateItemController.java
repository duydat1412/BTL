package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.common.enums.ItemType;
import com.auction.common.message.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
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
    private TextField imagePathField;
    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        typeBox.setItems(FXCollections.observableArrayList(ItemType.values()));
    }

    @FXML
    public void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh sản phẩm");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(nameField.getScene().getWindow());
        if (selectedFile != null) {
            imagePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    public void handleCreateItem() {
        String name = nameField.getText();
        String priceStr = priceField.getText();
        String desc = descArea.getText();
        String durationStr = durationField.getText();
        String imagePath = imagePathField.getText();
        ItemType type = typeBox.getValue();

        if (name.isEmpty() || priceStr.isEmpty() || type == null || durationStr.isEmpty()) {
            statusLabel.setText("Vui lòng điền đầy đủ Tên, Giá, Loại và Thời gian!");
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

            Map<String, String> extraAttrs = new HashMap<>();
            extraAttrs.put("durationMinutes", String.valueOf(duration));
            if (!imagePath.isEmpty()) {
                extraAttrs.put("imagePath", imagePath);
            }

            CreateItemRequest req = new CreateItemRequest(name, desc, price, user.getUserId(), type, extraAttrs);
            ClientRequest request = new ClientRequest(Action.CREATE_ITEM, req);

            ClientResponse res = NetworkClient.getInstance().sendRequest(request);

            if (res.isSuccess()) {
                statusLabel.setText("Tạo sản phẩm & lên lịch đấu giá thành công!");
                statusLabel.setStyle("-fx-text-fill: #2ecc71;");

                // Chuyển về Dashboard sau 1 giây
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                    javafx.application.Platform.runLater(this::goBack);
                }).start();
            } else {
                statusLabel.setText(res.getMessage());
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            }

        } catch (NumberFormatException e) {
            statusLabel.setText("Giá và Thời gian phải là số hợp lệ!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        } catch (Exception e) {
            statusLabel.setText("Lỗi kết nối server!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            e.printStackTrace();
        }
    }

    @FXML
    public void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/seller_dashboard.fxml"));
            Stage stage = (Stage) nameField.getScene().getWindow();
            Scene scene = new Scene(root);
            String css = getClass().getResource("/css/style.css").toExternalForm();
            scene.getStylesheets().add(css);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
