package com.mycompany.brickbreaker;

import javafx.scene.control.TextField;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class MainMenu extends Application {

    @Override
    public void start(Stage stage) {
        Label title = new Label("BRICK BREAKER");
        title.setFont(Font.font("Arial", 48));
        title.setTextFill(Color.WHITE);

        TextField nameField = new TextField();
        nameField.setPromptText("輸入玩家名稱");
        nameField.setMaxWidth(200);

        Button easyBtn = new Button("簡單模式");
        Button mediumBtn = new Button("普通模式");
        Button hardBtn = new Button("困難模式");
        Button leaderboardBtn = new Button("排行榜");

        easyBtn.setPrefWidth(200);
        mediumBtn.setPrefWidth(200);
        hardBtn.setPrefWidth(200);
        leaderboardBtn.setPrefWidth(200);

        easyBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                SystemInfo.playerName = name;
            } else {
                SystemInfo.playerName = "player";
            }

            SystemInfo.difficulty = SystemInfo.Difficulty.EASY;
            new App().start(new Stage());
            stage.close();
        });

        mediumBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                SystemInfo.playerName = name;
            } else {
                SystemInfo.playerName = "player";
            }

            SystemInfo.difficulty = SystemInfo.Difficulty.MEDIUM;
            new App().start(new Stage());
            stage.close();
        });

        hardBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                SystemInfo.playerName = name;
            } else {
                SystemInfo.playerName = "player";
            }

            SystemInfo.difficulty = SystemInfo.Difficulty.HARD;
            new App().start(new Stage());
            stage.close();
        });

        leaderboardBtn.setOnAction(e -> {
            LeaderboardPage.show();
        });

        VBox menu = new VBox(20, title, nameField, easyBtn, mediumBtn, hardBtn, leaderboardBtn);
        menu.setAlignment(Pos.CENTER);
        menu.setStyle("-fx-background-color: #2c3e50;");

        Scene scene = new Scene(menu, 650, 800);
        stage.setTitle("主選單");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
