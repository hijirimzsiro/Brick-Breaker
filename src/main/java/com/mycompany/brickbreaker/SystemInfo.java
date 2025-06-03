package com.mycompany.brickbreaker;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONObject;

public class SystemInfo {
    public static String playerName = "player1"; // 可在主選單更改

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
        return playerName; // "player1" 或 "player2"
    }

    // ✅ 發送當前狀態到後端（playing / finished）
    public static void sendSetStatus(String serverURL, String playerId, String status) {
        try {
            URL url = new URL(serverURL + "/set_status");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");

            String jsonInput = String.format("{\"player\":\"%s\", \"status\":\"%s\"}", playerId, status);
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInput.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            con.getResponseCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ 開始輪詢對手狀態（每秒檢查一次）
    public static void startStatusPolling(String serverURL, String selfPlayerId, Runnable onOpponentFinished) {
        Timer pollingTimer = new Timer();
        pollingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    URL url = new URL(serverURL + "/get_status");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");

                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String response = in.readLine();
                    in.close();

                    JSONObject json = new JSONObject(response);
                    String opponent = selfPlayerId.equals("player1") ? "player2" : "player1";
                    String status = json.getString(opponent);

                    if ("finished".equals(status)) {
                        pollingTimer.cancel();
                        onOpponentFinished.run();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000);
    }

    // ✅ 重置雙方狀態為 playing
    public static void sendResetRequest(String serverURL) {
        try {
            URL url = new URL(serverURL + "/reset_status");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.getResponseCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
