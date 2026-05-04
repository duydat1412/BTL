package com.auction.client.controller;

import com.auction.common.entity.Auction;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AuctionDetailController {

    @FXML
    private Label nameLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private Label timeLabel;
    @FXML
    private TextField bidAmountField;
    @FXML
    private int secondsRemaining;
    private Timeline countdownTimeline;
    public void initialize() {
    }
    public void setData(Auction auction) {
        //Hiển thị thông tin
        nameLabel.setText(auction.getItemId());
        priceLabel.setText("Giá Hiện Tại: "+auction.getCurrentPrice());

        this.secondsRemaining = 3600;
        startTimer();
    }
    private void startTimer(){
        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1),e->{
            secondsRemaining--;
            int mins = secondsRemaining/60;
            int secs = secondsRemaining%60;
            timeLabel.setText(String.format("%02d:%02d",mins,secs));

            if (secondsRemaining <= 0) {

                countdownTimeline.stop();
                timeLabel.setText("Kết thúc phiên");
            }
        }));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }
    @FXML
    public void handlePlaceBid(){
        // gửi requast đặt giá
        String amount = bidAmountField.getText();
        System.out.println("Đang gửi đề nghị đặt giá" + amount);
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
    //12345
}