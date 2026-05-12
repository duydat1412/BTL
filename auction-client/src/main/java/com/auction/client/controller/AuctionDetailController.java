package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.common.entity.Auction;
import com.auction.common.entity.Item;
import com.auction.common.message.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AuctionDetailController {

    @FXML private Label nameLabel;
    @FXML private Label priceLabel;
    @FXML private Label timeLabel;
    @FXML private TextField bidAmountField;
    @FXML private Label descLabel;

    private Auction currentAuction;
    private Item currentItem;

    public void setData(Auction auction, Item item) {
        this.currentAuction = auction;
        this.currentItem = item;
        
        if (auction == null && item == null) {
            nameLabel.setText("Không có dữ liệu");
            return;
        }
        
        String title = auction != null ? auction.getTitle() : item.getName();
        double price = auction != null ? auction.getCurrentPrice() : item.getStartingPrice();
        String desc = item != null && item.getDescription() != null ? item.getDescription() : "Không có mô tả chi tiết.";
        if (item != null) desc = item.getDetailedDescription();

        nameLabel.setText(title);
        priceLabel.setText(String.format("$%,.0f", price));
        if (descLabel != null) {
            descLabel.setText(desc);
        }
        
        timeLabel.setText(auction != null && auction.getEndTime() != null 
            ? "Kết thúc lúc: " + auction.getEndTime().toString() 
            : "Đang chờ dữ liệu thời gian...");
    }

    @FXML
    public void handlePlaceBid() {
        String amountStr = bidAmountField.getText();

        if (amountStr.isEmpty()) {
            timeLabel.setText("Chưa nhập giá!");
            timeLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            String auctionId = currentAuction != null ? currentAuction.getId() : (currentItem != null ? currentItem.getId() : "");
            
            AuthUserData user = NetworkClient.getInstance().getCurrentUser();
            if (user == null) {
                timeLabel.setText("Vui lòng đăng nhập lại!");
                return;
            }

            PlaceBidRequest req = new PlaceBidRequest(auctionId, user.getId(), amount, false);
            ClientRequest request = new ClientRequest(Action.PLACE_BID, req);
            
            NetworkClient client = NetworkClient.getInstance();
            ClientResponse res = client.sendRequest(request);

            if (res.isSuccess()) {
                timeLabel.setText("Đặt giá thành công: " + amount);
                timeLabel.setStyle("-fx-text-fill: #2ecc71;");
                priceLabel.setText(String.format("$%,.0f", amount));
                if (currentAuction != null) currentAuction.setCurrentPrice(amount);
            } else {
                timeLabel.setText("Lỗi: " + res.getMessage());
                timeLabel.setStyle("-fx-text-fill: #e74c3c;");
            }
        } catch (NumberFormatException ex) {
            timeLabel.setText("Giá trị không hợp lệ!");
            timeLabel.setStyle("-fx-text-fill: #e74c3c;");
        } catch (Exception e) {
            timeLabel.setText("Lỗi kết nối server!");
            timeLabel.setStyle("-fx-text-fill: #e74c3c;");
            e.printStackTrace();
        }
    }

    @FXML
    public void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/auction_list.fxml"));
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}