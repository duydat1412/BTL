package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;
import com.auction.common.entity.Item;
import com.auction.common.message.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AuctionDetailController {

    @FXML private Label nameLabel;
    @FXML private Label priceLabel;
    @FXML private Label timeLabel;
    @FXML private TextField bidAmountField;
    @FXML private Label descLabel;
    @FXML private Button placeBidBtn;

    @FXML private ListView<String> bidHistoryView;
    @FXML private Label bidHistoryStatus;

    @FXML private TextField maxBidField;
    @FXML private TextField incrementField;
    @FXML private Button autoBidBtn;
    @FXML private Label balanceLabel;

    private Auction currentAuction;
    private Item currentItem;
    private NetworkClient.PushListener pushListener;
    private Timeline countdownTimeline;
    private boolean autoBidEnabled = false;

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
        priceLabel.setText(String.format("%,.0f VNĐ", price));
        if (descLabel != null) {
            descLabel.setText(desc);
        }

        // Load balance
        loadBalance();

        // Start countdown timer
        startCountdown();

        // Load bid history
        loadBidHistory();

        // Register push listener
        registerPushListener();
    }

    private void startCountdown() {
        if (currentAuction == null || currentAuction.getEndTime() == null) return;

        if (countdownTimeline != null) countdownTimeline.stop();

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateCountdown()));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void updateCountdown() {
        if (currentAuction == null || currentAuction.getEndTime() == null) return;

        LocalDateTime end = currentAuction.getEndTime();
        java.time.Duration remaining = java.time.Duration.between(LocalDateTime.now(), end);

        if (remaining.isNegative() || remaining.isZero()) {
            // Không tự disable — chờ server gửi AUCTION_ENDED (anti-sniping có thể gia hạn)
            timeLabel.setText("Đang chờ server xác nhận kết thúc...");
            timeLabel.setStyle("-fx-text-fill: #eab308; -fx-font-weight: bold;");
            return;
        }

        long h = remaining.toHours();
        long m = remaining.toMinutes() % 60;
        long s = remaining.getSeconds() % 60;
        timeLabel.setText(String.format("Còn lại: %02d:%02d:%02d", h, m, s));

        // Color: xanh (>1h), vàng (<1h), đỏ (<5 phút)
        if (remaining.toHours() >= 1) {
            timeLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
        } else if (remaining.toMinutes() >= 5) {
            timeLabel.setStyle("-fx-text-fill: #eab308; -fx-font-weight: bold;");
        } else {
            timeLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        }
    }

    private void loadBidHistory() {
        if (currentAuction == null) return;
        bidHistoryStatus.setText("Đang tải...");
        ClientRequest req = new ClientRequest(Action.GET_BID_HISTORY, new GetBidHistoryRequest(currentAuction.getId()));
        NetworkClient.getInstance().sendRequestAsync(req).thenAccept(res -> Platform.runLater(() -> {
            if (res.isSuccess() && res.getData() != null) {
                List<?> rawList = (List<?>) res.getData();
                List<BidTransaction> bids = rawList.stream()
                        .filter(BidTransaction.class::isInstance)
                        .map(BidTransaction.class::cast)
                        .toList();
                updateBidHistoryView(bids);
                bidHistoryStatus.setText(bids.size() + " lượt đặt giá");
            } else {
                bidHistoryStatus.setText("Không có dữ liệu");
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> bidHistoryStatus.setText("Lỗi tải!"));
            return null;
        });
    }

    /** Load bid history không block — dùng cho push listener để tránh requestLock contention */
    private void loadBidHistoryAsync() {
        if (currentAuction == null) return;
        ClientRequest req = new ClientRequest(Action.GET_BID_HISTORY, new GetBidHistoryRequest(currentAuction.getId()));
        NetworkClient.getInstance().sendRequestAsync(req).thenAccept(res -> {
            if (res.isSuccess() && res.getData() != null) {
                List<?> rawList = (List<?>) res.getData();
                List<BidTransaction> bids = rawList.stream()
                        .filter(BidTransaction.class::isInstance)
                        .map(BidTransaction.class::cast)
                        .toList();
                Platform.runLater(() -> {
                    updateBidHistoryView(bids);
                    bidHistoryStatus.setText(bids.size() + " lượt đặt giá");
                });
            }
        });
    }

    private void updateBidHistoryView(List<BidTransaction> bids) {
        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        String currentUserId = user != null ? user.getUserId() : "";

        bidHistoryView.getItems().clear();
        int seq = bids.size();
        for (int i = bids.size() - 1; i >= 0; i--) {
            BidTransaction bid = bids.get(i);
            String formatted = String.format("%d. %s: %,.0f VNĐ - %s",
                    seq--,
                    bid.getBidderId(),
                    bid.getBidAmount(),
                    bid.getBidTime() != null ? bid.getBidTime().format(DateTimeFormatter.ofPattern("dd/MM HH:mm:ss")) : "");
            bidHistoryView.getItems().add(formatted);
        }

        // Highlight current user's bids using cell factory
        bidHistoryView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Check if this bid is by current user (by checking bidderId in text)
                    if (currentUserId != null && !currentUserId.isEmpty() && item.contains(currentUserId)) {
                        setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #0f172a;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }

    private void loadBalance() {
        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        if (user == null || balanceLabel == null) return;
        ClientRequest req = new ClientRequest(Action.GET_BALANCE, user.getUserId());
        NetworkClient.getInstance().sendRequestAsync(req).thenAccept(res -> Platform.runLater(() -> {
            if (res.isSuccess() && res.getData() != null) {
                double balance = (double) res.getData();
                balanceLabel.setText("Số dư: " + String.format("%,.0f", balance) + " VNĐ");
                // Cập nhật balance trong AuthUserData để dùng cho client-side check
                user.setBalance(balance);
            }
        }));
    }

    private void registerPushListener() {
        pushListener = pushMsg -> {
            // Cập nhật endTime + giá từ push (quan trọng cho anti-sniping và auto-bid)
            if ((pushMsg.getType() == ServerPushMessage.PushType.NEW_BID
                    || pushMsg.getType() == ServerPushMessage.PushType.PRICE_UPDATE)
                    && pushMsg.getData() instanceof Auction updatedAuction) {

                if (currentAuction != null
                        && updatedAuction.getId().equals(currentAuction.getId())) {
                    Platform.runLater(() -> {
                        currentAuction.setCurrentPrice(updatedAuction.getCurrentPrice());
                        if (updatedAuction.getEndTime() != null) {
                            currentAuction.setEndTime(updatedAuction.getEndTime());
                        }
                        priceLabel.setText(String.format("%,.0f VNĐ", updatedAuction.getCurrentPrice()));
                        // Load bid history bất đồng bộ — không block UI
                        loadBidHistoryAsync();
                    });
                }
            } else if (pushMsg.getType() == ServerPushMessage.PushType.AUCTION_ENDED) {
                String pushMessage = pushMsg.getMessage();
                if (pushMsg.getData() instanceof Auction endedAuction
                        && currentAuction != null
                        && endedAuction.getId().equals(currentAuction.getId())) {
                    Platform.runLater(() -> {
                        timeLabel.setText(pushMessage != null ? pushMessage : "Đã kết thúc");
                        timeLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                        placeBidBtn.setDisable(true);
                        bidAmountField.setDisable(true);
                        maxBidField.setDisable(true);
                        incrementField.setDisable(true);
                        autoBidBtn.setDisable(true);
                        if (countdownTimeline != null) countdownTimeline.stop();
                        loadBidHistory();
                        loadBalance();
                    });
                }
            }
        };
        NetworkClient.getInstance().addPushListener(pushListener);
    }

    @FXML
    public void handlePlaceBid() {
        String amountStr = bidAmountField.getText();

        if (amountStr.isEmpty()) {
            showStatus("Chưa nhập giá!", true);
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            String auctionId = currentAuction != null ? currentAuction.getId() : (currentItem != null ? currentItem.getId() : "");

            AuthUserData user = NetworkClient.getInstance().getCurrentUser();
            if (user == null) {
                showStatus("Vui lòng đăng nhập lại!", true);
                return;
            }

            // Client-side balance check
            double balance = user.getBalance();
            if (balance < amount) {
                showStatus("Số dư không đủ! Số dư: " + String.format("%,.0f", balance) + " VNĐ", true);
                return;
            }

            PlaceBidRequest req = new PlaceBidRequest(auctionId, user.getUserId(), amount, false);
            ClientRequest request = new ClientRequest(Action.PLACE_BID, req);

            showStatus("Đang xử lý...", false);
            NetworkClient.getInstance().sendRequestAsync(request).thenAccept(res -> Platform.runLater(() -> {
                if (res.isSuccess()) {
                    showStatus("Đặt giá thành công: " + String.format("%,.0f", amount) + " VNĐ", false);
                    bidAmountField.clear();
                    loadBidHistory();
                } else {
                    showStatus("Lỗi: " + res.getMessage(), true);
                }
            })).exceptionally(ex -> {
                Platform.runLater(() -> showStatus("Lỗi kết nối server!", true));
                return null;
            });

        } catch (NumberFormatException ex) {
            showStatus("Giá trị không hợp lệ!", true);
        }
    }

    @FXML
    public void handleAutoBid() {
        if (autoBidEnabled) {
            // Disable auto-bid
            removeAutoBid();
        } else {
            // Enable auto-bid
            enableAutoBid();
        }
    }

    private void enableAutoBid() {
        String maxBidStr = maxBidField.getText();
        if (maxBidStr.isEmpty()) {
            showStatus("Nhập giá tối đa cho auto-bid!", true);
            return;
        }

        String incStr = incrementField.getText();
        if (incStr.isEmpty()) {
            showStatus("Nhập bước tăng giá cho auto-bid!", true);
            return;
        }

        try {
            double maxBid = Double.parseDouble(maxBidStr);
            double increment = Double.parseDouble(incStr);

            if (increment <= 0) {
                showStatus("Bước tăng phải lớn hơn 0!", true);
                return;
            }

            AuthUserData user = NetworkClient.getInstance().getCurrentUser();
            if (user == null) {
                showStatus("Vui lòng đăng nhập lại!", true);
                return;
            }

            // Client-side balance check
            double balance = user.getBalance();
            if (balance < maxBid) {
                showStatus("Số dư không đủ cho auto-bid! Số dư: " + String.format("%,.0f", balance) + " VNĐ", true);
                return;
            }

            String auctionId = currentAuction != null ? currentAuction.getId() : "";
            RegisterAutoBidRequest req = new RegisterAutoBidRequest(auctionId, user.getUserId(), maxBid, increment);
            ClientRequest request = new ClientRequest(Action.REGISTER_AUTO_BID, req);

            showStatus("Đang đăng ký auto-bid...", false);
            NetworkClient.getInstance().sendRequestAsync(request).thenAccept(res -> Platform.runLater(() -> {
                if (res.isSuccess()) {
                    autoBidEnabled = true;
                    autoBidBtn.setText("Hủy auto-bid");
                    autoBidBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6;");
                    maxBidField.setDisable(true);
                    incrementField.setDisable(true);
                    showStatus("Auto-bid đã bật!", false);
                } else {
                    showStatus("Lỗi: " + res.getMessage(), true);
                }
            })).exceptionally(ex -> {
                Platform.runLater(() -> showStatus("Lỗi kết nối server!", true));
                return null;
            });

        } catch (NumberFormatException ex) {
            showStatus("Giá tối đa không hợp lệ!", true);
        }
    }

    private void removeAutoBid() {
        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        if (user == null) return;

        String auctionId = currentAuction != null ? currentAuction.getId() : "";
        RemoveAutoBidRequest req = new RemoveAutoBidRequest(auctionId, user.getUserId());
        ClientRequest request = new ClientRequest(Action.REMOVE_AUTO_BID, req);

        NetworkClient.getInstance().sendRequestAsync(request).thenAccept(res -> Platform.runLater(() -> {
            if (res.isSuccess()) {
                autoBidEnabled = false;
                autoBidBtn.setText("Bật auto-bid");
                autoBidBtn.setStyle("");
                maxBidField.setDisable(false);
                incrementField.setDisable(false);
                showStatus("Đã hủy auto-bid", false);
            } else {
                showStatus("Lỗi: " + res.getMessage(), true);
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> showStatus("Lỗi kết nối server!", true));
            return null;
        });
    }

    private void showStatus(String msg, boolean isError) {
        timeLabel.setText(msg);
        timeLabel.setStyle(isError
                ? "-fx-text-fill: #ef4444; -fx-font-weight: bold;"
                : "-fx-text-fill: #22c55e; -fx-font-weight: bold;");
    }

    @FXML
    public void goBack() {
        if (countdownTimeline != null) countdownTimeline.stop();
        if (pushListener != null) {
            NetworkClient.getInstance().removePushListener(pushListener);
        }
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/auction_list.fxml"));
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
