package com.auction.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class AuctionClientApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        Scene scene = new Scene(loader.load());

        String css = getClass().getResource("/CSS/style.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("Hệ thống Đấu giá Trực tuyến");
        stage.setScene(scene);

        stage.setOnCloseRequest(e -> {
            System.exit(0);
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}