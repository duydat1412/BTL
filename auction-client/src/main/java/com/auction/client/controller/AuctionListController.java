package com.auction.client.controller;

import java.time.LocalDateTime;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import com.auction.common.entity.Auction;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import com.auction.client.controller.AuctionDetailController;


public class AuctionListController {

    @FXML
    private ListView<Auction> listView;

    @FXML
    public void initialize() {
        listView.getItems().addAll(
                new Auction("ITEM001", "SELLER001", "iPhone 15", 10000000, LocalDateTime.now(), LocalDateTime.now().plusDays(1)),
                new Auction("ITEM002", "SELLER001", "MacBook M2", 20000000, LocalDateTime.now(), LocalDateTime.now().plusDays(1))
        );
        listView.setCellFactory(new Callback<ListView<Auction>, ListCell<Auction>>() {
            @Override
            public ListCell<Auction> call(ListView<Auction> param) {
                return new ListCell<Auction>() {
                    @Override
                    protected void updateItem(Auction item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getTitle() + " - Giá: " + String.format("%,.0f", (double)item.getCurrentPrice()) + " VND");
                        }
                    }
                };
            }
        });
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                openDetail(newValue);
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
