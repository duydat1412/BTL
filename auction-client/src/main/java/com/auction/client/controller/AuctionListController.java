package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.common.entity.Auction;
import com.auction.common.message.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.control.ListCell;

import java.util.List;

public class AuctionListController {

    @FXML
    private ListView<Auction> listView;

    @FXML
    private Label userInfoLabel;

    @FXML
    private Label statusLabel;

    private NetworkClient.PushListener pushListener;

    @FXML
    public void initialize() {
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Auction item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle() + " - Giá: " + item.getCurrentPrice());
                }
            }
        });

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                openDetail(newVal);
            }
        });

        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        if (user != null && userInfoLabel != null) {
            userInfoLabel.setText("Xin chào, " + user.getUsername());
        }

        loadAuctions();
        registerPushListener();
    }

    private void loadAuctions() {
        statusLabel.setText("Đang tải...");
        ClientRequest req = new ClientRequest(Action.GET_AUCTIONS, null);
        NetworkClient.getInstance().sendRequestAsync(req).thenAccept(res -> Platform.runLater(() -> {
            if (res.isSuccess() && res.getData() != null) {
                List<?> rawList = (List<?>) res.getData();
                List<Auction> auctions = rawList.stream()
                        .filter(Auction.class::isInstance)
                        .map(Auction.class::cast)
                        .toList();
                listView.getItems().setAll(auctions);
                statusLabel.setText("Có " + auctions.size() + " phiên đấu giá");
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
                    || pushMsg.getType() == ServerPushMessage.PushType.NEW_BID
                    || pushMsg.getType() == ServerPushMessage.PushType.PRICE_UPDATE) {
                Platform.runLater(this::loadAuctions);
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
            Stage stage = (Stage) listView.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRefresh() {
        loadAuctions();
    }

    private void openDetail(Auction selected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auction_detail.fxml"));
            Parent root = loader.load();
            AuctionDetailController controller = loader.getController();
            controller.setData(selected, null);
            Stage stage = (Stage) listView.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            System.err.println("Lỗi khi mở màn hình chi tiết: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("Lỗi: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }
}
