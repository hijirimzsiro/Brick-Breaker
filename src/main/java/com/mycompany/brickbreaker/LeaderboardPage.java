package com.mycompany.brickbreaker;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

public class LeaderboardPage {

    public static void show() {
        Platform.runLater(() -> {
            Stage stage = new Stage();
            VBox root = new VBox(10);
            root.setAlignment(Pos.CENTER);
            root.setStyle("-fx-background-color: #2c3e50;");

            Label title = new Label("🏆 Leaderboard");
            title.setFont(new Font("Arial", 36));
            title.setTextFill(Color.GOLD);
            root.getChildren().add(title);

            try {
                URL url = new URL("http://127.0.0.1:5000/leaderboard");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
                reader.close();

                JSONArray arr = new JSONArray(json.toString());

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject entry = arr.getJSONObject(i);
                    String player = entry.getString("name");
                    int score = entry.getInt("score");

                    Label label = new Label((i + 1) + ". " + player + ": " + score);
                    label.setFont(new Font("Arial", 20));
                    label.setTextFill(Color.WHITE);
                    root.getChildren().add(label);
                }

            } catch (Exception e) {
                Label error = new Label("Failed to load leaderboard.");
                error.setTextFill(Color.RED);
                root.getChildren().add(error);
            }

            Scene scene = new Scene(root, 400, 400);
            stage.setScene(scene);
            stage.setTitle("Leaderboard");
            stage.show();
        });
    }
}
