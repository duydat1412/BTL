package com.auction.client.controller;

import java.time.LocalDateTime;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import com.auction.common.entity.Auction;
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
    }
    private void openDetail() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auction_detail.fxml"));
            Parent root = loader.load();
            AuctionDetailController controller = loader.getController();
            Auction selected = listView.getSelectionModel().getSelectedItem();

            if (selected == null ) return;
            controller.setData(selected);

            Stage stage = (Stage) listView.getScene().getWindow();
            stage.setScene(new Scene(root));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
