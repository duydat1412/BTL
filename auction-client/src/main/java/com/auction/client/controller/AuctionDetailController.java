package com.auction.client.controller;

import com.auction.common.entity.Auction;
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

    public void setData(Auction auction) {
        if (auction == null) {
            nameLabel.setText("Không có dữ liệu");
            return;
        }

        nameLabel.setText(auction.getTitle());
        priceLabel.setText("Giá hiện tại: " + auction.getCurrentPrice());

        timeLabel.setText("Đang chờ dữ liệu từ server...");
    }

    @FXML
    public void handlePlaceBid() {
        String amount = bidAmountField.getText();

        if (amount.isEmpty()) {
            System.out.println("Chưa nhập giá");
            return;
        }

        System.out.println("Gửi yêu cầu đặt giá: " + amount);
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