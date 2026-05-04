package com.auction.client.controller;

import com.auction.common.entity.Auction;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AuctionDetailController {

    @FXML private Label nameLabel;
    @FXML private Label priceLabel;
    @FXML private Label timeLabel;
    @FXML private TextField bidAmountField;

    private int secondsRemaining ;
    private Timeline timeline;

    public void setData(Auction auction) {
        nameLabel.setText(auction.getTitle());
        priceLabel.setText("Giá: " + auction.getCurrentPrice());

        secondsRemaining = 3600;
        startTimer();
    }
    private void startTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsRemaining--;

            int min = secondsRemaining / 60;
            int sec = secondsRemaining % 60;

            timeLabel.setText(String.format("%02d:%02d", min, sec));

            if (secondsRemaining <= 0) {
                timeline.stop();
                timeLabel.setText("Hết giờ");
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    @FXML
    private void handlePlaceBid() {
        String amount = bidAmountField.getText();

        if (amount.isEmpty()) {
            System.out.println("Chưa nhập giá");
            return;
        }

        System.out.println("Đặt giá: " + amount);
    }

    @FXML
    private void goBack() {
        try {
            if (timeline != null) timeline.stop();

            Parent root = FXMLLoader.load(getClass().getResource("/view/auction_list.fxml"));
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}