package com.auction.client.util;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

public class NotificationToast {

    public static void show(Window window, String message, boolean isError) {
        Platform.runLater(() -> {
            Popup popup = new Popup();
            popup.setAutoFix(true);

            Label msgLabel = new Label(message);
            msgLabel.setWrapText(true);
            msgLabel.setMaxWidth(350);
            
            // Apply custom styles directly to match the premium dark theme
            if (isError) {
                msgLabel.setStyle("-fx-background-color: #ff4d4f; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12px 24px; -fx-background-radius: 20px; -fx-effect: dropshadow(gaussian, rgba(255, 77, 79, 0.3), 10, 0, 0, 4);");
            } else {
                msgLabel.setStyle("-fx-background-color: #52c41a; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12px 24px; -fx-background-radius: 20px; -fx-effect: dropshadow(gaussian, rgba(82, 196, 26, 0.3), 10, 0, 0, 4);");
            }

            StackPane root = new StackPane(msgLabel);
            root.setStyle("-fx-background-color: transparent;");
            popup.getContent().add(root);

            popup.setOnShown(e -> {
                double targetX = window.getX() + window.getWidth() - msgLabel.getWidth() - 30;
                double targetY = window.getY() + 75; // Position below the navigation bar
                popup.setX(targetX);
                popup.setY(targetY);

                // Entry animation: Slide in from the right, fade in
                root.setTranslateX(150);
                root.setOpacity(0.0);

                TranslateTransition slideIn = new TranslateTransition(Duration.millis(350), root);
                slideIn.setToX(0);
                slideIn.setInterpolator(Interpolator.EASE_OUT);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(350), root);
                fadeIn.setToValue(1.0);

                ParallelTransition showAnim = new ParallelTransition(slideIn, fadeIn);
                showAnim.play();

                // Exit animation: Trigger after delay
                showAnim.setOnFinished(ev -> {
                    new Thread(() -> {
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException ignored) {}
                        Platform.runLater(() -> {
                            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), root);
                            slideOut.setToX(150);
                            slideOut.setInterpolator(Interpolator.EASE_IN);

                            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root);
                            fadeOut.setToValue(0.0);

                            ParallelTransition hideAnim = new ParallelTransition(slideOut, fadeOut);
                            hideAnim.setOnFinished(evt -> popup.hide());
                            hideAnim.play();
                        });
                    }).start();
                });
            });

            popup.show(window);
        });
    }
}
