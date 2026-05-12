package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.common.entity.Item;
import com.auction.common.enums.ItemType;
import com.auction.common.message.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class SellerDashboardController {

    @FXML private ListView<Item> itemListView;
    @FXML private Label statusLabel;
    @FXML private Label userInfoLabel;

    private final ObservableList<Item> items = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Hiển thị thông tin user
        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        if (user != null) {
            userInfoLabel.setText("Xin chào, " + user.getUsername() + " (Người bán)");
        }

        // Custom cell
        itemListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String type = item.getItemType() != null ? item.getItemType().name() : "N/A";
                    setText(item.getName() + "  |  " + type
                            + "  |  " + String.format("%,.0f", item.getStartingPrice()) + " VNĐ");
                }
            }
        });
        itemListView.setItems(items);

        // Load sản phẩm của seller
        handleRefresh();
    }

    @FXML
    public void navigateToCreate() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/create_item.fxml"));
            Stage stage = (Stage) itemListView.getScene().getWindow();
            Scene scene = new Scene(root);
            String css = getClass().getResource("/css/style.css").toExternalForm();
            scene.getStylesheets().add(css);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Lỗi khi mở trang tạo phiên đấu giá!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    @FXML
    @SuppressWarnings("unchecked")
    public void handleRefresh() {
        try {
            AuthUserData user = NetworkClient.getInstance().getCurrentUser();
            GetItemsRequest req = new GetItemsRequest(user != null ? user.getUserId() : null, null);
            ClientRequest request = new ClientRequest(Action.GET_ITEMS, req);
            ClientResponse res = NetworkClient.getInstance().sendRequest(request);

            if (res.isSuccess() && res.getData() != null) {
                List<Item> serverItems = (List<Item>) res.getData();
                items.clear();
                items.addAll(serverItems);
                statusLabel.setText("Đã tải " + serverItems.size() + " sản phẩm của bạn");
                statusLabel.setStyle("-fx-text-fill: #2ecc71;");
            } else {
                statusLabel.setText(res.getMessage());
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            }
        } catch (Exception e) {
            statusLabel.setText("Lỗi: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    @FXML
    public void handleLogout() {
        try {
            NetworkClient.getInstance().setCurrentUser(null);
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) itemListView.getScene().getWindow();
            Scene scene = new Scene(root);
            String css = getClass().getResource("/css/style.css").toExternalForm();
            scene.getStylesheets().add(css);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
