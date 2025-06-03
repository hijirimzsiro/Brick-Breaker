package com.mycompany.brickbreaker;

import java.io.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.json.JSONObject;

public class App extends Application {

    private final String serverURL = "http://192.168.87.27:5000";
    private final String playerId = SystemInfo.getPlayerID();

    private void resetGameStatus() {
        try {

            URL url = new URL(serverURL + "/reset_status");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.getResponseCode(); // 強制發送
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Ball ball;
    private Paddle paddle;
    private long lastCheckTime = 0;
    private boolean gameRunning = true;

    private void checkOpponentStatus() {
        new Thread(() -> {
            try {
                URL url = new URL(serverURL + "/get_status");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONObject status = new JSONObject(sb.toString());
                String opponent = SystemInfo.playerName.equals("player1") ? "player2" : "player1";

                if (status.has(opponent) && status.getString(opponent).equals("finished")) {
                    Platform.runLater(() -> {
                        if (gameRunning) {
                            endGame();
                        }
                    });
                }
            } catch (Exception e) {
                System.out.println("⚠️ 對手狀態輪詢失敗：" + e.getMessage());
            }
        }).start();
    }
    private List<Brick> bricks = new ArrayList<>();
    private int score = 0;
    private int lives = 5;
    private Label scoreLabel;
    private Label livesCountLabel;
    private ImageView heartIcon;
    private HBox livesBox;
    private boolean gameOver = false;
    private boolean paused = false;
    private AnimationTimer timer;

    @Override
    public void start(Stage stage) {
        sendStatusToServer(SystemInfo.playerName, "playing");
        resetGameStatus(); // 🆕 每次開局重置狀態
        Pane root = new Pane();

        Rectangle background = new Rectangle(650, 800);
        background.setFill(Color.web("#2c3e50"));
        root.getChildren().add(background);

        Scene scene = new Scene(root, 650, 720);

        ball = new Ball(300, 400, 10);
        root.getChildren().add(ball);

        paddle = new Paddle(250, 630, 150, 15);
        root.getChildren().add(paddle);

        String[] colors = {"#fdad34", "#74fd34", "#fd3464", "#34fdc1", "#d534fd"};
        int rows = 5, cols = 8;
        double brickWidth = 60, brickHeight = 20, startX = 50, startY = 80, gap = 10;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double x = startX + col * (brickWidth + gap);
                double y = startY + row * (brickHeight + gap);
                Brick brick = new Brick(x, y, brickWidth, brickHeight, colors[row % colors.length]);
                bricks.add(brick);
                root.getChildren().add(brick);
            }
        }

        scoreLabel = new Label("Score: 0");
        scoreLabel.setLayoutX(20);
        scoreLabel.setLayoutY(20);
        scoreLabel.setFont(new Font("Arial", 24));
        scoreLabel.setTextFill(Color.WHITE);
        root.getChildren().add(scoreLabel);

        Image heartImage = new Image(getClass().getResource("/images/heart.png").toString());
        heartIcon = new ImageView(heartImage);
        heartIcon.setFitWidth(24);
        heartIcon.setFitHeight(24);

        livesCountLabel = new Label(" " + lives);
        livesCountLabel.setFont(new Font("Arial", 24));
        livesCountLabel.setTextFill(Color.WHITE);

        livesBox = new HBox(heartIcon, livesCountLabel);
        livesBox.setLayoutX(500);
        livesBox.setLayoutY(20);
        root.getChildren().add(livesBox);

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT:
                    paddle.moveLeft();
                    break;
                case RIGHT:
                    paddle.moveRight();
                    break;
                case P:
                    togglePause();
                    break;
                default:
                    break;
            }
        });

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (System.currentTimeMillis() - lastCheckTime > 1000) {
                    checkOpponentStatus();
                    lastCheckTime = System.currentTimeMillis();
                }

                if (now - lastCheckTime > 1_000_000_000) {
                    checkOpponentStatus();
                    lastCheckTime = now;
                }
                if (!gameOver && !paused) {
                    ball.move();
                    checkCollision();
                }
            }
        };
        timer.start();

        stage.setTitle("Brick Breaker");
        stage.setScene(scene);
        stage.show();
    }

    private void checkCollision() {
        if (ball.getCenterX() - ball.getRadius() <= 0 || ball.getCenterX() + ball.getRadius() >= 650) {
            ball.reverseX();
        }
        if (ball.getCenterY() - ball.getRadius() <= 0) {
            ball.reverseY();
        }

        if (ball.getBoundsInParent().intersects(paddle.getBoundsInParent())) {
            ball.reverseY();
        }

        for (int i = 0; i < bricks.size(); i++) {
            Brick brick = bricks.get(i);
            if (ball.getBoundsInParent().intersects(brick.getBoundsInParent())) {
                ball.reverseY();
                bricks.remove(i);
                brick.setVisible(false);
                score += 10;
                scoreLabel.setText("Score: " + score);

                ball.speedUp(1.05);
                if (paddle.getWidth() > 80) {
                    paddle.setWidth(paddle.getWidth() - 5);
                }

                if (bricks.isEmpty()) {
                    winGame();
                }
                break;
            }
        }

        if (ball.getCenterY() - ball.getRadius() >= 800) {
            lives--;
            updateLivesLabel();

            if (lives <= 0) {
                endGame();
            } else {
                resetBall();
            }
        }
    }

    private void updateLivesLabel() {
        livesCountLabel.setText(" " + lives);
    }

    private void resetBall() {
        ball.setCenterX(300);
        ball.setCenterY(400);
        ball.reverseY();
    }

    private void showScoreUploadDialog(int score) {
        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog("Player");
            dialog.setTitle("上傳分數");
            dialog.setHeaderText("遊戲結束！你的分數是：" + score);
            dialog.setContentText("請輸入你的名稱：");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                uploadScore(name, score);
                LeaderboardPage.show(); // 完成輸入才顯示排行榜
            });
        });
    }

    private void uploadScore(String name, int score) {
        try {
            URL url = new URL(serverURL + "/upload_score");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("score", score);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode(); // ✅ 取得回傳碼
            System.out.println("回傳狀態碼：" + responseCode); // ✅ 顯示是否成功
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void endGame() {
        sendStatusToServer(SystemInfo.playerName, "finished");
        gameOver = true;
        timer.stop();

        Label gameOverLabel = new Label("GAME OVER");
        gameOverLabel.setFont(new Font("Arial", 48));
        gameOverLabel.setTextFill(Color.RED);
        gameOverLabel.setLayoutX(180);
        gameOverLabel.setLayoutY(300);

        Button retryButton = new Button("重玩一局");
        retryButton.setLayoutX(250);
        retryButton.setLayoutY(400);
        retryButton.setPrefWidth(150);
        retryButton.setStyle("-fx-background-color: orange; -fx-font-size: 16px;");

        Button backButton = new Button("回主畫面");
        backButton.setLayoutX(250);
        backButton.setLayoutY(460);
        backButton.setPrefWidth(150);
        backButton.setStyle("-fx-background-color: gray; -fx-font-size: 16px;");

        Pane parent = (Pane) ball.getParent();
        parent.getChildren().addAll(gameOverLabel, retryButton, backButton);

        retryButton.setOnAction(e -> {
            Stage stage = (Stage) parent.getScene().getWindow();
            stage.close();
            new App().start(new Stage()); // 重新啟動 App
        });

        backButton.setOnAction(e -> {
            Stage stage = (Stage) parent.getScene().getWindow();
            stage.close();
            new MainMenu().start(new Stage()); // 回主畫面
        });

        // 🆕 輸入名稱並上傳分數（完成後再顯示排行榜）
        showScoreUploadDialog(score);
    }

    private void winGame() {
        gameOver = true;
        timer.stop();

        Label winLabel = new Label("YOU WIN!! 🎉");
        winLabel.setFont(new Font("Arial", 48));
        winLabel.setTextFill(Color.GOLD);
        winLabel.setLayoutX(150); // 可調整置中位置
        winLabel.setLayoutY(300);

        Button retryButton = new Button("重玩一局");
        retryButton.setLayoutX(250);
        retryButton.setLayoutY(400);
        retryButton.setPrefWidth(150);
        retryButton.setStyle("-fx-background-color: orange; -fx-font-size: 16px;");
        retryButton.setOnAction(e -> {
            SystemInfo.sendSetStatus(serverURL, playerId, "finished");
            new App().start(new Stage());
            ((Stage) retryButton.getScene().getWindow()).close();
        });

        Button backButton = new Button("回主畫面");
        backButton.setLayoutX(250);
        backButton.setLayoutY(460);
        backButton.setPrefWidth(150);
        backButton.setStyle("-fx-background-color: gray; -fx-font-size: 16px;");
        backButton.setOnAction(e -> {
            new MainMenu().start(new Stage());
            ((Stage) backButton.getScene().getWindow()).close();
        });

        // 輸入玩家名稱後上傳分數
        showScoreUploadDialog(score);

        Pane parent = (Pane) ball.getParent();
        parent.getChildren().addAll(winLabel, retryButton, backButton);
    }

    private void togglePause() {
        paused = !paused;
    }

    public static void main(String[] args) {
        launch();
    }

    private void sendStatusToServer(String player, String status) {
        new Thread(() -> {
            try {
                URL url = new URL(serverURL + "/set_status");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);
                String jsonInput = String.format("{\"player\":\"%s\", \"status\":\"%s\"}", player, status);
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInput.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                conn.getInputStream().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}
