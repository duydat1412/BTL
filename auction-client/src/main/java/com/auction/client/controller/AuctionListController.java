package com.auction.client.controller;

import java.time.LocalDateTime;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import com.auction.common.entity.Auction;
import javafx.scene.control.ListCell;
import javafx.util.Callback;

public class AuctionListController {

    @FXML
    private ListView<Auction> listView;

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
    }
    private void openDetail(Auction selected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auction_detail.fxml"));
            Parent root = loader.load();
            AuctionDetailController controller = loader.getController();
            controller.setData(selected);
            Stage stage = (Stage) listView.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            System.err.println("Lỗi khi mở màn hình chi tiết: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
