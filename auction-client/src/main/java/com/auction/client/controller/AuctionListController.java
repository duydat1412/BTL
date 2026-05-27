package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.client.util.NotificationToast;
import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.message.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AuctionListController {

    @FXML
    private ListView<Auction> listView;

    @FXML
    private ListView<Auction> wonListView;

    @FXML
    private Label userInfoLabel;

    @FXML
    private Label balanceLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label wonStatusLabel;

    private NetworkClient.PushListener pushListener;

    @FXML
    public void initialize() {
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Auction item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle() + " - Giá: " + String.format("%,.0f", item.getCurrentPrice()) + " VNĐ");
                }
            }
        });

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                openDetail(newVal);
            }
        });

        wonListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Auction item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle() + " - Giá thắng: " + String.format("%,.0f", item.getCurrentPrice()) + " VNĐ");
                }
            }
        });

        wonListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                openDetail(newVal);
            }
        });

        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        if (user != null && userInfoLabel != null) {
            userInfoLabel.setText("Xin chào, " + user.getUsername());
        }

        loadAuctions();
        loadWonAuctions();
        loadBalance();
        registerPushListener();
    }

    private void loadAuctions() {
        statusLabel.setText("Đang tải...");
        ClientRequest req = new ClientRequest(Action.GET_AUCTIONS, null);
        NetworkClient client = NetworkClient.getInstance();
        if (client == null) {
            statusLabel.setText("Lỗi: chưa kết nối");
            return;
        }
        client.sendRequestAsync(req).thenAccept(res -> Platform.runLater(() -> {
            if (res.isSuccess() && res.getData() != null) {
                List<?> rawList = (List<?>) res.getData();
                List<Auction> auctions = rawList.stream()
                        .filter(Auction.class::isInstance)
                        .map(Auction.class::cast)
                        .filter(a -> a.getStatus() != AuctionStatus.FINISHED)
                        .toList();
                listView.getItems().setAll(auctions);
                statusLabel.setText("Có " + auctions.size() + " phiên đang diễn ra");
            } else {
                String msg = res != null ? res.getMessage() : "null response";
                statusLabel.setText("Lỗi tải dữ liệu: " + msg);
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> statusLabel.setText("Lỗi kết nối server!"));
            return null;
        });
    }

    private void loadWonAuctions() {
        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        if (user == null) {
            wonStatusLabel.setText("Chưa đăng nhập");
            return;
        }
        wonStatusLabel.setText("Đang tải...");
        ClientRequest req = new ClientRequest(Action.GET_AUCTIONS, new GetAuctionsRequest(null, "FINISHED"));
        NetworkClient.getInstance().sendRequestAsync(req).thenAccept(res -> Platform.runLater(() -> {
            if (res.isSuccess() && res.getData() != null) {
                List<?> rawList = (List<?>) res.getData();
                List<Auction> finishedAuctions = rawList.stream()
                        .filter(Auction.class::isInstance)
                        .map(Auction.class::cast)
                        .toList();
                // Check each auction if current user is winner
                List<Auction> wonAuctions = new ArrayList<>();
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (Auction auction : finishedAuctions) {
                    CompletableFuture<Void> f = new CompletableFuture<>();
                    futures.add(f);
                    ClientRequest bidReq = new ClientRequest(Action.GET_BID_HISTORY,
                            new GetBidHistoryRequest(auction.getId()));
                    NetworkClient.getInstance().sendRequestAsync(bidReq).thenAccept(bidRes -> {
                        if (bidRes.isSuccess() && bidRes.getData() != null) {
                            List<?> bidRaw = (List<?>) bidRes.getData();
                            List<BidTransaction> bids = bidRaw.stream()
                                    .filter(BidTransaction.class::isInstance)
                                    .map(BidTransaction.class::cast)
                                    .toList();
                            if (!bids.isEmpty()) {
                                BidTransaction lastBid = bids.get(bids.size() - 1);
                                if (user.getUserId().equals(lastBid.getBidderId())) {
                                    synchronized (wonAuctions) {
                                        wonAuctions.add(auction);
                                    }
                                }
                            }
                        }
                        f.complete(null);
                    });
                }
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() ->
                    Platform.runLater(() -> {
                        wonListView.getItems().setAll(wonAuctions);
                        wonStatusLabel.setText("Đã thắng " + wonAuctions.size() + " phiên");
                    })
                );
            } else {
                wonStatusLabel.setText("Không có phiên nào");
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> wonStatusLabel.setText("Lỗi kết nối server!"));
            return null;
        });
    }

    private void registerPushListener() {
        pushListener = pushMsg -> {
            if (pushMsg.getType() == ServerPushMessage.PushType.NEW_BID) {
                Platform.runLater(() -> {
                    loadAuctions();
                    loadWonAuctions();
                    loadBalance();
                    Stage stage = (Stage) listView.getScene().getWindow();
                    NotificationToast.show(stage, "Có lượt đặt giá mới!", false);
                });
            } else if (pushMsg.getType() == ServerPushMessage.PushType.AUCTION_ENDED) {
                Platform.runLater(() -> {
                    loadAuctions();
                    loadWonAuctions();
                    loadBalance();
                    String title = pushMsg.getData() instanceof Auction a ? a.getTitle() : "";
                    Stage stage = (Stage) listView.getScene().getWindow();
                    NotificationToast.show(stage, "Phiên \"" + title + "\" đã kết thúc!", true);
                });
            } else if (pushMsg.getType() == ServerPushMessage.PushType.AUCTION_STARTED
                    || pushMsg.getType() == ServerPushMessage.PushType.PRICE_UPDATE
                    || pushMsg.getType() == ServerPushMessage.PushType.AUCTION_CREATED) {
                Platform.runLater(() -> {
                    loadAuctions();
                    loadWonAuctions();
                });
            }
        };
        NetworkClient.getInstance().addPushListener(pushListener);
    }

    @FXML
    public void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Đăng xuất");
        confirm.setHeaderText("Xác nhận đăng xuất");
        confirm.setContentText("Bạn có chắc muốn đăng xuất?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (pushListener != null) {
                NetworkClient.getInstance().removePushListener(pushListener);
            }
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
                Stage stage = (Stage) listView.getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleRefresh() {
        loadAuctions();
        loadWonAuctions();
        loadBalance();
    }

    private void loadBalance() {
        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        if (user == null || balanceLabel == null) return;
        ClientRequest req = new ClientRequest(Action.GET_BALANCE, user.getUserId());
        NetworkClient.getInstance().sendRequestAsync(req).thenAccept(res -> Platform.runLater(() -> {
            if (res.isSuccess() && res.getData() != null) {
                double balance = (double) res.getData();
                balanceLabel.setText("Số dư: " + String.format("%,.0f", balance) + " VNĐ");
            }
        }));
    }

    @FXML
    public void handleTopUp() {
        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        if (user == null) return;

        TextInputDialog dialog = new TextInputDialog("100000");
        dialog.setTitle("Nạp tiền");
        dialog.setHeaderText("Nhập số tiền muốn nạp:");
        dialog.setContentText("Số tiền (VNĐ):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    showAlert("Số tiền phải lớn hơn 0!");
                    return;
                }
                TopUpRequest req = new TopUpRequest(user.getUserId(), amount);
                NetworkClient.getInstance().sendRequestAsync(new ClientRequest(Action.TOP_UP, req))
                    .thenAccept(res -> Platform.runLater(() -> {
                        if (res.isSuccess()) {
                            NotificationToast.show((Stage) balanceLabel.getScene().getWindow(),
                                    "Nạp thành công: " + String.format("%,.0f", amount) + " VNĐ", false);
                            loadBalance();
                        } else {
                            showAlert("Lỗi: " + res.getMessage());
                        }
                    }));
            } catch (NumberFormatException e) {
                showAlert("Số tiền không hợp lệ!");
            }
        });
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void openDetail(Auction selected) {
        if (pushListener != null) {
            NetworkClient.getInstance().removePushListener(pushListener);
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auction_detail.fxml"));
            Parent root = loader.load();
            AuctionDetailController controller = loader.getController();
            controller.setData(selected, null);
            Stage stage = (Stage) listView.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            System.err.println("Lỗi khi mở màn hình chi tiết: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("Lỗi: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }
}
