package com.mycompany.brickbreaker;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONObject;
import javafx.application.Platform;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SystemInfo {

    public static String playerName = "player2"; // 可在主選單更改

    public static Difficulty difficulty = Difficulty.MEDIUM;

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    public static String javaVersion() {
        return System.getProperty("java.version");
    }

    public static String javafxVersion() {
        return System.getProperty("javafx.version");
    }

    // ✅ 根據 playerName 回傳 playerID（供後端使用）
    public static String getPlayerID() {
        return playerName;
    }

    public static void sendSetStatus(String serverURL, String playerId, String status) {
        try {
            URL url = new URL(serverURL + "/set_status");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            String jsonInput = String.format("{\"player\": \"%s\", \"status\": \"%s\"}", playerId, status);

            OutputStream os = conn.getOutputStream();
            os.write(jsonInput.getBytes("utf-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            System.out.println("送出狀態: " + status + " 回應碼: " + responseCode);
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startStatusPolling(String serverURL, String playerId, Runnable onOpponentFinished) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        String opponent = playerId.equals("player1") ? "player2" : "player1";

        scheduler.scheduleAtFixedRate(() -> {
            try {
                URL url = new URL(serverURL + "/get_status");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String json = response.toString();
                String opponentStatus = extractStatus(json, opponent);

                System.out.println("對手狀態: " + opponentStatus);

                if ("finished".equals(opponentStatus)) {
                    System.out.println("對手已結束遊戲，自動同步結束！");
                    scheduler.shutdown();
                    Platform.runLater(onOpponentFinished);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    private static String extractStatus(String json, String player) {
        int index = json.indexOf(player);
        if (index == -1) {
            return "";
        }
        int colon = json.indexOf(":", index);
        int quoteStart = json.indexOf("\"", colon);
        int quoteEnd = json.indexOf("\"", quoteStart + 1);
        return json.substring(quoteStart + 1, quoteEnd);
    }

}
