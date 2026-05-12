package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.common.entity.Item;
import com.auction.common.message.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class AuctionListController {

    @FXML private ListView<Item> listView;
    @FXML private Label statusLabel;
    @FXML private Label userInfoLabel;

    private final ObservableList<Item> items = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Hiển thị thông tin user
        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        if (user != null) {
            userInfoLabel.setText("Xin chào, " + user.getUsername() + " (" + user.getRole() + ")");
        }

        // Custom cell hiển thị từng item
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String type = item.getItemType() != null ? item.getItemType().name() : "N/A";
                    setText(item.getName() + "  |  Loại: " + type
                            + "  |  Giá khởi điểm: " + String.format("%,.0f", item.getStartingPrice()) + " VNĐ");
                }
            }
        });

        listView.setItems(items);
        
        // Xử lý sự kiện click đúp vào một sản phẩm để xem chi tiết
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Item selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openAuctionDetail(selected);
                }
            }
        });

        // Load dữ liệu ngay khi mở màn hình
        handleRefresh();
    }
    
    private void openAuctionDetail(Item item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auction_detail.fxml"));
            Parent root = loader.load();
            
            AuctionDetailController detailController = loader.getController();
            
            // Tạo một Auction ảo từ Item vì phía Server chưa hoàn thiện lấy Auction
            com.auction.common.entity.Auction fakeAuction = new com.auction.common.entity.Auction();
            fakeAuction.setId(item.getId()); // Dùng ID của item tạm
            fakeAuction.setItemId(item.getId());
            fakeAuction.setTitle(item.getName());
            fakeAuction.setStartPrice(item.getStartingPrice());
            fakeAuction.setCurrentPrice(item.getStartingPrice());
            
            detailController.setData(fakeAuction, item);
            
            Stage stage = (Stage) listView.getScene().getWindow();
            Scene scene = new Scene(root);
            String css = getClass().getResource("/css/style.css").toExternalForm();
            scene.getStylesheets().add(css);
            stage.setScene(scene);
        } catch (Exception e) {
            statusLabel.setText("Không thể mở chi tiết phiên đấu");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            e.printStackTrace();
        }
    }

    @FXML
    @SuppressWarnings("unchecked")
    public void handleRefresh() {
        try {
            NetworkClient client = NetworkClient.getInstance();
            GetItemsRequest req = new GetItemsRequest(null, null);
            ClientRequest request = new ClientRequest(Action.GET_ITEMS, req);
            ClientResponse res = client.sendRequest(request);

            if (res.isSuccess() && res.getData() != null) {
                List<Item> serverItems = (List<Item>) res.getData();
                items.clear();
                items.addAll(serverItems);
                statusLabel.setText("Đã tải " + serverItems.size() + " sản phẩm");
                statusLabel.setStyle("-fx-text-fill: #2ecc71;");
            } else {
                statusLabel.setText(res.getMessage());
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            }
        } catch (Exception e) {
            statusLabel.setText("Lỗi kết nối: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    @FXML
    public void handleLogout() {
        try {
            NetworkClient.getInstance().setCurrentUser(null);
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) listView.getScene().getWindow();
            Scene scene = new Scene(root);
            String css = getClass().getResource("/css/style.css").toExternalForm();
            scene.getStylesheets().add(css);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
