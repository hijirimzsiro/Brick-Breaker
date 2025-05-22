package com.mycompany.brickbreaker;

public class SystemInfo {
    public static String playerName = "player1"; // 預設值，可由主選單指定

    public static String javaVersion() {
        return System.getProperty("java.version");
    }

    public static String javafxVersion() {
        return System.getProperty("javafx.version");
    }

    public static Difficulty difficulty = Difficulty.MEDIUM;

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
}